package io.github.tanguygab.cctv.old.functions;

import java.util.ArrayList;
import java.util.UUID;

import io.github.tanguygab.cctv.CCTV;
import io.github.tanguygab.cctv.config.LanguageFile;
import io.github.tanguygab.cctv.entities.Viewer;
import io.github.tanguygab.cctv.entities.Camera;
import io.github.tanguygab.cctv.managers.CameraManager;
import io.github.tanguygab.cctv.old.events.PlayerInventoryClickEvent;
import io.github.tanguygab.cctv.old.library.Search;
import io.github.tanguygab.cctv.entities.Computer;
import io.github.tanguygab.cctv.utils.Heads;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;

import io.github.tanguygab.cctv.old.library.Arguments;

public class camerafunctions {

  public static void immediateViewCamera(String id, Player player) {
    Camera cam = CCTV.get().getCameras().get(id);
    if (cam == null) {
      CCTV.get().getViewers().delete(player);
      player.sendMessage(CCTV.get().getLang().CAMERA_NOT_FOUND);
      return;
    }
    teleportToCamera(id, player);
  }
  
  public static void teleportToCamera(String id, Player player) {
    Camera cam = CCTV.get().getCameras().get(id);
    if (cam == null) {
      return;
    }
    double Degrees_Yaw = cam.getArmorStand().getEyeLocation().getYaw();
    double Degrees_Pitch = cam.getArmorStand().getEyeLocation().getPitch();
    double radian_yaw = Math.toRadians(Degrees_Yaw);
    double radian_pitch = Math.toRadians(Degrees_Pitch);
    double radius_head = 0.29D;
    double radius = CCTV.get().CAMERA_HEAD_RADIUS;
    double x = cam.getArmorStand().getLocation().getX();
    double y = cam.getArmorStand().getLocation().getY() + 0.115D;
    double z = cam.getArmorStand().getLocation().getZ();
    double x2;
    double y2;
    double z2;
    double l3;
    double x3;
    double y3;
    double z3;
    l3 = radius_head * Math.sin(radian_pitch);
    boolean b = Math.abs(Degrees_Yaw) > 90.0D && Math.abs(Degrees_Yaw) <= 270.0D;
    if (b)
      l3 = -l3;
    x3 = l3 * Math.sin(radian_yaw);
    y3 = Math.sqrt(Math.pow(radius_head, 2.0D) - Math.pow(l3, 2.0D));
    z3 = Math.sqrt(Math.pow(l3, 2.0D) - Math.pow(x3, 2.0D));
    if (Degrees_Pitch < 0.0D)
      z3 = -z3;
    x3 = -x3;
    if (b) {
      x3 = -x3;
      z3 = -z3;
    }
    x2 = radius * Math.sin(radian_yaw);
    z2 = Math.sqrt(Math.pow(radius, 2.0D) - Math.pow(x2, 2.0D));
    y2 = radius * Math.sin(radian_pitch);
    if (b)
      z2 = -z2;
    y2 = -y2;
    x2 = -x2;
    Location loc = new Location(cam.getArmorStand().getLocation().getWorld(), x + x2 + x3, y + y2 + y3 - radius_head, z + z2 + z3, cam.getArmorStand().getLocation().getYaw(), cam.getArmorStand().getLocation().getPitch());
    player.teleport(loc);
  }
  
  public static void moveHere(String name, Player player) {
    LanguageFile lang = CCTV.get().getLang();
    CameraManager cm = CCTV.get().getCameras();
    if (!cm.exists(name)) {
      player.sendMessage(lang.CAMERA_NOT_FOUND);
      return;
    }
      Camera cam = cm.get(name);
      Location loc = player.getLocation();
      ArmorStand armorstand = cam.getArmorStand();
      armorstand.teleport(loc);
      armorstand.setGravity(false);
      armorstand.setCollidable(false);
      armorstand.setInvulnerable(true);
      armorstand.setVisible(false);
      armorstand.setCustomName("CAM-" + cam.getId());
      armorstand.setSilent(true);
      armorstand.getEquipment().setHelmet(Heads.CAMERA_1.get());
      armorstand.setHeadPose(new EulerAngle(Math.toRadians(loc.getPitch()), 0.0D, 0.0D));
      cam.setLocation(loc);
      for (Viewer p : CCTV.get().getViewers().values()) {
        if (p.getCamera() == cam) {
          Player target = Bukkit.getServer().getPlayer(UUID.fromString(p.getId()));
          if (target != null)
            teleportToCamera(name, target);
        }
      }
      player.sendMessage(lang.CAMERA_MOVED);
  }
  
  public static void showHideCamera(String name, Player player, boolean state) {
    LanguageFile lang = CCTV.get().getLang();
    CameraManager cm = CCTV.get().getCameras();
    if (!cm.exists(name)) {
      player.sendMessage(lang.CAMERA_NOT_FOUND);
      return;
    }
    Camera cam = cm.get(name);
    if (!player.hasPermission("cctv.hide") && !cam.getOwner().equals(player.getUniqueId().toString()))
      return;
    if (!state) {
      cam.getArmorStand().getEquipment().setHelmet(null);
      cam.setShown(false);
      player.sendMessage(lang.getCameraDisabled(cam.getId()));
    } else {
      cam.getArmorStand().getEquipment().setHelmet(Heads.CAMERA_1.get());
      cam.setShown(true);
      player.sendMessage(lang.getCameraEnabled(cam.getId()));
    }
  }
  
  public static void disable(String name, Player p) {
    LanguageFile lang = CCTV.get().getLang();
    CameraManager cm = CCTV.get().getCameras();
    if (cm.exists(name)) {
      Camera cam = cm.get(name);
      if (!cam.isEnabled()) {
        p.sendMessage(lang.CAMERA_ALREADY_DISABLED);
        return;
      } 
      if (p.hasPermission("cctv.admin") || cam.getOwner().equals(p.getUniqueId().toString())) {
        for (Viewer player : CCTV.get().getViewers().values()) {
          if (!player.getCamera().getId().equals(name)) continue;
          Player target = Bukkit.getPlayer(UUID.fromString(player.getId()));
          if (target == null) continue;
          if (!target.hasPermission("cctv.camera.view.override") && !target.hasPermission("cctv.admin")) {
            target.sendTitle(lang.CAMERA_OFFLINE,"",0,15,0);
            cm.unviewCamera(target);
            continue;
          }
          target.sendMessage(lang.CAMERA_OFFLINE_OVERRIDE);
        }
        cam.setEnabled(false);
        p.sendMessage(lang.getCameraDisabled(cam.getId()));
      } else {
        p.sendMessage(lang.ONLY_OWNER);
      } 
    } else {
      p.sendMessage(lang.CAMERA_NOT_FOUND);
    } 
  }
  
  public static void enable(String name, Player player) {
    LanguageFile lang = CCTV.get().getLang();
    CameraManager cm = CCTV.get().getCameras();
    if (cm.exists(name)) {
      Camera cam = cm.get(name);
      if (cam.isEnabled()) {
        player.sendMessage(lang.CAMERA_ALREADY_ENABLED);
        return;
      } 
      if (player.hasPermission("cctv.admin") || cam.getOwner().equals(player.getUniqueId().toString())) {
        cam.setEnabled(true);
        player.sendMessage(lang.getCameraEnabled(cam.getId()));
      } else {
        player.sendMessage(lang.ONLY_OWNER);
      } 
    } else {
      player.sendMessage(lang.CAMERA_NOT_FOUND);
    } 
  }
  
  public static void list(Player player, int page, Search s, String search) {
    LanguageFile lang = CCTV.get().getLang();

    ArrayList<Camera> list = new ArrayList<>();
    CCTV.get().getCameras().values().stream().filter(c -> !(s != Search.all && (s != Search.personal || !c.getOwner().equals(player.getUniqueId().toString())) && (s != Search.name || !c.getId().toLowerCase().startsWith(search.toLowerCase())) && (s != Search.player || c.getOwner().equals("none") || !Bukkit.getOfflinePlayer(UUID.fromString(c.getOwner())).getName().startsWith(search)))).forEach(list::add);
    list.sort((c1, c2) -> {
      String name1 = "none";
      String name2 = "none";
      if (!c1.getOwner().equals("none")) {
        OfflinePlayer off1 = Bukkit.getOfflinePlayer(UUID.fromString(c1.getOwner()));
        name1 = off1.getName();
      }
      if (!c2.getOwner().equals("none")) {
        OfflinePlayer off2 = Bukkit.getOfflinePlayer(UUID.fromString(c2.getOwner()));
        name2 = off2.getName();
      }
      return name2.compareTo(name1);
    });
    int maxpages = (int)Math.ceil(list.size() / 8.0D);
    if (page > maxpages && page == 1) {
      player.sendMessage(Arguments.list_no_result.replaceAll("%search%", s.toString()).replaceAll("%value%", search));
      return;
    } 
    if (page > maxpages || page < 1) {
      player.sendMessage(lang.TOO_MANY_PAGES);
      return;
    } 
    for (int a = (page - 1) * 8; a < 8 * page && a < list.size(); a++) {
      Camera rec = list.get(a);
      String name = "none";
      if (!rec.getOwner().equals("none")) {
        OfflinePlayer off = Bukkit.getOfflinePlayer(UUID.fromString(rec.getOwner()));
        name = off.getName();
      } 
      player.sendMessage(((s == Search.all || s == Search.player || s == Search.name) ? lang.getListAdmin(name,rec.getId()) : lang.getList(rec.getId())));
    } 
    player.sendMessage(ChatColor.YELLOW + "===== " + ChatColor.GOLD + page + ChatColor.YELLOW + "/" + ChatColor.GOLD + maxpages + ChatColor.YELLOW + " =====");
  }
  
  public static void setCameraOwner(Player player, String camera, String target) {
    LanguageFile lang = CCTV.get().getLang();
    CameraManager cm = CCTV.get().getCameras();
    byte b;
    int i;
    OfflinePlayer[] arrayOfOfflinePlayer;
    for (i = (arrayOfOfflinePlayer = Bukkit.getOfflinePlayers()).length, b = 0; b < i; ) {
      OfflinePlayer off = arrayOfOfflinePlayer[b];
      if (off != null && off.getName() != null && off.getName().equalsIgnoreCase(target))
        if (cm.exists(camera)) {
          Camera rec = cm.get(camera);
          if (rec.getOwner().equals(player.getUniqueId().toString()) || player.hasPermission("cctv.camera.other")) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(target);
            if (!rec.getOwner().equals(owner.getUniqueId().toString())) {
              rec.setOwner(owner.getUniqueId().toString());
              player.sendMessage(lang.getCameraOwnerChanged(owner.getName()));
              return;
            }
            player.sendMessage(lang.CAMERA_PLAYER_ALREADY_OWNER);
          } else {
            player.sendMessage(lang.CAMERA_CHANGE_NO_PERMS);
          } 
        } else {
          player.sendMessage(lang.CAMERA_NOT_FOUND);
        }  
      b++;
    } 
    player.sendMessage(lang.PLAYER_NOT_FOUND);
  }
  
  public static void getCCTVFromComputer(Player player, Location loc) {
    if (loc.getX() == 0.0D && loc.getY() == 0.0D && loc.getZ() == 0.0D) {
      player.closeInventory();
      return;
    } 
    Computer pc = computerfunctions.getComputerRecordFromLocation(computerfunctions.getLastClickedComputerFromPlayer(player));
    if (pc == null) {
      player.closeInventory();
      return;
    } 
    player.openInventory(PlayerInventoryClickEvent.CCTV(player, 1));
  }

  public static void countConnectedPlayersToCamera(String camera, Player player) {
    LanguageFile lang = CCTV.get().getLang();
    if (!CCTV.get().getCameras().exists(camera)) {
      player.sendMessage(lang.CAMERA_NOT_FOUND);
      return;
    }
    int count = 0;
    for (Viewer playerRec : CCTV.get().getViewers().values())
      if (playerRec.getCamera().getId().equals(camera)) count++;
    player.sendMessage(lang.getCameraViewCount(count,camera));
  }
}

