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

public class ShipInteractionExecutor implements CommandExecutor {
    private final FrogShip plugin;
    private final Random random = new Random();

    public ShipInteractionExecutor(FrogShip plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // Команда: /sitonship (только для игрока, который сам её вводит)
        if (command.getName().equalsIgnoreCase("sitonship")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Эту команду может использовать только игрок!");
                return true;
            }
            return trySeatPlayer(player);
        }

        // Команда: /embark <игрок> (может выполнить командный блок)
        if (command.getName().equalsIgnoreCase("embark")) {
            if (args.length < 1) {
                sender.sendMessage("§cИспользование: /embark <ник_игрока>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cОшибка: Игрок " + args[0] + " не найден!");
                return true;
            }

            return trySeatPlayer(target);
        }

        // Команда: /sitoff (выход)
        if (command.getName().equalsIgnoreCase("sitoff")) {
            if (!(sender instanceof Player player)) return true;
            return handleSitOff(player);
        }

        return false;
    }

    // Универсальный метод посадки игрока на любое свободное место
    private boolean trySeatPlayer(Player player) {
        List<ArmorStand> seats = player.getWorld().getEntitiesByClass(ArmorStand.class).stream()
                .filter(as -> as.getScoreboardTags().contains("ship_seat_player"))
                .filter(as -> as.getPassengers().isEmpty())
                .toList();

        if (seats.isEmpty()) {
            player.sendMessage("§cНа корабле нет свободных мест!");
            return true;
        }

        ArmorStand randomSeat = seats.get(random.nextInt(seats.size()));
        randomSeat.addPassenger(player);
        player.sendMessage("§aВы были размещены на корабле!");
        return true;
    }

    private boolean handleSitOff(Player player) {
        if (player.getVehicle() instanceof ArmorStand as && as.getScoreboardTags().contains("ship_seat")) {
            player.addScoreboardTag("is_leaving_ship");
            Location exit = player.getLocation().add(0, 1.0, 0);
            player.leaveVehicle();
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(exit);
                player.sendMessage("§eВы сошли с корабля.");
            });
            return true;
        }
        player.sendMessage("§cВы не на корабле!");
        return true;
    }
}
