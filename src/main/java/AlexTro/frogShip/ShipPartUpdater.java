package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

public class ShipPartUpdater {

    /**
     * Синхронизация позиции и поворота сидений с корпусом корабля
     */
    public static void syncSeatRotation(ArmorStand as, Location shipLoc, double ox, double oy, double oz, FrogShip plugin) {
        Vector offset = new Vector(ox, oy, oz);
        // Вращаем офсет сиденья вокруг Y в зависимости от поворота корабля
        offset.rotateAroundY(Math.toRadians(-shipLoc.getYaw()));
        Location seatLoc = shipLoc.clone().add(offset);

        NamespacedKey yawKey = new NamespacedKey(plugin, "seat_yaw");
        Float savedYaw = as.getPersistentDataContainer().get(yawKey, PersistentDataType.FLOAT);
        float baseYaw = (savedYaw != null ? savedYaw : 0);

        // Логика для DJ-сиденья (слежение за игроком)
        if (as.getScoreboardTags().contains("ship_seat_dj")) {
            Player target = seatLoc.getWorld().getNearbyEntities(seatLoc, 10, 5, 10).stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player) e)
                    .findFirst()
                    .orElse(null);

            if (target != null) {
                Vector dir = target.getLocation().add(0, 1.5, 0).toVector().subtract(seatLoc.toVector()).normalize();
                seatLoc.setDirection(dir);
            } else {
                seatLoc.setYaw(shipLoc.getYaw() + baseYaw);
            }
        } else {
            // Обычные сиденья просто повторяют поворот корабля + свой базовый угол
            seatLoc.setYaw(shipLoc.getYaw() + baseYaw);
        }

        as.teleport(seatLoc);
        
        // Здесь можно добавить ShipEffectHandler.playSeatEffects(as, plugin); если он у тебя есть
    }

    /**
     * Обновление визуальных частей (вращение колеса)
     */
    public static void updateVisualParts(FrogShip plugin, BlockDisplay root, float wheelAngle) {
        NamespacedKey typeKey = new NamespacedKey(plugin, "wheel_type");
        NamespacedKey offXKey = new NamespacedKey(plugin, "offset_x");
        NamespacedKey offYKey = new NamespacedKey(plugin, "offset_y");
        NamespacedKey offZKey = new NamespacedKey(plugin, "offset_z");

        // Параметры оси из конфига
        float axisX = (float) plugin.getConfig().getDouble("ship.wheel.center-x", -1.0);
        float axisY = (float) plugin.getConfig().getDouble("ship.wheel.center-y", -1.0);
        float axisZ = (float) plugin.getConfig().getDouble("ship.wheel.center-z", -11.0);
        String axisType = plugin.getConfig().getString("ship.wheel.axis", "x").toLowerCase();

        for (Entity p : root.getPassengers()) {
            if (!(p instanceof BlockDisplay bd)) continue;

            String type = bd.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
            if (!"blade".equals(type)) continue;

            float ox = bd.getPersistentDataContainer().getOrDefault(offXKey, PersistentDataType.FLOAT, 0f);
            float oy = bd.getPersistentDataContainer().getOrDefault(offYKey, PersistentDataType.FLOAT, 0f);
            float oz = bd.getPersistentDataContainer().getOrDefault(offZKey, PersistentDataType.FLOAT, 0f);

            Transformation t = bd.getTransformation();
            
            if (axisType.equals("x")) {
                // Плоскость вращения YZ (ось X направлена вдоль корабля)
                float relY = oy - axisY;
                float relZ = oz - axisZ;
                float cos = (float) Math.cos(wheelAngle);
                float sin = (float) Math.sin(wheelAngle);
                
                t.getTranslation().set(ox, (relY * cos - relZ * sin) + axisY, (relY * sin + relZ * cos) + axisZ);
                t.getLeftRotation().setAngleAxis(wheelAngle, 1, 0, 0); 
            } else {
                // Плоскость вращения XY (ось Z направлена вдоль корабля - старый вариант)
                float relX = ox - axisX;
                float relY = oy - axisY;
                float cos = (float) Math.cos(wheelAngle);
                float sin = (float) Math.sin(wheelAngle);
                
                t.getTranslation().set((relX * cos - relY * sin) + axisX, (relX * sin + relY * cos) + axisY, oz);
                t.getLeftRotation().setAngleAxis(wheelAngle, 0, 0, 1);
            }

            bd.setTransformation(t);
            bd.setInterpolationDuration(1);
            bd.setInterpolationDelay(0);
        }
    }
}
