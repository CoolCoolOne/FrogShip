package AlexTro.frogShip;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;

public class ShipCommandExecutor implements CommandExecutor {

  private final FrogShip plugin;
  private final Random random = new Random();

  public ShipCommandExecutor(FrogShip plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    plugin.getLogManager().log(sender.getName(), label);

    World targetWorld = (sender instanceof Player p) ? p.getWorld()
        : Bukkit.getWorld(plugin.getConfig().getString("ship-world", "world"));

    if (targetWorld == null) {
      sender.sendMessage("§cОшибка: Мир не найден!");
      return true;
    }

    switch (command.getName().toLowerCase()) {
      case "ship_rmship":
        return handleRemoveShip(sender);
      case "ship_spawnship":
        return handleSpawnShip(sender, args, targetWorld);
      case "ship_spawnfrogs":
        return handleSpawnFrogs(targetWorld);
      case "ship_spawnjason":
        return handleFrogMusic(sender, targetWorld);
      case "reload_shipconf":
        Settings.load(plugin); // Перезагружаем все данные из конфига в память
        sender.sendMessage("§a[FrogShip] Конфигурация перезагружена!");
        return true;
    }
    return false;
  }

  private boolean handleRemoveShip(CommandSender sender) {
    if (plugin.getActiveShips().isEmpty()) {
      sender.sendMessage("Корабля сейчас нет.");
      return true;
    }
    plugin.removeAllShips();

    for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
      player.stopSound("frogship.track");
    }

    Bukkit.broadcastMessage("§6[FrogShip] §eКорабль был отозван.");
    return true;
  }

  private boolean handleSpawnShip(CommandSender sender, String[] args, World world) {
    if (!plugin.getActiveShips().isEmpty()) {
      sender.sendMessage("§cКорабль уже заспавнен!");
      return true;
    }
    if (args.length < 3)
      return false;

    try {
      int x = Integer.parseInt(args[0]);
      int y = Integer.parseInt(args[1]);
      int z = Integer.parseInt(args[2]);
      Location loc = new Location(world, x + 0.5, y + 0.5, z + 0.5);

      plugin.removeAllShips();
      plugin.cleanAllWorldsFromShips();
      ShipSpawner.spawn(plugin, loc);

      Bukkit.broadcastMessage(String.format("§6[FrogShip] §eКорабль появился на: %d %d %d", x, y, z));
    } catch (NumberFormatException e) {
      sender.sendMessage("§cОшибка координат.");
    }
    return true;
  }

  private boolean handleSpawnFrogs(World world) {
    List<ArmorStand> emptySeats = world.getEntitiesByClass(ArmorStand.class).stream()
        .filter(as -> as.getScoreboardTags().contains("ship_seat_mob"))
        .filter(as -> as.getPassengers().isEmpty())
        .toList();

    if (emptySeats.isEmpty()) {
      Bukkit.broadcastMessage("§cНет свободных мест для лягушек.");
      return true;
    }

    int count = 0;
    for (ArmorStand seat : emptySeats) {
      world.spawn(seat.getLocation(), Frog.class, frog -> {
        seat.addPassenger(frog);
        frog.setInvulnerable(true);
        Frog.Variant[] vars = Frog.Variant.values();
        frog.setVariant(vars[random.nextInt(vars.length)]);
      });
      count++;
    }
    Bukkit.broadcastMessage("§aЗаспавнено лягушек: " + count);
    return true;
  }

  private boolean handleFrogMusic(CommandSender sender, World world) {
    ArmorStand djSeat = world.getEntitiesByClass(ArmorStand.class).stream()
        .filter(as -> as.getScoreboardTags().contains("ship_seat_dj"))
        .filter(as -> as.getPassengers().isEmpty())
        .findFirst().orElse(null);

    if (djSeat == null) {
      sender.sendMessage("§cМесто для певца не найдено!");
      return true;
    }

    world.spawn(djSeat.getLocation(), Frog.class, frog -> {
      frog.setCustomName("§6§lКВАс");
      frog.setCustomNameVisible(true);
      frog.setInvulnerable(true);
      frog.setGlowing(false);
      frog.setAI(true);
      frog.setCollidable(false);
      frog.setVariant(Frog.Variant.TEMPERATE);

      if (frog.getAttribute(Attribute.SCALE) != null) {
        frog.getAttribute(Attribute.SCALE).setBaseValue(2.5);
      }

      djSeat.addPassenger(frog);
      frog.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);
    });

    sender.sendMessage("§aDJ успешно заспавнен!");
    return true;
  }
}
