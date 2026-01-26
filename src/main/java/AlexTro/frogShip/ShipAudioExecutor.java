package AlexTro.frogShip;

import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShipAudioExecutor implements CommandExecutor {

    private final FrogShip plugin;
    private final String TRACK_ID = "frogship.track"; // Вынесли в константу для удобства

    public ShipAudioExecutor(FrogShip plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // --- Логика включения ---
        if (command.getName().equalsIgnoreCase("audioship")) {
            if (args.length < 1) {
                sender.sendMessage("§cИспользование: /audioship <игрок>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                playShipMusic(target);
            } else {
                sender.sendMessage("§cИгрок не найден.");
            }
            return true;
        }

        // --- Логика выключения ---
        if (command.getName().equalsIgnoreCase("stopaudioship")) {
            if (args.length < 1) {
                // Если аргументов нет и команду ввел игрок — выключаем ему
                if (sender instanceof Player player) {
                    stopShipMusic(player);
                } else {
                    sender.sendMessage("§cИспользование из консоли: /stopaudioship <игрок>");
                }
                return true;
            }

            // Выключение звука конкретному игроку (для КБ или админа)
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                stopShipMusic(target);
            } else {
                sender.sendMessage("§cИгрок не найден.");
            }
            return true;
        }

        return false;
    }

    private void playShipMusic(Player player) {
        // Останавливаем старый, если он играл, чтобы не было наложения
        player.stopSound(TRACK_ID);

        player.playSound(
                player.getLocation(),
                TRACK_ID,
                SoundCategory.RECORDS, 
                1.0f, 
                1.0f 
        );
        player.sendMessage("§b[♪] На корабле кто то вещает с микрофона [проверьте громкость. настройка ПЛАСТИНКИ].");
    }

    private void stopShipMusic(Player player) {
        player.stopSound(TRACK_ID);
        player.sendMessage("§7[♪] Вещание остановлено.");
    }

    public void stopAllMusic() {
    for (Player player : Bukkit.getOnlinePlayers()) {
        player.stopSound(TRACK_ID);
        // Опционально: можно не слать сообщение каждому, чтобы не спамить при удалении
    }
}
}
