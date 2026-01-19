package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

public class ShipPartUpdater {

    public static void updateSeats(FrogShip plugin, Location nextLoc, NamespacedKey oxKey, NamespacedKey oyKey, NamespacedKey ozKey) {
        // Теперь этот метод только ищет сиденья в мире
        nextLoc.getWorld().getEntitiesByClass(ArmorStand.class).stream()
            .filter(as -> as.getScoreboardTags().contains("ship_seat"))
            .forEach(as -> {
                Double ox = as.getPersistentDataContainer().get(oxKey, PersistentDataType.DOUBLE);
                Double oy = as.getPersistentDataContainer().get(oyKey, PersistentDataType.DOUBLE);
                Double oz = as.getPersistentDataContainer().get(ozKey, PersistentDataType.DOUBLE);
                
                if (ox != null && oy != null && oz != null) {
                    // Вызываем новый метод для обработки конкретного стенда
                    syncSeatRotation(as, nextLoc, ox, oy, oz, plugin);
                }
            });
    }

    /**
     * Новый метод: отвечает только за математику поворота и телепортацию одного сиденья
     */
    private static void syncSeatRotation(ArmorStand as, Location shipLoc, double ox, double oy, double oz, FrogShip plugin) {
        Location seatLoc = shipLoc.clone().add(ox, oy, oz);
        
        // Получаем сохраненный при спавне случайный угол
        NamespacedKey yawKey = new NamespacedKey(plugin, "seat_yaw");
        Float savedYaw = as.getPersistentDataContainer().get(yawKey, PersistentDataType.FLOAT);
        
        // Итоговый Yaw = Поворот корабля + личный разворот сиденья
        float finalYaw = shipLoc.getYaw() + (savedYaw != null ? savedYaw : 0);
        
        seatLoc.setYaw(finalYaw);
        as.teleport(seatLoc);
        
        // Эффекты (кваканье лягушек)
        ShipEffectHandler.playSeatEffects(as, plugin);
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
