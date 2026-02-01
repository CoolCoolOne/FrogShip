package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import java.util.List;

public class ShipPathCalculator {

    private final List<Vector> points;
    private int currentPointIndex = 0;
    private double waveTime = 0;
    
    private final double arrivalRadiusSq;


    // Конструктор теперь принимает параметры из конфига через ShipMoveTask
    public ShipPathCalculator(List<Vector> points, double arrivalRadius) {
        this.points = points;
        this.arrivalRadiusSq = arrivalRadius * arrivalRadius;
    }

    public double[] getNextHorizontalOffset(Location currentLoc) {
        if (points.isEmpty()) return new double[]{0, 0};

        Vector target = points.get(currentPointIndex);
        Vector direction = new Vector(target.getX(), 0, target.getZ())
                .subtract(new Vector(currentLoc.getX(), 0, currentLoc.getZ()));

        double distSq = direction.lengthSquared();

        // 1. Используем arrivalRadiusSq из конфига вместо 0.25
        if (distSq < arrivalRadiusSq) {
            currentPointIndex = (currentPointIndex + 1) % points.size();
        }

        if (distSq < 0.0001) return new double[]{0, 0};

        double baseSpeed = Settings.shipSpeed * Settings.speedMultiplier;
        // 2. Логика замедления перед точкой (чтобы не было резких рывков)
        double currentSpeed = baseSpeed;
        double dist = Math.sqrt(distSq);
        if (dist < 1.5) { // Если до точки меньше 1.5 блоков
            currentSpeed = baseSpeed * (dist / 1.5 + 0.2); // Плавное снижение
        }

        // 3. Используем динамическую скорость вместо 0.1
        Vector move = direction.normalize().multiply(currentSpeed);
        return new double[]{move.getX(), move.getZ()};
    }

    public double getNextWaveOffset() {
        if (points.isEmpty()) return 0;
        waveTime += 0.1;
        // Качка + целевая высота точки (чтобы корабль мог подниматься/опускаться)
        return points.get(currentPointIndex).getY() + (Math.sin(waveTime) * 0.125);
    }
}
