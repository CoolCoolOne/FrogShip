package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class ShipEffectHandler {

    public static void playEffects(BlockDisplay part, FrogShip plugin) {
        Material mat = part.getBlock().getMaterial();

        // 1. Быстрая проверка: если эффекты выключены в ноль, выходим сразу
        if (Settings.bubbleCount <= 0 && Settings.smokeCount <= 0 && Settings.lanternCount <= 0) return;

        NamespacedKey offXKey = new NamespacedKey(plugin, "offset_x");
        NamespacedKey offYKey = new NamespacedKey(plugin, "offset_y");
        NamespacedKey offZKey = new NamespacedKey(plugin, "offset_z");

        Float offX = part.getPersistentDataContainer().get(offXKey, PersistentDataType.FLOAT);
        Float offY = part.getPersistentDataContainer().get(offYKey, PersistentDataType.FLOAT);
        Float offZ = part.getPersistentDataContainer().get(offZKey, PersistentDataType.FLOAT);

        if (offX == null) return; 

        float shipYaw = 0;
        if (part.getVehicle() instanceof BlockDisplay root) {
            shipYaw = root.getLocation().getYaw();
        }
        
// 1. Сначала центрируем (добавляем 0.5), чтобы точка вращения была в центре блока
Vector offset = new Vector(offX + 0.5, offY + 0.5, offZ + 0.5);

// 2. Теперь вращаем уже центрированный вектор
offset.rotateAroundY(Math.toRadians(-shipYaw)); 

// 3. Берем локацию КОРНЯ корабля (root), а не части (part), так надежнее
if (part.getVehicle() instanceof BlockDisplay root) {
    Location realLoc = root.getLocation().clone().add(offset);
    // Теперь realLoc — это ВСЕГДА идеальный центр блока, как бы корабль ни повернулся
}

        // --- ИСПОЛЬЗУЕМ КЭШИРОВАННЫЕ НАСТРОЙКИ ---

        if (mat == Material.BARRIER && Settings.bubbleCount > 0) {
            realLoc.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, realLoc, Settings.bubbleCount, 0.1, 0.1, 0.1, 0.02);
        }
        else if (mat == Material.POLISHED_BLACKSTONE_BRICK_WALL && Settings.smokeCount > 0) {
    // Добавляем 0.5 к X и Z, чтобы дым шел из центра трубы, а не из угла
    Location smokeLoc = realLoc.clone().add(0, 0.7, 0);
    realLoc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, smokeLoc, Settings.smokeCount, 0.05, 0.1, 0.05, 0.01);
}
        else if ((mat == Material.LANTERN || mat == Material.SEA_LANTERN || mat == Material.GLOWSTONE) && Settings.lanternCount > 0) {
    // Добавляем проверку шанса. 0.05 — это шанс 5% при каждом вызове.
    // Если нужно еще реже, поставьте 0.01 (1%) или 0.005
    if (ThreadLocalRandom.current().nextDouble() < 0.005) { 
        realLoc.getWorld().spawnParticle(
            Particle.GLOW, 
            realLoc.clone().add(0.5, 0.5, 0.5), 
            Settings.lanternCount, 
            0.3, 0.3, 0.3, 0.02
        );
    }
}
        // Логика для красного ковра (музыкальное выступление)
else if (mat == Material.RED_CARPET && Settings.musicNoteCount > 0) {
    // Проверка шанса, чтобы ноты не вылетали сплошным потоком
    if (ThreadLocalRandom.current().nextDouble() < Settings.musicNoteChance) {
        
        // Смещаем позицию к центру ковра и поднимаем на уровень головы (1.2 - 1.5 метра)
        Location noteLoc = realLoc.clone().add(0.5, 1.5, 0.5);
        
        // Спавним ноту
        // Параметр extra (последний) установленный в 1.0 дает случайный цвет ноты в Minecraft
        realLoc.getWorld().spawnParticle(
            Particle.NOTE, 
            noteLoc, 
            Settings.musicNoteCount, 
            0.5, 0.5, 0.5, 1.0 
        );
    }
}

    }

    public static void playSeatEffects(ArmorStand seat, FrogShip plugin) {
        if (Settings.frogGlowCount <= 0) return; // Оптимизация: не считаем логику, если частицы выключены

        if (seat.getScoreboardTags().contains("ship_seat_mob") && !seat.getPassengers().isEmpty()) {
            Entity passenger = seat.getPassengers().get(0);

            if (passenger instanceof Frog frog) {

                if (ThreadLocalRandom.current().nextDouble() < 0.0005) {
                    frog.getWorld().playSound(
                        frog.getLocation(), 
                        Sound.ENTITY_FROG_AMBIENT, 
                        1.0f, 
                        (float) (0.8 + Math.random() * 0.4)
                    );
                    
                    frog.getWorld().spawnParticle(
                        Particle.GLOW,
                        frog.getLocation().add(0, 0.5, 0),
                        Settings.frogGlowCount, 0.2, 0.2, 0.2, 0.05
                    );
                }
            }
        }
    }
}
