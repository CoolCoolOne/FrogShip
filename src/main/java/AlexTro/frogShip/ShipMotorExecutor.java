package AlexTro.frogShip;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ShipMotorExecutor implements CommandExecutor {

    private final FrogShip plugin;

    public ShipMotorExecutor(FrogShip plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Определяем желаемое состояние по названию команды
        boolean enable = command.getName().equalsIgnoreCase("shipmotor_on");

        // 1. Сохраняем настройку в конфиг, чтобы она применилась к будущим запускам корабля
        plugin.getConfig().set("ship.motor-enabled", enable);
        plugin.saveConfig();

        // 2. Пытаемся применить настройку к уже летящему кораблю
        ShipMoveTask moveTask = plugin.getActiveMoveTask();
        if (moveTask != null) {
            moveTask.setMotorEnabled(enable);
        }

        // 3. Уведомление
        if (enable) {
            sender.sendMessage("§6[FrogShip] §eЗвук двигателя включен и сохранен в настройках.");
        } else {
            sender.sendMessage("§7[FrogShip] §8Звук двигателя выключен и сохранен в настройках.");
        }

        return true;
    }
}
