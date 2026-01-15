// src/main/java/AlexTro/frogShip/ShipCommandExecutor.java

package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShipCommandExecutor implements CommandExecutor {

    private final FrogShip plugin;

    public ShipCommandExecutor(FrogShip plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("sitonship")) {
            if (plugin.getSeat() == null || plugin.getSeat().isDead()) {
                player.sendMessage("§cСначала заспавни корабль командой /spawnship!");
                return true;
            }
            plugin.getSeat().addPassenger(player);
            player.sendMessage("§aВы сели на корабль!");
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

                // Вызываем логику спавна из отдельного класса
                ShipSpawner.spawnMovingPlatform(plugin, targetLoc);
                player.sendMessage(String.format("§eКорабль заспавнен на координатах: %d, %d, %d", x, y, z));

            } catch (NumberFormatException e) {
                player.sendMessage("§cОшибка: Координаты должны быть целыми числами!");
            }
            return true;
        }
        return false;
    }
}
