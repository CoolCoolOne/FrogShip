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
import  org.bukkit.entity.Entity;

public class ShipMoveTask extends BukkitRunnable {

    private final FrogShip plugin;
    private final BlockDisplay root;
    private final ShipPathCalculator pathCalculator;
    private final ShipRotationController rotationController; // Наш новый контроллер
    private final List<ArmorStand> seats;
    private final double maxDistanceSq = Math.pow(64, 2);
    private float wheelAngle = 0f;
    private float motorVolume;
    private float motorPitch;

    private final NamespacedKey oxKey, oyKey, ozKey;

    public ShipMoveTask(FrogShip plugin, BlockDisplay root, List<Vector> points, List<ArmorStand> seats) {
        this.plugin = plugin;
        this.root = root;
        this.seats = seats;


        // Считываем всё из секции 'ship.'
        double moveSpeed = plugin.getConfig().getDouble("ship.speed", 0.1);
        double arrivalRadius = plugin.getConfig().getDouble("ship.arrival-radius", 1.0);
        float rotSpeed = (float) plugin.getConfig().getDouble("ship.rotation-speed", 0.05f);
        float yawOffset = (float) plugin.getConfig().getDouble("ship.yaw-offset", 90.0f);

        // Инициализируем калькуляторы
        this.pathCalculator = new ShipPathCalculator(points, arrivalRadius, moveSpeed);
        this.rotationController = new ShipRotationController(root.getLocation().getYaw(), rotSpeed, yawOffset);

        // Ключи для сидений
        this.oxKey = new NamespacedKey(plugin, "seat_off_x");
        this.oyKey = new NamespacedKey(plugin, "seat_off_y");
        this.ozKey = new NamespacedKey(plugin, "seat_off_z");

        this.motorVolume = (float) plugin.getConfig().getDouble("ship.motor-volume", 0.5);
        this.motorPitch = (float) plugin.getConfig().getDouble("ship.motor-pitch", 0.5);
        this.motorEnabled = plugin.getConfig().getBoolean("ship.motor-enabled", false);

        this.motorEnabled = plugin.getConfig().getBoolean("ship.motor-enabled", false);
    }

    private int soundTicks = 0;
    private boolean motorEnabled; //

    // Добавляем метод для управления извне
    public void setMotorEnabled(boolean enabled) {
        this.motorEnabled = enabled;
    }

    @Override
    public void run() {
        if (shouldRemove()) {
            stop();
            return;
        }

        Location currentLoc = root.getLocation();

        if (motorEnabled) {
            soundTicks++;
            if (soundTicks >= 20) {
                Location engineLoc = root.getLocation().subtract(0, 3, 0);

                // Используем значения из конфига
                engineLoc.getWorld().playSound(
                        engineLoc,
                        org.bukkit.Sound.BLOCK_PISTON_EXTEND,
                        motorVolume,
                        motorPitch
                );

                soundTicks = 0;
            }
        }

        // 1. Движение
        double[] xz = pathCalculator.getNextHorizontalOffset(currentLoc);
        double y = pathCalculator.getNextWaveOffset();
        float currentYaw = rotationController.updateAndGetYaw(xz[0], xz[1]);

        Location nextLoc = new Location(
                currentLoc.getWorld(),
                currentLoc.getX() + xz[0],
                y,
                currentLoc.getZ() + xz[1],
                currentYaw,
                0);

        // 2. Сиденья
        updateSeatsDirectly(nextLoc);

        // 3. Телепортация основы
        root.setInterpolationDelay(0);
        root.setInterpolationDuration(1);
        root.teleport(nextLoc);

        // --- ВОТ ЭТОТ БЛОК НУЖНО ДОБАВИТЬ ---
        // 4. Запуск эффектов частиц для всех деталей корабля
        for (Entity passenger : root.getPassengers()) {
            if (passenger instanceof BlockDisplay bd) {
                // Эффекты дыма, пузырей и фонарей
                ShipEffectHandler.playEffects(bd, plugin);
            }
        }

        // 5. Эффекты для лягушек на сиденьях (кваканье и блеск)
        for (ArmorStand seat : seats) {
            ShipEffectHandler.playSeatEffects(seat, plugin);
        }
        // ------------------------------------

        // 6. Вращение колеса
        wheelAngle += 0.05f;
        ShipPartUpdater.updateVisualParts(plugin, root, wheelAngle);
    }


    private void updateSeatsDirectly(Location nextLoc) {
        for (ArmorStand as : seats) {
            if (as == null || !as.isValid())
                continue;
            // ИСПРАВЛЕНО: Читаем как DOUBLE
            Double ox = as.getPersistentDataContainer().get(oxKey, PersistentDataType.DOUBLE);
            Double oy = as.getPersistentDataContainer().get(oyKey, PersistentDataType.DOUBLE);
            Double oz = as.getPersistentDataContainer().get(ozKey, PersistentDataType.DOUBLE);
            if (ox != null && oy != null && oz != null) {
                ShipPartUpdater.syncSeatRotation(as, nextLoc, ox, oy, oz, plugin);
            }
        }
        for (Entity entity : root.getNearbyEntities(20, 20, 20)) {
        if (entity instanceof org.bukkit.entity.Interaction && entity.getScoreboardTags().contains("ship_firework_button")) {
            moveDynamicPart(entity, nextLoc);
        }
    }
    }

    private boolean shouldRemove() {
        if (root.isDead() || !root.isValid())
            return true;
        return root.getWorld().getPlayers().stream()
                .noneMatch(p -> p.getLocation().distanceSquared(root.getLocation()) <= maxDistanceSq);
    }

    private void stop() {
        Bukkit.broadcastMessage("§6[FrogShip] §eКорабль завершил маршрут.");
        plugin.removeAllShips(); // Осторожно: это удалит ВЕЕ корабли
        this.cancel();
    }

    private void moveDynamicPart(Entity entity, Location shipLoc) {
        Double ox = entity.getPersistentDataContainer().get(oxKey, PersistentDataType.DOUBLE);
        Double oy = entity.getPersistentDataContainer().get(oyKey, PersistentDataType.DOUBLE);
        Double oz = entity.getPersistentDataContainer().get(ozKey, PersistentDataType.DOUBLE);

        if (ox != null && oy != null && oz != null) {
            Vector offset = new Vector(ox, oy, oz);
            offset.rotateAroundY(Math.toRadians(-shipLoc.getYaw()));
            entity.teleport(shipLoc.clone().add(offset));
        }
    }
}
