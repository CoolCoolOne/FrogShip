package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.List;

public class ShipMoveTask extends BukkitRunnable {

    private final FrogShip plugin;
    private final BlockDisplay root;
    private final ShipPathCalculator pathCalculator;

    private final double maxDistanceSq = Math.pow(64, 2);

    // Добавляем List<Vector> в конструктор
    public ShipMoveTask(FrogShip plugin, BlockDisplay root, List<Vector> points) {
        this.plugin = plugin;
        this.root = root;
        // Передаем точки в калькулятор
        this.pathCalculator = new ShipPathCalculator(points);
    }

    @Override
    public void run() {
        if (shouldRemove()) {
            stop();
            return;
        }

        // 1. Получаем текущую локацию корабля
        Location currentLoc = root.getLocation();

        // 2. Считаем смещение (передаем текущую локацию для расчета направления)
        double[] xz = pathCalculator.getNextHorizontalOffset(currentLoc);
        double y = pathCalculator.getNextWaveOffset();

        // 3. Создаем новую локацию
        // К текущим X и Z прибавляем шаг, а Y устанавливаем из калькулятора
        Location nextLoc = new Location(
                currentLoc.getWorld(),
                currentLoc.getX() + xz[0],
                y,
                currentLoc.getZ() + xz[1]
        );

        // 4. Физическое перемещение
        root.teleport(nextLoc);

        if (plugin.getSeat() != null && !plugin.getSeat().isDead()) {
            // Сиденье двигаем вслед за рутом
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
        root.setInterpolationDuration(1); // Для 20 тиков/сек лучше ставить 1-2
        root.setInterpolationDelay(0);
        for (Entity p : root.getPassengers()) {
            if (p instanceof BlockDisplay bd) {
                bd.setInterpolationDuration(1);
                bd.setInterpolationDelay(0);
            }
        }
    }
}
