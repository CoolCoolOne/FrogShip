package AlexTro.frogShip;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.List;
import org.bukkit.persistence.PersistentDataType;

public class ShipMoveTask extends BukkitRunnable {

    private final FrogShip plugin;
    private final BlockDisplay root;
    private final ShipPathCalculator pathCalculator;
    private final List<ArmorStand> seats; // НОВОЕ: список конкретно наших сидений
    private final double maxDistanceSq = Math.pow(64, 2);
    private float wheelAngle = 0f;
    private float smoothYaw = -1f;

    private final NamespacedKey oxKey, oyKey, ozKey;

    // ОБНОВЛЕННЫЙ КОНСТРУКТОР: теперь принимает 4 аргумента
    public ShipMoveTask(FrogShip plugin, BlockDisplay root, List<Vector> points, List<ArmorStand> seats) {
        this.plugin = plugin;
        this.root = root;
        this.pathCalculator = new ShipPathCalculator(points);
        this.seats = seats; // Сохраняем список сидений
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
        float diff = targetYaw - smoothYaw;
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;
        smoothYaw += diff * rotSpeed;

        Location nextLoc = new Location(
                currentLoc.getWorld(),
                currentLoc.getX() + xz[0],
                y,
                currentLoc.getZ() + xz[1],
                smoothYaw,
                0
        );

        // --- БОНУС СОВЕТ: ПОРЯДОК ИМЕЕТ ЗНАЧЕНИЕ ---

        // 1. Сначала рассчитываем позиции сидений для следующего тика
        updateSeatsDirectly(nextLoc);

        // 2. Затем телепортируем сам корабль
        root.setInterpolationDelay(0);
        root.setInterpolationDuration(1);
        root.teleport(nextLoc);

        // 3. Обновляем анимацию колес и эффекты
        wheelAngle -= 0.05f;
        ShipPartUpdater.updateVisualParts(plugin, root, wheelAngle);
    }

    // НОВЫЙ МЕТОД: двигаем только свои сиденья без поиска по миру
    private void updateSeatsDirectly(Location nextLoc) {
        for (ArmorStand as : seats) {
            if (as == null || !as.isValid()) continue;

            Double ox = as.getPersistentDataContainer().get(oxKey, PersistentDataType.DOUBLE);
            Double oy = as.getPersistentDataContainer().get(oyKey, PersistentDataType.DOUBLE);
            Double oz = as.getPersistentDataContainer().get(ozKey, PersistentDataType.DOUBLE);

            if (ox != null && oy != null && oz != null) {
                // Вызываем старую добрую логику поворота из ShipPartUpdater,
                // но передаем конкретную стойку
                ShipPartUpdater.syncSeatRotation(as, nextLoc, ox, oy, oz, plugin);
            }
        }
    }

    private boolean shouldRemove() {
        if (root.isDead() || !root.isValid()) return true;
        return root.getWorld().getPlayers().stream()
                .noneMatch(p -> p.getLocation().distanceSquared(root.getLocation()) <= maxDistanceSq);
    }

    private void stop() {
        Bukkit.broadcastMessage("§6[FrogShip] §eКорабль завершил маршрут.");
        plugin.removeAllShips();
        this.cancel();
    }

    private float calculateTargetYaw(double xOffset, double zOffset) {
        float movementYaw = (float) Math.toDegrees(Math.atan2(-xOffset, zOffset));
        float configOffset = (float) plugin.getConfig().getDouble("ship-yaw-offset", 0.0);
        return movementYaw + configOffset;
    }
}
