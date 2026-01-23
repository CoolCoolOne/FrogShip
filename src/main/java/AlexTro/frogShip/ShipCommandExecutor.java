// src/main/java/AlexTro/frogShip/ShipCommandExecutor.java

package AlexTro.frogShip;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class ShipCommandExecutor implements CommandExecutor {

  private final FrogShip plugin;
  private final Random random = new Random(); // Создаем один раз для оптимизации

  public ShipCommandExecutor(FrogShip plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    plugin.getLogManager().log(sender.getName(), label);

    org.bukkit.World targetWorld;
    if (sender instanceof Player player) {
      targetWorld = player.getWorld();
    } else {
      String worldName = plugin.getConfig().getString("ship-world", "world");
      targetWorld = Bukkit.getWorld(worldName);
    }

    if (targetWorld == null) {
      sender.sendMessage("§cОшибка: Мир не найден в конфиге!");
      return true;
    }

    // --- Новая команда: /rmship ---
    if (command.getName().equalsIgnoreCase("rmship")) {
      if (plugin.getActiveShips().isEmpty()) {
        Bukkit.broadcastMessage("Корабля сейчас нет.");
        return true;
      }
      plugin.removeAllShips();
      Bukkit.broadcastMessage("§6[FrogShip] §eКорабль был отозван командой /rmship.");
      return true;
    }

    // Логика посадки на случайное мангровое сиденье
    if (command.getName().equalsIgnoreCase("sitonship")) {

      if (!(sender instanceof Player player)) {
        sender.sendMessage("Командный блок не может сидеть на корабле!");
        return true;
      }

      List<ArmorStand> playerSeats = player.getWorld().getEntitiesByClass(ArmorStand.class).stream()
          .filter(as -> as.getScoreboardTags().contains("ship_seat_player"))
          .filter(as -> as.getPassengers().isEmpty())
          .toList();

      if (playerSeats.isEmpty()) {
        player.sendMessage("§cНет свободных мангровых мест!");
        return true;
      }

      ArmorStand randomSeat = playerSeats.get(random.nextInt(playerSeats.size()));
      randomSeat.addPassenger(player);
      player.sendMessage("§aВы сели на мангровое сиденье!");
      return true;
    }

    // Логика принудительного выхода
    if (command.getName().equalsIgnoreCase("sitoff")) {

      if (!(sender instanceof Player player)) {
        sender.sendMessage("Команда только от игрока!");
        return true;
      }
      if (player.getVehicle() instanceof ArmorStand as
          && as.getScoreboardTags().contains("ship_seat")) {
        player.addScoreboardTag("is_leaving_ship");

        // 1. Запоминаем локацию выхода (чуть выше сиденья или в конкретную точку)
        Location exitLoc = player.getLocation().add(0, 1.0, 0);

        // 2. Снимаем игрока
        player.leaveVehicle();

        // 3. Телепортируем в безопасное место в следующем тике, чтобы перебить
        // ванильное
        // выталкивание
        Bukkit.getScheduler()
            .runTask(
                plugin,
                () -> {
                  player.teleport(exitLoc);
                  player.sendMessage("§eВы сошли с корабля.");
                });
      } else {
        player.sendMessage("§cВы не сидите на корабле!");
      }
      return true;
    }

    if (command.getName().equalsIgnoreCase("spawnship")) {
      // Проверка наличия корабля через plugin, а сообщения шлем отправителю (sender)
      if (!plugin.getActiveShips().isEmpty()) {
        sender.sendMessage("§cКорабль уже заспавнен! Сначала удалите его командой /rmship.");
        return true;
      }

      if (args.length < 3) {
        sender.sendMessage("§cИспользование: /spawnship <x> <y> <z>");
        return true;
      }

      try {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        int z = Integer.parseInt(args[2]);

        // Используем targetWorld, который определен в начале onCommand
        Location targetLoc = new Location(targetWorld, x + 0.5, y + 0.5, z + 0.5);

        plugin.removeAllShips();
        plugin.cleanAllWorldsFromShips();

        ShipSpawner.spawn(plugin, targetLoc);

        // Красивое уведомление в чат всем игрокам о появлении корабля
        Bukkit.broadcastMessage(
            String.format("§6[FrogShip] §eКорабль появился на координатах: %d, %d, %d", x, y, z));

      } catch (NumberFormatException e) {
        sender.sendMessage("§cОшибка: Координаты должны быть целыми числами!");
      }
      return true;
    }

    if (command.getName().equalsIgnoreCase("spawnfrogs")) {

      // Получаем все ArmorStand в целевом мире
      List<ArmorStand> allArmorStands = targetWorld.getEntitiesByClass(ArmorStand.class).stream().toList();
      // Bukkit.broadcastMessage("§7[Debug] Всего ArmorStand в мире: " +
      // allArmorStands.size());

      // Фильтруем по тегу ship_seat_mob
      List<ArmorStand> mobSeats = allArmorStands.stream()
          .filter(as -> as.getScoreboardTags().contains("ship_seat_mob"))
          .toList();

      // Bukkit.broadcastMessage("§7[Debug] Найдено сидений с тегом 'ship_seat_mob': "
      // + mobSeats.size());

      // Фильтруем только те, где нет пассажиров
      List<ArmorStand> emptyMobSeats = mobSeats.stream().filter(as -> as.getPassengers().isEmpty()).toList();

      // Bukkit.broadcastMessage("§7[Debug] Из них свободно: " +
      // emptyMobSeats.size());

      if (emptyMobSeats.isEmpty()) {
        Bukkit.broadcastMessage("§c[!] Нет свободных бамбуковых мест для спавна лягушек.");
        return true;
      }

      int spawnedCount = 0;
      for (ArmorStand seat : emptyMobSeats) {
        try {
          // Используем targetWorld вместо player.getWorld()
          targetWorld.spawn(
              seat.getLocation(),
              org.bukkit.entity.Frog.class,
              frog -> {
                boolean success = seat.addPassenger(frog);
                frog.setInvulnerable(true);

                org.bukkit.entity.Frog.Variant[] variants = org.bukkit.entity.Frog.Variant.values();
                frog.setVariant(variants[random.nextInt(variants.length)]);

                if (!success) {
                  Bukkit.broadcastMessage(
                      "§e[Debug] Не удалось посадить лягушку на сиденье в "
                          + seat.getLocation().toVector());
                }
              });
          spawnedCount++;
        } catch (Exception e) {
          Bukkit.broadcastMessage("§c[Debug] Ошибка при спавне лягушки: " + e.getMessage());
        }
      }

      Bukkit.broadcastMessage("§a[Готово] Заспавнено лягушек: " + spawnedCount);
      return true;
    }

    // --- Команда: /shipdj (только спавн лягушки) ---
    if (command.getName().equalsIgnoreCase("frogmusic")) {

      // 1. Ищем ArmorStand, который ShipBlockProcessor пометил тегом ship_seat_dj
      ArmorStand djSeat = targetWorld.getEntitiesByClass(ArmorStand.class).stream()
          .filter(as -> as.getScoreboardTags().contains("ship_seat_dj"))
          .filter(as -> as.getPassengers().isEmpty()) // Проверяем, что место свободно
          .findFirst()
          .orElse(null);

      if (djSeat == null) {
        sender.sendMessage(
            "§c[!] Спец-место для Певун не найдено! Убедитесь, что корабль заспавнен и в нем есть блок света уровня 1.");
        return true;
      }

      // 2. Спавним особую лягушку на это место
      targetWorld.spawn(djSeat.getLocation(), org.bukkit.entity.Frog.class, frog -> {
        frog.setCustomName("§6§lДжейсон Фандерберкер");
        frog.setCustomNameVisible(true);
        frog.setInvulnerable(true); // Чтобы не убили
        frog.setGlowing(true); // Чтобы выделялась

        // Устанавливаем белый (теплый) вариант
        frog.setVariant(org.bukkit.entity.Frog.Variant.TEMPERATE);

        // --- УВЕЛИЧЕНИЕ РАЗМЕРА ---
        // Значение 2.0 сделает её в 2 раза больше, 3.0 - в три и так далее.
        double scaleValue = 2.5;
        org.bukkit.attribute.AttributeInstance scaleAttr = frog.getAttribute(org.bukkit.attribute.Attribute.SCALE);
        if (scaleAttr != null) {
          scaleAttr.setBaseValue(scaleValue);
        }
        // --------------------------
        // Сажаем лягушку на ArmorStand
        djSeat.addPassenger(frog);
        frog.setRotation(djSeat.getLocation().getYaw(), 0);

        // Важно: помечаем лягушку ключом корабля для автоматического удаления через
        // /rmship
        frog.getPersistentDataContainer().set(
            plugin.getShipKey(),
            org.bukkit.persistence.PersistentDataType.BYTE,
            (byte) 1);
      });

      sender.sendMessage("§a[FrogShip] Певун успешно заспавнен на свое место!");
      return true;
    }

    return false; // Добавлен возврат, если ни одна команда не подошла
  } // Конец метода onCommand
} // Конец класса ShipCommandExecuto
