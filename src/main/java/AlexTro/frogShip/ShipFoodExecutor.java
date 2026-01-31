package AlexTro.frogShip;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Random;

public class ShipFoodExecutor implements CommandExecutor {

    private final FrogShip plugin;
    private final Random random = new Random();

    // Список доступной еды
    private final Material[] shipFood = {
            Material.COOKED_COD, Material.COOKED_SALMON,
            Material.BREAD, Material.COOKED_BEEF,
            Material.APPLE, Material.COOKIE, Material.GLOW_BERRIES,
            Material.BAKED_POTATO, Material.PUMPKIN_PIE
    };

    public ShipFoodExecutor(FrogShip plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут брать еду.");
            return true;
        }

        // Проверка: заспавнен ли корабль (используем твой геттер)
        if (plugin.getActiveMoveTask() == null) {
            player.sendMessage("§c[⚓] Корабль сейчас не в плавании. Кухня закрыта.");
            return true;
        }

        // Выбираем еду и количество
        Material food = shipFood[random.nextInt(shipFood.length)];
        int amount = random.nextInt(3) + 1; // От 1 до 3 штук

        // Выдаем игроку
        player.getInventory().addItem(new ItemStack(food, amount));

        player.sendMessage("§6[⚓] Корабельный кок выдал вам: §f" +
                food.name().replace("_", " ").toLowerCase() + " x" + amount);

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);

        return true;
    }
}
