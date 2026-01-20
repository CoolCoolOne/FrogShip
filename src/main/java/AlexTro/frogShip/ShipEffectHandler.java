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

        NamespacedKey offXKey = new NamespacedKey(plugin, "offset_x");
        NamespacedKey offYKey = new NamespacedKey(plugin, "offset_y");
        NamespacedKey offZKey = new NamespacedKey(plugin, "offset_z");

        Float offX = part.getPersistentDataContainer().get(offXKey, PersistentDataType.FLOAT);
        Float offY = part.getPersistentDataContainer().get(offYKey, PersistentDataType.FLOAT);
        Float offZ = part.getPersistentDataContainer().get(offZKey, PersistentDataType.FLOAT);

        if (offX == null) return; 

        // 1. Получаем текущий угол поворота корня корабля
        float shipYaw = 0;
        if (part.getVehicle() instanceof BlockDisplay root) {
            shipYaw = root.getLocation().getYaw();
        }
        
        // 2. Поворачиваем вектор смещения на угол корабля
        Vector offset = new Vector(offX, offY, offZ);
        offset.rotateAroundY(Math.toRadians(-shipYaw)); 

        // 3. Итоговая позиция
        Location realLoc = part.getLocation().clone().add(offset);

        // --- ЛОГИКА ЧАСТИЦ ---

        // Брызги воды (BARRIER используется как технический блок под килем)
        if (mat == Material.BARRIER) {
            realLoc.getWorld().spawnParticle(Particle.WATER_SPLASH, realLoc, 3, 0.1, 0.1, 0.1, 0.05);
            // Добавляем немного светящихся частиц к воде
            realLoc.getWorld().spawnParticle(Particle.GLOW, realLoc, 1, 0.2, 0.2, 0.2, 0.02);
        }
        // Дым из трубы
        else if (mat == Material.POLISHED_BLACKSTONE_BRICK_WALL) {
            realLoc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, realLoc.clone().add(0, 1.2, 0), 1, 0.05, 0.1, 0.05, 0.01);
        }
        // Светящиеся частицы для фонарей или магических блоков
        else if (mat == Material.LANTERN || mat == Material.SEA_LANTERN || mat == Material.GLOWSTONE) {
            realLoc.getWorld().spawnParticle(Particle.GLOW, realLoc.clone().add(0.5, 0.5, 0.5), 1, 0.3, 0.3, 0.3, 0.02);
        }
    }

    public static void playSeatEffects(ArmorStand seat, FrogShip plugin) {
        // Проверяем, что это бамбуковое сиденье и на нем кто-то есть
        if (seat.getScoreboardTags().contains("ship_seat_mob") && !seat.getPassengers().isEmpty()) {
            Entity passenger = seat.getPassengers().get(0);

            if (passenger instanceof Frog frog) {
                // Шанс примерно раз в 10 секунд
                if (ThreadLocalRandom.current().nextDouble() < 0.005) {
                    // Звук кваканья
                    frog.getWorld().playSound(
                        frog.getLocation(), 
                        Sound.ENTITY_FROG_AMBIENT, 
                        1.0f, 
                        (float) (0.8 + Math.random() * 0.4)
                    );
                    
                    // Светящиеся частицы при кваканье
                    frog.getWorld().spawnParticle(
                        Particle.GLOW,
                        frog.getLocation().add(0, 0.5, 0),
                        5, 0.2, 0.2, 0.2, 0.05
                    );
                }
            }
        }
    }
}
