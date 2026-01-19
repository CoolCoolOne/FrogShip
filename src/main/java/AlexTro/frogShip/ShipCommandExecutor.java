// src/main/java/AlexTro/frogShip/ShipCommandExecutor.java

package AlexTro.frogShip;

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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков!");
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
                player.leaveVehicle();
                player.sendMessage("§eВы сошли с корабля.");
            } else {
                player.sendMessage("§cВы не сидите на корабле!");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("spawnship")) {
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
                player.sendMessage(String.format("§eКорабль заспавнен на координатах: %d, %d, %d", x, y, z));

            } catch (NumberFormatException e) {
                player.sendMessage("§cОшибка: Координаты должны быть целыми числами!");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("spawnfrogs")) {
    // Ищем все бамбуковые сиденья
    List<ArmorStand> mobSeats = player.getWorld().getEntitiesByClass(ArmorStand.class).stream()
            .filter(as -> as.getScoreboardTags().contains("ship_seat_mob")) // Тот самый тег из ShipBlockProcessor
            .filter(as -> as.getPassengers().isEmpty()) // Только свободные
            .toList();

    if (mobSeats.isEmpty()) {
        player.sendMessage("§cНет свободных бамбуковых мест для лягушек!");
        return true;
    }

    for (ArmorStand seat : mobSeats) {
        // Спавним лягушку
        player.getWorld().spawn(seat.getLocation(), org.bukkit.entity.Frog.class, frog -> {
            seat.addPassenger(frog);
            frog.setInvulnerable(true); // Чтобы лягушки не погибли
            // Можно даже сделать их случайного цвета
            org.bukkit.entity.Frog.Variant[] variants = org.bukkit.entity.Frog.Variant.values();
            frog.setVariant(variants[new Random().nextInt(variants.length)]);
        });
    }
    player.sendMessage("§aЛягушки-матросы заняли свои бамбуковые места!");
    return true;
}
        return false;
    }
}
