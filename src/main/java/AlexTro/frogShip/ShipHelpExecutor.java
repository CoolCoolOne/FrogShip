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
        net.md_5.bungee.api.chat.TextComponent link = new net.md_5.bungee.api.chat.TextComponent(" §6§n▶ Авторы, музыка, посмотреть (клик) ◀");
        link.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, "https://aleksey199.temp.swtest.ru/credits.html"));
        link.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.ComponentBuilder("§7Нажмите, чтобы открыть сайт").create()));

// Отправляем сообщение игроку (нужно привести sender к Player или использовать spigot().sendMessage)
        if (sender instanceof org.bukkit.entity.Player) {
            ((org.bukkit.entity.Player) sender).spigot().sendMessage(link);
        } else {
            sender.sendMessage("§6Ссылка: https://aleksey199.temp.swtest.ru/credits.html");
        }

        
        sender.sendMessage("§8§m                            ");
        
        return true;
    }
}
