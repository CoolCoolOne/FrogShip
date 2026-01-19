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
        Vector offset = new Vector(ox, oy, oz);
        offset.rotateAroundY(Math.toRadians(-shipLoc.getYaw()));
        Location seatLoc = shipLoc.clone().add(offset);
        NamespacedKey yawKey = new NamespacedKey(plugin, "seat_yaw");
        Float savedYaw = as.getPersistentDataContainer().get(yawKey, PersistentDataType.FLOAT);
        seatLoc.setYaw(shipLoc.getYaw() + (savedYaw != null ? savedYaw : 0));
        as.teleport(seatLoc);
        ShipEffectHandler.playSeatEffects(as, plugin);
    }

    public static void updateVisualParts(FrogShip plugin, BlockDisplay root, float wheelAngle) {
        NamespacedKey typeKey = new NamespacedKey(plugin, "wheel_type");
        NamespacedKey offXKey = new NamespacedKey(plugin, "offset_x");
        NamespacedKey offYKey = new NamespacedKey(plugin, "offset_y");
        NamespacedKey offZKey = new NamespacedKey(plugin, "offset_z");

        // Координаты оси колеса (настрой под свой схематик)
        float AXIS_X = -11.0f;
        float AXIS_Y = -1.0f;

        for (Entity p : root.getPassengers()) {
            if (!(p instanceof BlockDisplay bd)) continue;

            float ox = bd.getPersistentDataContainer().getOrDefault(offXKey, PersistentDataType.FLOAT, 0f);
            float oy = bd.getPersistentDataContainer().getOrDefault(offYKey, PersistentDataType.FLOAT, 0f);
            float oz = bd.getPersistentDataContainer().getOrDefault(offZKey, PersistentDataType.FLOAT, 0f);

            Transformation t = bd.getTransformation();
            String type = bd.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);

            if ("blade".equals(type)) {
                // Орбитальное вращение
                float relX = ox - AXIS_X;
                float relY = oy - AXIS_Y;
                float cos = (float) Math.cos(wheelAngle);
                float sin = (float) Math.sin(wheelAngle);

                t.getTranslation().set((relX * cos - relY * sin) + AXIS_X, (relX * sin + relY * cos) + AXIS_Y, oz);
                t.getLeftRotation().setAngleAxis(wheelAngle, 0, 0, 1);
            } else {
                // Корпус просто стоит на оффсете
                t.getTranslation().set(ox, oy, oz);
                t.getLeftRotation().set(0, 0, 0, 1);
            }

            // Масштаб всегда 1
            t.getScale().set(1, 1, 1);

            bd.setTransformation(t);
            bd.setInterpolationDuration(1);
            bd.setInterpolationDelay(0);

            ShipEffectHandler.playEffects(bd, plugin);
        }
    }
}
