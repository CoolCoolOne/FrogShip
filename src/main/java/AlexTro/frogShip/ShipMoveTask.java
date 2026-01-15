package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public class ShipMoveTask extends BukkitRunnable {

    private final FrogShip plugin;
    private final BlockDisplay root;
    private final Location startLoc;
    private final ShipPathCalculator pathCalculator; // Подключаем навигатор
    
    private final double maxDistanceSq = Math.pow(64, 2);

    public ShipMoveTask(FrogShip plugin, BlockDisplay root, Location startLoc) {
        this.plugin = plugin;
        this.root = root;
        this.startLoc = startLoc;
        this.pathCalculator = new ShipPathCalculator(); // Создаем экземпляр навигатора
    }

    @Override
    public void run() {
        if (shouldRemove()) {
            stop();
            return;
        }

        // 1. Берем данные из навигатора
        double[] xz = pathCalculator.getNextHorizontalOffset();
        double y = pathCalculator.getNextWaveOffset();

        // 2. Применяем их к начальной точке
        Location nextLoc = startLoc.clone().add(xz[0], y, xz[1]);

        // 3. Физическое перемещение
        root.teleport(nextLoc);
        
        if (plugin.getSeat() != null && !plugin.getSeat().isDead()) {
            plugin.getSeat().teleport(nextLoc.clone().add(0, -0.6, 0));
        }

        updateInterpolation(root);
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

    private void updateInterpolation(BlockDisplay root) {
        root.setInterpolationDuration(2);
        root.setInterpolationDelay(0);
        for (Entity p : root.getPassengers()) {
            if (p instanceof BlockDisplay bd) {
                bd.setInterpolationDuration(2);
                bd.setInterpolationDelay(0);
            }
        }
    }
}
