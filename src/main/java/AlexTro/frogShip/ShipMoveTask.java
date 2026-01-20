package AlexTro.frogShip;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.List;

public class ShipMoveTask extends BukkitRunnable {

    private final FrogShip plugin;
    private final BlockDisplay root;
    private final ShipPathCalculator pathCalculator;
    private final double maxDistanceSq = Math.pow(64, 2);
    private float wheelAngle = 0f;
    private float smoothYaw = -1f;

    private final NamespacedKey oxKey, oyKey, ozKey;

    public ShipMoveTask(FrogShip plugin, BlockDisplay root, List<Vector> points) {
        this.plugin = plugin;
        this.root = root;
        this.pathCalculator = new ShipPathCalculator(points);
        this.oxKey = new NamespacedKey(plugin, "seat_off_x");
        this.oyKey = new NamespacedKey(plugin, "seat_off_y");
        this.ozKey = new NamespacedKey(plugin, "seat_off_z");
    }

    @Override
    public void run() {
        if (shouldRemove()) { stop(); return; }

        Location currentLoc = root.getLocation();
        double[] xz = pathCalculator.getNextHorizontalOffset(currentLoc);
        double y = pathCalculator.getNextWaveOffset();

        float targetYaw = calculateTargetYaw(xz[0], xz[1]);
        if (smoothYaw == -1f) smoothYaw = targetYaw;

float rotSpeed = (float) plugin.getConfig().getDouble("ship.rotation-speed", 0.05f);
// Сглаживаем поворот (0.1f - скорость поворота, можно уменьшить для плавности)
        float diff = targetYaw - smoothYaw;
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;
        smoothYaw += diff * rotSpeed;

        Location nextLoc = new Location(
                currentLoc.getWorld(),
                currentLoc.getX() + xz[0],
                y,
                currentLoc.getZ() + xz[1],
                smoothYaw, // Используем только сглаженный угол
                0          // Pitch (наклон) оставляем 0

);

        
        root.teleport(nextLoc);

        // Вызываем вынесенную логику
        ShipPartUpdater.updateSeats(plugin, nextLoc, oxKey, oyKey, ozKey);
        
        wheelAngle -= 0.05f;
        ShipPartUpdater.updateVisualParts(plugin, root, wheelAngle);
    }

    private boolean shouldRemove() {
        if (root.isDead() || !root.isValid()) return true;
        return root.getWorld().getPlayers().stream()
                .noneMatch(p -> p.getLocation().distanceSquared(root.getLocation()) <= maxDistanceSq);
    }

    private void stop() {
        Bukkit.broadcastMessage("§6[FrogShip] §eКорабль уплыл слишком далеко и вернулся в док.");
        plugin.removeAllShips();
        this.cancel();
    }

    /**
 * Рассчитывает направление носа корабля на основе вектора движения и данных из конфига.
 */
private float calculateTargetYaw(double xOffset, double zOffset) {
    // Математика вычисления угла на плоскости XZ
    float movementYaw = (float) Math.toDegrees(Math.atan2(-xOffset, zOffset));
    
    // Считываем коррекцию из конфига
    float configOffset = (float) plugin.getConfig().getDouble("ship-yaw-offset", 0.0);
    
    return movementYaw + configOffset;
}

}
