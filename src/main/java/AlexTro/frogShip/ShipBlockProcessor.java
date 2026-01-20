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

    public static void process(FrogShip plugin, BlockDisplay root, Location startLoc, String blockDataStr, float offX, float offY, float offZ) {
        BlockData data;
        try {
            data = Bukkit.createBlockData(blockDataStr);
        } catch (IllegalArgumentException e) {
            data = Bukkit.createBlockData(Material.STONE); 
        }

        Material mat = data.getMaterial();

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
        double addY = plugin.getConfig().getDouble("ship.seat-offset-y", 1.5);
        double centerX = plugin.getConfig().getDouble("ship.seat-center-xz", 0.5);
        double centerZ = plugin.getConfig().getDouble("ship.seat-center-xz", 0.5);

        // Применяем смещение к локации спавна сиденья
        // BlockDisplay спавнится от угла блока, поэтому мы добавляем 0.5 для центровки
        Location seatLoc = loc.clone().add(offX + centerX, offY + addY, offZ + centerZ);
        ArmorStand seat = (ArmorStand) loc.getWorld().spawnEntity(seatLoc, EntityType.ARMOR_STAND);
        float randomYaw = (float) (Math.random() * 360.0);
        
        seat.setVisible(false);
        seat.setGravity(false);
        seat.setSmall(true);
        seat.setMarker(true);
        
        seat.addScoreboardTag("ship_seat");
        seat.addScoreboardTag(mat == Material.MANGROVE_SLAB ? "ship_seat_player" : "ship_seat_mob");

        NamespacedKey ox = new NamespacedKey(plugin, "seat_off_x");
        NamespacedKey oy = new NamespacedKey(plugin, "seat_off_y");
        NamespacedKey oz = new NamespacedKey(plugin, "seat_off_z");

        seat.getPersistentDataContainer().set(ox, PersistentDataType.DOUBLE, (double) offX + centerX);
        seat.getPersistentDataContainer().set(oy, PersistentDataType.DOUBLE, (double) offY + addY);
        seat.getPersistentDataContainer().set(oz, PersistentDataType.DOUBLE, (double) offZ + centerZ);

        NamespacedKey yawKey = new NamespacedKey(plugin, "seat_yaw");
seat.getPersistentDataContainer().set(yawKey, PersistentDataType.FLOAT, randomYaw);
    }

    private static void saveMetadata(FrogShip plugin, BlockDisplay part, float offX, float offY, float offZ, Material mat, BlockData data) {
        part.getPersistentDataContainer().set(new NamespacedKey(plugin, "offset_x"), PersistentDataType.FLOAT, offX);
        part.getPersistentDataContainer().set(new NamespacedKey(plugin, "offset_y"), PersistentDataType.FLOAT, offY);
        part.getPersistentDataContainer().set(new NamespacedKey(plugin, "offset_z"), PersistentDataType.FLOAT, offZ);

        if (mat == Material.SMOOTH_RED_SANDSTONE || mat == Material.RED_CONCRETE || mat == Material.IRON_BLOCK) {
            part.getPersistentDataContainer().set(new NamespacedKey(plugin, "wheel_type"), PersistentDataType.STRING, "blade");
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
