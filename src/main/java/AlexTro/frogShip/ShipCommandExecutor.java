// src/main/java/AlexTro/frogShip/ShipCommandExecutor.java

package AlexTro.frogShip;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class ShipCommandExecutor implements CommandExecutor {

    private final FrogShip plugin;
    private final Random random = new Random(); // Создаем один раз для оптимизации

    public ShipCommandExecutor(FrogShip plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

plugin.getLogManager().log(sender.getName(), label);

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков!");
            return true;
        }

        // --- Новая команда: /rmship ---
    if (command.getName().equalsIgnoreCase("rmship")) {
        if (plugin.getActiveShips().isEmpty()) {
            player.sendMessage("§cКорабля сейчас нет в мире!");
            return true;
        }
        plugin.removeAllShips();
        Bukkit.broadcastMessage("§6[FrogShip] §eКорабль был отозван командой /rmship.");
        return true;
    }

        // Логика посадки на случайное мангровое сиденье
        if (command.getName().equalsIgnoreCase("sitonship")) {
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
            if (player.getVehicle() instanceof ArmorStand as && as.getScoreboardTags().contains("ship_seat")) {
                player.addScoreboardTag("is_leaving_ship"); 
                player.leaveVehicle();
                player.sendMessage("§eВы сошли с корабля.");
            } else {
                player.sendMessage("§cВы не сидите на корабле!");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("spawnship")) {
            if (!plugin.getActiveShips().isEmpty()) {
            player.sendMessage("§cКорабль уже заспавнен! Сначала удалите его командой /rmship.");
            return true;
        }
            if (args.length < 3) {
                player.sendMessage("§cИспользование: /spawnship <x> <y> <z>");
                return true;
            }

            try {
                int x = Integer.parseInt(args[0]);
                int y = Integer.parseInt(args[1]);
                int z = Integer.parseInt(args[2]);

                Location targetLoc = new Location(player.getWorld(), x + 0.5, y + 0.5, z + 0.5);

                plugin.removeAllShips();
                plugin.cleanAllWorldsFromShips();

                ShipSpawner.spawn(plugin, targetLoc);
                player.sendMessage(String.format("§eКорабль появился на координатах: %d, %d, %d", x, y, z));

            } catch (NumberFormatException e) {
                player.sendMessage("§cОшибка: Координаты должны быть целыми числами!");
            }
            return true;
        }

if (command.getName().equalsIgnoreCase("spawnfrogs")) {
    player.sendMessage("§7[Debug] Поиск бамбуковых сидений в текущем мире...");

    // Получаем вообще все ArmorStand в мире для сравнения
    List<ArmorStand> allArmorStands = player.getWorld().getEntitiesByClass(ArmorStand.class).stream().toList();
    player.sendMessage("§7[Debug] Всего ArmorStand в мире: " + allArmorStands.size());

    // Фильтруем по тегу ship_seat_mob
    List<ArmorStand> mobSeats = allArmorStands.stream()
            .filter(as -> {
                boolean hasTag = as.getScoreboardTags().contains("ship_seat_mob");
                return hasTag;
            })
            .toList();
    
    player.sendMessage("§7[Debug] Найдено сидений с тегом 'ship_seat_mob': " + mobSeats.size());

    // Фильтруем только пустые
    List<ArmorStand> emptyMobSeats = mobSeats.stream()
            .filter(as -> as.getPassengers().isEmpty())
            .toList();

    player.sendMessage("§7[Debug] Из них свободно (без пассажиров): " + emptyMobSeats.size());

    if (emptyMobSeats.isEmpty()) {
        player.sendMessage("§c[!] Нет свободных бамбуковых мест для спавна лягушек.");
        return true;
    }

    int spawnedCount = 0;
    for (ArmorStand seat : emptyMobSeats) {
        try {
            player.getWorld().spawn(seat.getLocation(), org.bukkit.entity.Frog.class, frog -> {
                boolean success = seat.addPassenger(frog);
                frog.setInvulnerable(true);
                org.bukkit.entity.Frog.Variant[] variants = org.bukkit.entity.Frog.Variant.values();
                frog.setVariant(variants[new Random().nextInt(variants.length)]);
                
                if (!success) {
                    player.sendMessage("§e[Debug] Не удалось посадить лягушку на сиденье в " + seat.getLocation().toVector());
                }
            });
            spawnedCount++;
        } catch (Exception e) {
            player.sendMessage("§c[Debug] Ошибка при спавне лягушки: " + e.getMessage());
        }
    }

    player.sendMessage("§a[Готово] Заспавнено лягушек: " + spawnedCount);
    return true;
} // Конец блока if (spawnfrogs)

        return false; // Добавлен возврат, если ни одна команда не подошла
    } // Конец метода onCommand
} // Конец класса ShipCommandExecuto
