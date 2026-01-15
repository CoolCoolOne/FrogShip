package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import java.util.List;

public class ShipPathCalculator {

    private final List<Vector> points;
    private int currentPointIndex = 0;
    private double waveTime = 0;

    public ShipPathCalculator(List<Vector> points) {
        this.points = points;
    }

    public double[] getNextHorizontalOffset(Location currentLoc) {
        if (points.isEmpty()) return new double[]{0, 0};

        Vector target = points.get(currentPointIndex);
        // Вектор направления к цели (только X и Z)
        Vector direction = new Vector(target.getX(), 0, target.getZ())
                .subtract(new Vector(currentLoc.getX(), 0, currentLoc.getZ()));

        // Проверка: достигли ли мы текущей точки (радиус 0.5 блока)
        if (direction.lengthSquared() < 0.25) {
            // Переход к следующей точке, после последней будет снова 0-я
            currentPointIndex = (currentPointIndex + 1) % points.size();
        }

        // Двигаем корабль с фиксированной скоростью 0.1
        Vector move = direction.normalize().multiply(0.1);
        return new double[]{move.getX(), move.getZ()};
    }

    public double getNextWaveOffset() {
        if (points.isEmpty()) return 0;

        waveTime += 0.1;
        // Качка (1/8 блока) + Y целевой точки
        return points.get(currentPointIndex).getY() + (Math.sin(waveTime) * 0.125);
    }
}
