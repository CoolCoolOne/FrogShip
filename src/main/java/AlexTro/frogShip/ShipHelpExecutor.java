package AlexTro.frogShip;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ShipHelpExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        sender.sendMessage("");
        sender.sendMessage("§8§m      §r §6§lFROG SHIP §7v1.2 AlexTro §8§m      ");
        
        // Категория: Управление (Админ)
        sender.sendMessage("§e§l▶ Управление кораблем [для админов!]");
        sender.sendMessage(" §7/ship_spawnship <x> <y> <z> §8- §fПризвать корабль");
        sender.sendMessage(" §7/ship_rmship §8- §fУдалить корабль [осторожно!]");
        sender.sendMessage(" §7/ship_spawnfrogs §8- §fЗаполнить матросами");
        sender.sendMessage(" §7/ship_spawnjason §8- §fПризвать Певца-лягушку");
        sender.sendMessage(" §7/embark <player> §8- §fПосадить игрока (для КБ)");
        
        sender.sendMessage("");
        
        // Категория: Для игроков
        sender.sendMessage("§a§l▶ Игровые команды");
        sender.sendMessage(" §7/sitonship §8- §fНа палубу! (сесть)");
        sender.sendMessage(" §7/sitoff §8- §fПокинуть судно");
        sender.sendMessage(" §7/shipfood §8- §fПолучить немного еды");
        sender.sendMessage(" §7/ship_speedset 1 (от 0 до 5) §8- §fНастроить скорость");
        
        // Категория: Музыка
        sender.sendMessage("§b§l▶ Аудио-система");
        sender.sendMessage(" §7/audioship §8- §fВключить эфир");
        sender.sendMessage(" §7/stopaudioship §8- §fВыключить");
        sender.sendMessage(" §7/shipmotor_on (_off) §8- §fЗвук мотора");

        
        sender.sendMessage("§8§m                            ");
        
        return true;
    }
}
