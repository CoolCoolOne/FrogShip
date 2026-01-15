package AlexTro.frogShip;

import org.bukkit.Location;

public class ShipPathCalculator {

    private double routeTime = 0;
    private double waveTime = 0;

    /**
     * Вычисляет горизонтальное смещение (маршрут)
     */
    public double[] getNextHorizontalOffset() {
        // Здесь будет твой сложный маршрут (X и Z)
        double x = Math.sin(routeTime) * 5.0;
        double z = 0;
        
        routeTime += 0.04; // Скорость движения по маршруту
        return new double[]{x, z};
    }

    /**
     * Вычисляет вертикальное смещение (качка)
     */
    public double getNextWaveOffset() {
        double y = Math.sin(waveTime) * 0.125; // 1/8 блока
        
        waveTime += 0.02; // Скорость качания
        return y;
    }
}
