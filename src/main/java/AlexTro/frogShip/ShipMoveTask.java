package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import java.util.List;

public class ShipMoveTask extends BukkitRunnable {

    private final FrogShip plugin;
    private final BlockDisplay root;
    private final ShipPathCalculator pathCalculator;
    private final double maxDistanceSq = Math.pow(64, 2);

    private float wheelAngle = 0f;
    private final float rotationSpeed = 0.05f;

    public ShipMoveTask(FrogShip plugin, BlockDisplay root, List<Vector> points) {
        this.plugin = plugin;
        this.root = root;
        this.pathCalculator = new ShipPathCalculator(points);
    }

    @Override
    public void run() {
        if (shouldRemove()) {
            stop();
            return;
        }

        Location currentLoc = root.getLocation();
        double[] xz = pathCalculator.getNextHorizontalOffset(currentLoc);
        double y = pathCalculator.getNextWaveOffset();

        Location nextLoc = new Location(
                currentLoc.getWorld(),
                currentLoc.getX() + xz[0],
                y,
                currentLoc.getZ() + xz[1]
        );

        root.teleport(nextLoc);

        if (plugin.getSeat() != null && !plugin.getSeat().isDead()) {
            plugin.getSeat().teleport(nextLoc.clone().add(0, -0.6, 0));
        }

        // Направление вращения: уменьшаем угол для движения вперед (нос на +X)
        wheelAngle -= rotationSpeed;
        updateShipParts();
    }

    private void updateShipParts() {
        NamespacedKey typeKey = new NamespacedKey(plugin, "wheel_type");
        NamespacedKey offXKey = new NamespacedKey(plugin, "offset_x");
        NamespacedKey offYKey = new NamespacedKey(plugin, "offset_y");
        NamespacedKey offZKey = new NamespacedKey(plugin, "offset_z");

        for (Entity p : root.getPassengers()) {
            if (!(p instanceof BlockDisplay bd)) continue;

            bd.setInterpolationDuration(1);
            bd.setInterpolationDelay(0);

            String type = bd.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);

            if ("blade".equals(type)) {
                // Вызываем логику из нового класса
                ShipWheel.update(bd, wheelAngle, offXKey, offYKey, offZKey);
            }
        }
    }

    private boolean shouldRemove() {
        if (root.isDead() || !root.isValid() || !root.getLocation().getChunk().isLoaded()) return true;
        return root.getWorld().getPlayers().stream()
                .noneMatch(p -> p.getLocation().distanceSquared(root.getLocation()) <= maxDistanceSq);
    }

    private void stop() {
        plugin.removeAllShips();
        this.cancel();
    }
}
