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

        // Команда: /sitonship (только для игрока)
        if (command.getName().equalsIgnoreCase("sitonship")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Эту команду может использовать только игрок!");
                return true;
            }
            return trySeatPlayer(player);
        }

        // Команда: /embark <цель> [-all]
        if (command.getName().equalsIgnoreCase("embark")) {
            if (args.length < 1) {
                sender.sendMessage("§cИспользование: /embark <ник/@p> [-all]");
                return true;
            }

            // 1. Проверяем флаг -all (он может быть вторым аргументом)
            boolean seatAll = args.length > 1 && args[1].equalsIgnoreCase("-all");

            // 2. Определяем точку отсчета. Используем эффективно финальную переменную для лямбд
            Location tempOrigin = null;
            if (sender instanceof Player p) {
                tempOrigin = p.getLocation();
            } else if (sender instanceof org.bukkit.command.BlockCommandSender block) {
                tempOrigin = block.getBlock().getLocation();
            }

            final Location origin = tempOrigin;

            // 3. Логика массовой посадки
            if (seatAll) {
                if (origin == null) {
                    sender.sendMessage("§cЭту операцию нельзя выполнить из консоли (нет координат).");
                    return true;
                }

                List<Player> targets = origin.getWorld().getNearbyEntities(origin, 50, 50, 50).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .toList();

                if (targets.isEmpty()) {
                    sender.sendMessage("§cИгроки в радиусе 50 блоков не найдены!");
                    return true;
                }

                for (Player p : targets) {
                    trySeatPlayer(p);
                }
                return true;
            }

            // 4. Логика посадки одного игрока
            Player target;
            if (args[0].equalsIgnoreCase("@p")) {
                if (origin == null) {
                    sender.sendMessage("§cНе удалось определить координаты для поиска @p.");
                    return true;
                }
                target = origin.getWorld().getNearbyEntities(origin, 50, 50, 50).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .min((p1, p2) -> Double.compare(p1.getLocation().distance(origin), p2.getLocation().distance(origin)))
                        .orElse(null);
            } else {
                target = Bukkit.getPlayer(args[0]);
            }

            if (target == null) {
                sender.sendMessage("§cОшибка: Игрок не найден!");
                return true;
            }

            return trySeatPlayer(target);
        }

        // Команда: /sitoff (выход)
        if (command.getName().equalsIgnoreCase("sitoff")) {
            if (!(sender instanceof Player player))
                return true;
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
        player.sendMessage("§aВы размещены на корабле!");
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
