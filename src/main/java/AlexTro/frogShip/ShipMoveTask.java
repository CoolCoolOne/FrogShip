package AlexTro.frogShip;

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

        Location nextLoc = new Location(currentLoc.getWorld(), currentLoc.getX() + xz[0], y, currentLoc.getZ() + xz[1]);
        // В 2026 году можно добавить вычисление поворота по xz, если нужно, чтобы корабль плавно поворачивал носом
        
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
        plugin.removeAllShips();
        this.cancel();
    }
}
