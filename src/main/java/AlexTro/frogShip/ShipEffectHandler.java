package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

public class ShipEffectHandler {

    public static void playEffects(BlockDisplay part, FrogShip plugin) {
        Material mat = part.getBlock().getMaterial();

        // Берем те же ключи, что ты создавал в ShipSpawner
        NamespacedKey offXKey = new NamespacedKey(plugin, "offset_x");
        NamespacedKey offYKey = new NamespacedKey(plugin, "offset_y");
        NamespacedKey offZKey = new NamespacedKey(plugin, "offset_z");

        // Плагин САМ достает сохраненные цифры из блока
        Float offX = part.getPersistentDataContainer().get(offXKey, PersistentDataType.FLOAT);
        Float offY = part.getPersistentDataContainer().get(offYKey, PersistentDataType.FLOAT);
        Float offZ = part.getPersistentDataContainer().get(offZKey, PersistentDataType.FLOAT);

        if (offX == null) return; // Если это не часть корабля

        // Вычисляем позицию: Центр корабля + смещение этого конкретного блока
        Location realLoc = part.getLocation().clone().add(offX, offY, offZ);

        if (mat == Material.BARRIER) {
            realLoc.getWorld().spawnParticle(Particle.valueOf("WATER_SPLASH"), realLoc, 3, 0.2, 0.1, 0.2, 0.05);
        }
        else if (mat == Material.POLISHED_BLACKSTONE_BRICK_WALL) {
            // Дым над трубой
            realLoc.getWorld().spawnParticle(Particle.valueOf("CAMPFIRE_COSY_SMOKE"), realLoc.add(0.5, 1.2, 0.5), 1, 0.05, 0.1, 0.05, 0.01);
        }
    }

}
