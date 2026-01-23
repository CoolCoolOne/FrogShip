package AlexTro.frogShip;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;

public class ShipBlockProcessor {

    public static void process(FrogShip plugin, BlockDisplay root, Location startLoc, String blockDataStr, float offX,
            float offY, float offZ) {
        BlockData data;
        try {
            data = Bukkit.createBlockData(blockDataStr);
        } catch (IllegalArgumentException e) {
            data = Bukkit.createBlockData(Material.STONE);
        }

        Material mat = data.getMaterial();

        if (mat == Material.LIGHT) {
            if (data instanceof org.bukkit.block.data.type.Light lightData && lightData.getLevel() == 1) {
                // Спавним сиденье, используя ту же логику, что и для плит
                spawnSeat(plugin, startLoc, offX, offY, offZ, mat);
            }
        }

        // 1. ЛОГИКА СИДЕНИЙ
        if (mat == Material.MANGROVE_SLAB || mat == Material.BAMBOO_SLAB) {
            spawnSeat(plugin, startLoc, offX, offY, offZ, mat);
        }

        // 2. СОЗДАНИЕ ВИЗУАЛЬНОЙ ЧАСТИ
        BlockDisplay part = (BlockDisplay) startLoc.getWorld().spawnEntity(startLoc, EntityType.BLOCK_DISPLAY);
        part.setBlock(data);
        part.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);

        animateAppearance(plugin, part, offX, offY, offZ);

        // Настройка позиции
        Transformation t = part.getTransformation();
        t.getTranslation().set(offX, offY, offZ);
        part.setTransformation(t);

        // Сохранение метаданных
        saveMetadata(plugin, part, offX, offY, offZ, mat, data);

        root.addPassenger(part);
    }

    private static void spawnSeat(FrogShip plugin, Location loc, float offX, float offY, float offZ, Material mat) {
        double addY;
        double centerX;
        double centerZ;
        float finalYaw;

        // 1. Определяем параметры в зависимости от типа сиденья
        if (mat == Material.LIGHT) {
            centerX = plugin.getConfig().getDouble("dj-seat.offset-x", 0.5);
            addY = plugin.getConfig().getDouble("dj-seat.offset-y", 1.0);
            centerZ = plugin.getConfig().getDouble("dj-seat.offset-z", 0.5);
            finalYaw = (float) plugin.getConfig().getDouble("dj-seat.yaw", 0.0);
        } else {
            addY = plugin.getConfig().getDouble("ship.seat-offset-y", 1.5);
            centerX = plugin.getConfig().getDouble("ship.seat-center-xz", 0.5);
            centerZ = plugin.getConfig().getDouble("ship.seat-center-xz", 0.5);
            finalYaw = (float) (Math.random() * 360.0); // Рандом для обычных лягушек
        }

        // 2. Создаем локацию и устанавливаем Yaw
        Location seatLoc = loc.clone().add(offX + centerX, offY + addY, offZ + centerZ);
        seatLoc.setYaw(finalYaw);

        // 3. Спавним ОДИН ArmorStand
        ArmorStand seat = (ArmorStand) loc.getWorld().spawnEntity(seatLoc, EntityType.ARMOR_STAND);

        seat.setVisible(false);
        seat.setGravity(false);
        seat.setSmall(true);
        seat.setMarker(true);

        // 4. Добавляем теги
        seat.addScoreboardTag("ship_seat");
        if (mat == Material.MANGROVE_SLAB) {
            seat.addScoreboardTag("ship_seat_player");
        } else if (mat == Material.BAMBOO_SLAB) {
            seat.addScoreboardTag("ship_seat_mob");
        } else if (mat == Material.LIGHT) {
            seat.addScoreboardTag("ship_seat_dj");
        }

        // 5. Сохраняем данные в PDC (для движения и поворота корабля)
        NamespacedKey ox = new NamespacedKey(plugin, "seat_off_x");
        NamespacedKey oy = new NamespacedKey(plugin, "seat_off_y");
        NamespacedKey oz = new NamespacedKey(plugin, "seat_off_z");
        NamespacedKey yawKey = new NamespacedKey(plugin, "seat_yaw");

        seat.getPersistentDataContainer().set(ox, PersistentDataType.DOUBLE, offX + centerX);
        seat.getPersistentDataContainer().set(oy, PersistentDataType.DOUBLE, offY + addY);
        seat.getPersistentDataContainer().set(oz, PersistentDataType.DOUBLE, offZ + centerZ);
        seat.getPersistentDataContainer().set(yawKey, PersistentDataType.FLOAT, finalYaw);
    }

    private static void saveMetadata(FrogShip plugin, BlockDisplay part, float offX, float offY, float offZ,
            Material mat, BlockData data) {
        part.getPersistentDataContainer().set(new NamespacedKey(plugin, "offset_x"), PersistentDataType.FLOAT, offX);
        part.getPersistentDataContainer().set(new NamespacedKey(plugin, "offset_y"), PersistentDataType.FLOAT, offY);
        part.getPersistentDataContainer().set(new NamespacedKey(plugin, "offset_z"), PersistentDataType.FLOAT, offZ);

        if (mat == Material.SMOOTH_RED_SANDSTONE || mat == Material.RED_CONCRETE || mat == Material.IRON_BLOCK) {
            part.getPersistentDataContainer().set(new NamespacedKey(plugin, "wheel_type"), PersistentDataType.STRING,
                    "blade");
        }

        if (data.getLightEmission() > 0) {
            part.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));
        }
    }

    private static void animateAppearance(FrogShip plugin, BlockDisplay part, float offX, float offY, float offZ) {
        Transformation t = part.getTransformation();
        // Сразу ставим финальные значения
        t.getTranslation().set(offX, offY, offZ);
        t.getScale().set(1, 1, 1);
        part.setTransformation(t);

        // Убираем все задержки (runTaskLater) и Duration
        part.setInterpolationDuration(0);
    }

}
