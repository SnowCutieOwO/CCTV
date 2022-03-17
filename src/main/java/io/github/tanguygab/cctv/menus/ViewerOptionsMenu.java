package io.github.tanguygab.cctv.menus;

import io.github.tanguygab.cctv.managers.ViewerManager;
import io.github.tanguygab.cctv.utils.Heads;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class ViewerOptionsMenu extends CCTVMenu {

    private final ViewerManager vm = cctv.getViewers();

    public ViewerOptionsMenu(Player p) {
        super(p);
    }

    @Override
    public void open() {
        inv = Bukkit.getServer().createInventory(null, 9, lang.CAMERA_VIEW_OPTIONS_TITLE);
        if (hasItemPerm(p,"nightvision")) inv.setItem(3, p.hasPotionEffect(PotionEffectType.NIGHT_VISION) ? Heads.NIGHT_VISION_ON.get() : Heads.NIGHT_VISION_OFF.get());

        if (hasItemPerm(p,"zoom")) {
            PotionEffect effect = p.getPotionEffect(PotionEffectType.SLOW);
            inv.setItem(4, getItem(Heads.ZOOM,
                    effect != null
                            ? lang.getCameraViewZoom(effect.getAmplifier()+1)
                            : lang.CAMERA_VIEW_OPTIONS_ZOOM_OFF
            ));
        }

        if (hasItemPerm(p,"spot")) inv.setItem(5, getItem(Heads.SPOTTING,lang.CAMERA_VIEW_OPTIONS_SPOT));

        inv.setItem(8, getItem(Heads.EXIT,lang.CAMERA_VIEW_OPTIONS_BACK));
        p.openInventory(inv);
    }

    private boolean hasItemPerm(Player p, String perm) {
        return vm.CISWP || p.hasPermission("cctv.view."+perm);
    }

    @Override
    public void onClick(ItemStack item, int slot) {
        switch (slot) {
            case 3 -> nightvision(p, !p.hasPotionEffect(PotionEffectType.NIGHT_VISION));
            case 4 -> {
                PotionEffect effect = p.getPotionEffect(PotionEffectType.SLOW);
                if (effect == null) {
                    zoom(p, 1);
                    return;
                }
                int zoom = effect.getAmplifier()+1;
                zoom(p, zoom == 6 ? 0 : zoom+1);

            }
            case 5 -> spotting(p);
            case 8 -> p.closeInventory();
        }
    }

    private void spotting(Player p) {
        if (!p.hasPermission("cctv.view.spot")) {
            p.sendMessage(lang.NO_PERMISSIONS);
            return;
        }
        p.closeInventory();
        List<Player> spotted = new ArrayList<>();
        for (Player viewed : Bukkit.getOnlinePlayers())
            if (spot(p, viewed,true))
                spotted.add(viewed);

        Bukkit.getScheduler().scheduleSyncDelayedTask(cctv, () -> spotted.forEach(viewed->spot(viewed,viewed,false)), vm.TIME_FOR_SPOT * 20L);
    }
    private boolean spot(Player viewer, Player viewed, boolean glow) {
        if (!viewed.canSee(viewed)) return false;
        EntityPlayer viewedNMS = ((CraftPlayer)viewed).getHandle();
        viewedNMS.i(glow); //setGlowingTag(boolean)
        if (!glow) {
            viewed.setSneaking(true); // yeah, I'm doing that because it doesn't want to work with PacketPlayOutEntityMetadata...
            viewed.setSneaking(false);
            return true;
        }
        PlayerConnection connection = ((CraftPlayer)viewer).getHandle().b;
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(viewedNMS.ae(),viewedNMS.ai(),true);
        connection.a(packet);
        return true;
    }
    private void nightvision(Player p, boolean vision) {
        if (!p.hasPermission("cctv.view.nightvision")) {
            p.sendMessage(lang.NO_PERMISSIONS);
            return;
        }
        Inventory inv = p.getOpenInventory().getTopInventory();
        if (vision) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 60000000, 0, false, false));
            inv.setItem(3, Heads.NIGHT_VISION_ON.get());
            return;
        }
        p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        inv.setItem(3, Heads.NIGHT_VISION_OFF.get());
    }
    private void zoom(Player p, int zoomlevel) {
        if (!p.hasPermission("cctv.view.zoom")) {
            p.sendMessage(lang.NO_PERMISSIONS);
            return;
        }
        Inventory inv = p.getOpenInventory().getTopInventory();
        if (zoomlevel == 0) {
            p.removePotionEffect(PotionEffectType.SLOW);
            inv.setItem(4, Heads.ZOOM.get());
            return;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60000000, zoomlevel - 1, false, false));
        inv.setItem(4, getItem(Heads.ZOOM,lang.getCameraViewZoom(zoomlevel)));
    }
}
