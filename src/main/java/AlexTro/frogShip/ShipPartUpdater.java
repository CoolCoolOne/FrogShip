package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

public class ShipPartUpdater {

    public static void updateSeats(FrogShip plugin, Location nextLoc, NamespacedKey oxKey, NamespacedKey oyKey, NamespacedKey ozKey) {
        NamespacedKey yawKey = new NamespacedKey(plugin, "seat_yaw");
        
        nextLoc.getWorld().getEntitiesByClass(ArmorStand.class).stream()
            .filter(as -> as.getScoreboardTags().contains("ship_seat"))
            .forEach(as -> {
                Double ox = as.getPersistentDataContainer().get(oxKey, PersistentDataType.DOUBLE);
                Double oy = as.getPersistentDataContainer().get(oyKey, PersistentDataType.DOUBLE);
                Double oz = as.getPersistentDataContainer().get(ozKey, PersistentDataType.DOUBLE);
                
                if (ox != null && oy != null && oz != null) {
                    Location seatLoc = nextLoc.clone().add(ox, oy, oz);
                    Float savedYaw = as.getPersistentDataContainer().get(yawKey, PersistentDataType.FLOAT);
                    float finalYaw = nextLoc.getYaw() + (savedYaw != null ? savedYaw : 0);
                    
                    seatLoc.setYaw(finalYaw);
                    as.teleport(seatLoc);
                    ShipEffectHandler.playSeatEffects(as, plugin);
                }
            });
    }

    public static void updateVisualParts(FrogShip plugin, BlockDisplay root, float wheelAngle) {
        NamespacedKey typeKey = new NamespacedKey(plugin, "wheel_type");
        NamespacedKey offXKey = new NamespacedKey(plugin, "offset_x");
        NamespacedKey offYKey = new NamespacedKey(plugin, "offset_y");
        NamespacedKey offZKey = new NamespacedKey(plugin, "offset_z");

        for (Entity p : root.getPassengers()) {
            if (!(p instanceof BlockDisplay bd)) continue;
            
            ShipEffectHandler.playEffects(bd, plugin);
            bd.setInterpolationDuration(1);
            bd.setInterpolationDelay(0);

            String type = bd.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
            if ("blade".equals(type)) {
                ShipWheel.update(bd, wheelAngle, offXKey, offYKey, offZKey);
            }
        }
    }
}
