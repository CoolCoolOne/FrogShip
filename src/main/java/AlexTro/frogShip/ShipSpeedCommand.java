package AlexTro.frogShip;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ShipSpeedCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {


        if (args.length < 1) {
            sender.sendMessage("§6[FrogShip] §eИспользование: /ship_speedset [0-5]");
            sender.sendMessage("§70-медленно, 1-дефолт, 4-быстро, 5-стоп");
            return true;
        }

        try {
            int level = Integer.parseInt(args[0]);
            double multiplier;

            switch (level) {
                case 0 -> multiplier = 0.5; // Скорость 0.05
                case 1 -> multiplier = 1.0; // Скорость 0.1 (дефолт)
                case 2 -> multiplier = 2.0; // Скорость 0.2
                case 3 -> multiplier = 4.0; // Скорость 0.4
                case 4 -> multiplier = 8.0; // Скорость 0.8 (очень быстро)
                case 5 -> multiplier = 0.0; // ОСТАНОВКА
                default -> {
                    sender.sendMessage("§cВыберите уровень от 0 до 5!");
                    return true;
                }
            }

            Settings.speedMultiplier = multiplier;

            if (multiplier == 0) {
                sender.sendMessage("§6[FrogShip] §fКорабль §cостановлен§f.");
            } else {
                sender.sendMessage("§6[FrogShip] §fСкорость установлена на уровень §e" + level);
            }

        } catch (NumberFormatException e) {
            sender.sendMessage("§cВведите корректное число!");
        }

        return true;
    }
}
