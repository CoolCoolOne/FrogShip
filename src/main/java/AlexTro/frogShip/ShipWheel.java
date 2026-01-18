package AlexTro.frogShip;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;

public class ShipWheel {

    private static final float AXIS_X = -11.0f;
    private static final float AXIS_Y = -1.0f;
    private static final float AXIS_Z_BASE = 7.0f;

    public static void update(BlockDisplay bd, float wheelAngle, NamespacedKey offXKey, NamespacedKey offYKey, NamespacedKey offZKey) {
        float ox = bd.getPersistentDataContainer().getOrDefault(offXKey, PersistentDataType.FLOAT, 0f);
        float oy = bd.getPersistentDataContainer().getOrDefault(offYKey, PersistentDataType.FLOAT, 0f);
        float oz = bd.getPersistentDataContainer().getOrDefault(offZKey, PersistentDataType.FLOAT, 0f);

        // 1. Считаем радиус-вектор от оси до УГЛА блока
        float relX = ox - AXIS_X;
        float relY = oy - AXIS_Y;

        float cos = (float) Math.cos(wheelAngle);
        float sin = (float) Math.sin(wheelAngle);

        // 2. Вращаем координаты (орбита)
        float rotatedX = relX * cos - relY * sin;
        float rotatedY = relX * sin + relY * cos;

        Transformation t = bd.getTransformation();

        // 3. Устанавливаем позицию угла на орбите
        t.getTranslation().set(rotatedX + AXIS_X, rotatedY + AXIS_Y, oz);

        // 4. Визуальный поворот самого блока вокруг оси Z
        t.getLeftRotation().setAngleAxis(wheelAngle, 0, 0, 1);

        bd.setTransformation(t);
    }
}
