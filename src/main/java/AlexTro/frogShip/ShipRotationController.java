package AlexTro.frogShip;

public class ShipRotationController {

    private float smoothYaw;
    private final float rotationSpeed;
    private final float yawOffset;

    public ShipRotationController(float initialYaw, float rotationSpeed, float yawOffset) {
        this.smoothYaw = initialYaw;
        this.rotationSpeed = rotationSpeed;
        this.yawOffset = yawOffset;
    }

    public float updateAndGetYaw(double xOffset, double zOffset) {
        // Если корабль почти не двигается, не меняем угол (чтобы не было дерганий)
        if (Math.abs(xOffset) < 0.001 && Math.abs(zOffset) < 0.001) {
            return smoothYaw;
        }

        // Вычисляем целевой угол
        float targetYaw = (float) Math.toDegrees(Math.atan2(-xOffset, zOffset)) + yawOffset;

        // Нормализация разницы углов
        float diff = targetYaw - smoothYaw;
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;

        // Плавный поворот
        smoothYaw += diff * rotationSpeed;
        return smoothYaw;
    }
}
