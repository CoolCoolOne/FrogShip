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

        // 1. Быстрая проверка: если эффекты выключены, выходим
        if (Settings.bubbleCount <= 0 && Settings.smokeCount <= 0 && Settings.lanternCount <= 0
                && Settings.musicNoteCount <= 0)
            return;

        NamespacedKey offXKey = new NamespacedKey(plugin, "offset_x");
        NamespacedKey offYKey = new NamespacedKey(plugin, "offset_y");
        NamespacedKey offZKey = new NamespacedKey(plugin, "offset_z");

        Float fx = part.getPersistentDataContainer().get(offXKey, PersistentDataType.FLOAT);
        Float fy = part.getPersistentDataContainer().get(offYKey, PersistentDataType.FLOAT);
        Float fz = part.getPersistentDataContainer().get(offZKey, PersistentDataType.FLOAT);

        double offX, offY, offZ;

        if (fx != null) {
            offX = fx.doubleValue();
            offY = fy.doubleValue();
            offZ = fz.doubleValue();
        } else {
            // Если Float не найден, пробуем Double (на всякий случай)
            Double dx = part.getPersistentDataContainer().get(offXKey, PersistentDataType.DOUBLE);
            if (dx == null) return; // Если и тут null, значит данных нет
            offX = dx;
            offY = part.getPersistentDataContainer().get(offYKey, PersistentDataType.DOUBLE);
            offZ = part.getPersistentDataContainer().get(offZKey, PersistentDataType.DOUBLE);
        }


        float shipYaw = 0;
        if (part.getVehicle() instanceof BlockDisplay root) {
            shipYaw = root.getLocation().getYaw();
        }

        // Центрируем вектор (0.5), чтобы точка была в середине блока
        Vector offset = new Vector(offX + 0.5, offY + 0.5, offZ + 0.5);
        offset.rotateAroundY(Math.toRadians(-shipYaw));

        // Вычисляем итоговую локацию (теперь она видна всему методу)
        Location realLoc = null;
        if (part.getVehicle() instanceof BlockDisplay root) {
            realLoc = root.getLocation().clone().add(offset);
        }

        if (realLoc == null)
            return;

        // --- ЛОГИКА ЭФФЕКТОВ ---

        // Пузыри (под кораблем)
        if (mat == Material.BARRIER && Settings.bubbleCount > 0) {
            realLoc.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, realLoc, Settings.bubbleCount, 0.1, 0.1, 0.1,
                    0.02);
        }

        // Дым из труб
        else if (mat == Material.POLISHED_BLACKSTONE_BRICK_WALL && Settings.smokeCount > 0) {
            // Приподнимаем только по Y, так как X и Z уже центрированы
            Location smokeLoc = realLoc.clone().add(0, 0.7, 0);
            realLoc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, smokeLoc, Settings.smokeCount, 0.05, 0.1,
                    0.05, 0.01);
        }

        // Фонари и светящиеся блоки
        else if ((mat == Material.LANTERN || mat == Material.SEA_LANTERN || mat == Material.GLOWSTONE)
                && Settings.lanternCount > 0) {
            if (ThreadLocalRandom.current().nextDouble() < 0.01) {
                // Убрал лишние +0.5, так как они уже есть в realLoc
                realLoc.getWorld().spawnParticle(
                        Particle.GLOW,
                        realLoc,
                        Settings.lanternCount,
                        0.3, 0.3, 0.3, 0.02);
            }
        }

        // Музыка (красный ковер)
        else if ((mat == Material.RED_CARPET || mat == Material.RED_WOOL) && Settings.musicNoteCount > 0) {
            if (ThreadLocalRandom.current().nextDouble() < Settings.musicNoteChance) {
                // Поднимаем ноты над ковром
                Location noteLoc = realLoc.clone().add(0, 1.0, 0);

                realLoc.getWorld().spawnParticle(
                        Particle.NOTE,
                        noteLoc,
                        Settings.musicNoteCount,
                        0.5, 0.5, 0.5, 1.0);
            }
        }
    }

    public static void playSeatEffects(ArmorStand seat, FrogShip plugin) {
        if (Settings.frogGlowCount <= 0)
            return;

        if (seat.getScoreboardTags().contains("ship_seat_mob") && !seat.getPassengers().isEmpty()) {
            Entity passenger = seat.getPassengers().get(0);

            if (passenger instanceof Frog frog) {
                if (ThreadLocalRandom.current().nextDouble() < Settings.frogAmbienceChance) {
                    frog.getWorld().playSound(
                            frog.getLocation(),
                            Sound.ENTITY_FROG_AMBIENT,
                            0.9f,
                            (float) (0.5 + ThreadLocalRandom.current().nextDouble() * 1.0));
                    frog.getWorld().spawnParticle(
                            Particle.GLOW,
                            frog.getLocation().add(0, 0.5, 0),
                            Settings.frogGlowCount, 0.2, 0.2, 0.2, 0.05);
                }
            }
        }
    }
}
