package AlexTro.frogShip;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.util.Comparator;

public class ShipAudioExecutor implements CommandExecutor {

    private final FrogShip plugin;
    private final String TRACK_ID = "frogship.track";

    public ShipAudioExecutor(FrogShip plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Определяем, есть ли флаг -all (обычно это второй аргумент: /audioship @p -all)
        boolean isAll = args.length > 1 && args[1].equalsIgnoreCase("-all");

        // 1. Команда включения: /audioship <цель> [-all]
        if (command.getName().equalsIgnoreCase("audioship")) {
            if (isAll) {
                Location origin = getOrigin(sender);
                if (origin == null) {
                    sender.sendMessage("§cНе удалось определить координаты для -all.");
                    return true;
                }
                // Ищем всех в радиусе 50 и включаем
                origin.getWorld().getNearbyEntities(origin, 50, 50, 50).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .forEach(this::playShipMusic);
                sender.sendMessage("§aМузыка включена для всех в радиусе 50 блоков.");
                return true;
            }

            // Обычная логика для одного игрока
            Player target = (args.length > 0) ? getTarget(sender, args[0]) : (sender instanceof Player p ? p : null);
            if (target != null) {
                playShipMusic(target);
            } else {
                sender.sendMessage("§cИгрок не найден.");
            }
            return true;
        }

        // 2. Команда выключения: /stopaudioship <цель> [-all]
        if (command.getName().equalsIgnoreCase("stopaudioship")) {
            if (isAll) {
                Location origin = getOrigin(sender);
                if (origin == null) return true;

                origin.getWorld().getNearbyEntities(origin, 50, 50, 50).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .forEach(this::stopShipMusic);
                return true;
            }

            Player target = (args.length > 0) ? getTarget(sender, args[0]) : (sender instanceof Player p ? p : null);
            if (target != null) {
                stopShipMusic(target);
            } else {
                sender.sendMessage("§cИгрок не найден.");
            }
            return true;
        }

        return false;
    }

    // Вспомогательный метод для получения точки отсчета (чтобы не дублировать код)
    private Location getOrigin(CommandSender sender) {
        if (sender instanceof Player p) return p.getLocation();
        if (sender instanceof BlockCommandSender b) return b.getBlock().getLocation();
        if (sender instanceof Entity e) return e.getLocation();
        return null;
    }


    // Универсальный метод поиска цели (Ник или @p)
    private Player getTarget(CommandSender sender, String arg) {
        if (arg.equalsIgnoreCase("@p")) {
            Location origin = null;
            if (sender instanceof Player p) origin = p.getLocation();
            else if (sender instanceof BlockCommandSender b) origin = b.getBlock().getLocation();
            else if (sender instanceof Entity e) origin = e.getLocation();

            if (origin != null) {
                final Location finalLoc = origin;
                return origin.getWorld().getNearbyEntities(origin, 50, 50, 50).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(finalLoc)))
                        .orElse(null);
            }
        }
        return Bukkit.getPlayer(arg);
    }

    private void playShipMusic(Player player) {
        stopShipMusic(player);
        player.playSound(player.getLocation(), TRACK_ID, SoundCategory.RECORDS, 1.0f, 1.0f);
        player.sendMessage("§b[♪] Кто-то вещает с микрофона... [настройте громкость в МУЗЫКАЛЬНЫЕ БЛОКИ]");
    }

    private void stopShipMusic(Player player) {
        player.stopSound(TRACK_ID, SoundCategory.RECORDS);
        player.sendMessage("§7[♪] Вещание остановлено.");
    }

    public void stopAllMusic() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.stopSound(TRACK_ID, SoundCategory.RECORDS);
        }
    }
}
