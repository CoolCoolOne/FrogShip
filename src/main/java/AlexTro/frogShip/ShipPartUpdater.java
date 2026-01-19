package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

public class ShipPartUpdater {

    public static void updateSeats(FrogShip plugin, Location nextLoc, NamespacedKey oxKey, NamespacedKey oyKey, NamespacedKey ozKey) {
        nextLoc.getWorld().getEntitiesByClass(ArmorStand.class).stream()
                .filter(as -> as.getScoreboardTags().contains("ship_seat"))
                .forEach(as -> {
                    Double ox = as.getPersistentDataContainer().get(oxKey, PersistentDataType.DOUBLE);
                    Double oy = as.getPersistentDataContainer().get(oyKey, PersistentDataType.DOUBLE);
                    Double oz = as.getPersistentDataContainer().get(ozKey, PersistentDataType.DOUBLE);

                    if (ox != null && oy != null && oz != null) {
                        syncSeatRotation(as, nextLoc, ox, oy, oz, plugin);
                    }
                });
    }

    private static void syncSeatRotation(ArmorStand as, Location shipLoc, double ox, double oy, double oz, FrogShip plugin) {
        // ФИКС БОКОВОГО ПЛАВАНИЯ ДЛЯ СИДЕНИЙ
        Vector offset = new Vector(ox, oy, oz);
        // Вращаем вектор смещения вокруг оси Y на угол корабля
        offset.rotateAroundY(Math.toRadians(-shipLoc.getYaw()));

        Location seatLoc = shipLoc.clone().add(offset);

        NamespacedKey yawKey = new NamespacedKey(plugin, "seat_yaw");
        Float savedYaw = as.getPersistentDataContainer().get(yawKey, PersistentDataType.FLOAT);

        seatLoc.setYaw(shipLoc.getYaw() + (savedYaw != null ? savedYaw : 0));
        as.teleport(seatLoc);

        ShipEffectHandler.playSeatEffects(as, plugin);
    }

    public static void updateVisualParts(FrogShip plugin, BlockDisplay root, float wheelAngle) {
        NamespacedKey growingKey = new NamespacedKey(plugin, "is_growing");
        NamespacedKey typeKey = new NamespacedKey(plugin, "wheel_type");
        NamespacedKey offXKey = new NamespacedKey(plugin, "offset_x");
        NamespacedKey offYKey = new NamespacedKey(plugin, "offset_y");
        NamespacedKey offZKey = new NamespacedKey(plugin, "offset_z");

        // Подготовка данных для вращения корпуса
        double yawRad = Math.toRadians(-root.getLocation().getYaw());
        float cos = (float) Math.cos(yawRad);
        float sin = (float) Math.sin(yawRad);

        for (Entity p : root.getPassengers()) {
            if (!(p instanceof BlockDisplay bd)) continue;

            // ФИКС МЕРЦАНИЯ: Если блок еще растет, пропускаем его обновление в этом тике
            if (bd.getPersistentDataContainer().has(growingKey, PersistentDataType.BYTE)) {
                continue;
            }

            ShipEffectHandler.playEffects(bd, plugin);

            // Получаем оригинальные смещения из схематика
            float ox = bd.getPersistentDataContainer().getOrDefault(offXKey, PersistentDataType.FLOAT, 0f);
            float oy = bd.getPersistentDataContainer().getOrDefault(offYKey, PersistentDataType.FLOAT, 0f);
            float oz = bd.getPersistentDataContainer().getOrDefault(offZKey, PersistentDataType.FLOAT, 0f);

            Transformation t = bd.getTransformation();

            // ФИКС БОКОВОГО ПЛАВАНИЯ ДЛЯ БЛОКОВ
            // Пересчитываем локальный Translation с учетом поворота корабля
            float rotatedX = ox * cos - oz * sin;
            float rotatedZ = ox * sin + oz * cos;
            t.getTranslation().set(rotatedX, oy, rotatedZ);

            bd.setInterpolationDuration(1);
            bd.setInterpolationDelay(0);

            String type = bd.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
            if ("blade".equals(type)) {
                // В ShipWheel передаем уже измененную трансформацию 't'
                ShipWheel.update(bd, wheelAngle, offXKey, offYKey, offZKey);
            } else {
                // Если не колесо, просто применяем повернутую трансформацию
                bd.setTransformation(t);
            }
        }
    }
}
