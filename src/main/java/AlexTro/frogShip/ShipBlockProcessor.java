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

    // Изменили void на ArmorStand
    public static ArmorStand process(FrogShip plugin, BlockDisplay root, Location startLoc, String blockDataStr,
            float offX, float offY, float offZ) {
        BlockData data;
        try {
            data = Bukkit.createBlockData(blockDataStr);
        } catch (IllegalArgumentException e) {
            data = Bukkit.createBlockData(Material.STONE);
        }

        Material mat = data.getMaterial();
        ArmorStand createdSeat = null; // Переменная для хранения стойки

        // 1. ЛОГИКА СИДЕНИЙ (теперь сохраняем результат в переменную)
        if (mat == Material.LIGHT) {
            if (data instanceof org.bukkit.block.data.type.Light lightData && lightData.getLevel() == 1) {
                createdSeat = spawnSeat(plugin, startLoc, offX, offY, offZ, mat);
            }
        }

        if (mat == Material.MANGROVE_SLAB || mat == Material.BAMBOO_SLAB) {
            createdSeat = spawnSeat(plugin, startLoc, offX, offY, offZ, mat);
        }

        if (mat == Material.PALE_OAK_BUTTON) {
            spawnFireworkTrigger(plugin, startLoc, offX, offY, offZ);
        }
if (mat == Material.BAMBOO_BUTTON || mat == Material.OAK_BUTTON) {
    spawnHornTrigger(plugin, startLoc, offX, offY, offZ);
}

        // 2. СОЗДАНИЕ ВИЗУАЛЬНОЙ ЧАСТИ
        BlockDisplay part = (BlockDisplay) startLoc.getWorld().spawnEntity(startLoc, EntityType.BLOCK_DISPLAY);
        part.setBlock(data);
        part.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);

        animateAppearance(plugin, part, offX, offY, offZ);

        Transformation t = part.getTransformation();
        t.getTranslation().set(offX, offY, offZ);
        part.setTransformation(t);

        saveMetadata(plugin, part, offX, offY, offZ, mat, data);
        root.addPassenger(part);

        return createdSeat; // Возвращаем стойку (или null, если это просто блок палубы)
    }

    private static ArmorStand spawnSeat(FrogShip plugin, Location loc, float offX, float offY, float offZ,
            Material mat) {
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

        return seat; // ОБЯЗАТЕЛЬНО возвращаем созданную стойку
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

    // В метод process добавь проверку:

    // Новый метод для создания хитбокса-триггера
    private static void spawnFireworkTrigger(FrogShip plugin, Location loc, float offX, float offY, float offZ) {
        // Смещаем хитбокс чуть вперед от блока, чтобы по нему было легко кликнуть
        Location triggerLoc = loc.clone().add(offX + 0.5, offY, offZ + 0.5);

        // Используем Interaction (хитбокс без модели, доступен в новых версиях)
        org.bukkit.entity.Interaction interaction = (org.bukkit.entity.Interaction) loc.getWorld()
                .spawnEntity(triggerLoc, EntityType.INTERACTION);
        interaction.setInteractionWidth(0.7f);
        interaction.setInteractionHeight(1.0f);

        interaction.setResponsive(true);
        // Помечаем тегом для удаления и поиска
        interaction.addScoreboardTag("ship_firework_button");
        interaction.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);

        // Сохраняем оффсеты, чтобы кнопка двигалась вместе с кораблем
        NamespacedKey ox = new NamespacedKey(plugin, "seat_off_x");
        NamespacedKey oy = new NamespacedKey(plugin, "seat_off_y");
        NamespacedKey oz = new NamespacedKey(plugin, "seat_off_z");
        interaction.getPersistentDataContainer().set(ox, PersistentDataType.DOUBLE, (double) offX + 0.5);
        interaction.getPersistentDataContainer().set(oy, PersistentDataType.DOUBLE, (double) offY);
        interaction.getPersistentDataContainer().set(oz, PersistentDataType.DOUBLE, (double) offZ + 0.5);
    }

    private static void spawnHornTrigger(FrogShip plugin, Location loc, float offX, float offY, float offZ) {
        // Центрируем хитбокс на блоке кнопки
        Location triggerLoc = loc.clone().add(offX + 0.5, offY, offZ + 0.5);

        // Создаем сущность Interaction (невидимый хитбокс)
        org.bukkit.entity.Interaction interaction = (org.bukkit.entity.Interaction) loc.getWorld()
                .spawnEntity(triggerLoc, EntityType.INTERACTION);

        interaction.setInteractionWidth(0.7f);
        interaction.setInteractionHeight(1.0f);
        interaction.setResponsive(true);

        // Тег для нашего слушателя звуков (из первой части кода)
        interaction.addScoreboardTag("ship_horn_button");

        // Тег плагина для удаления при очистке корабля
        interaction.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);

        // Сохраняем оффсеты для системы перемещения корабля
        NamespacedKey ox = new NamespacedKey(plugin, "seat_off_x");
        NamespacedKey oy = new NamespacedKey(plugin, "seat_off_y");
        NamespacedKey oz = new NamespacedKey(plugin, "seat_off_z");

        interaction.getPersistentDataContainer().set(ox, PersistentDataType.DOUBLE, (double) offX + 0.5);
        interaction.getPersistentDataContainer().set(oy, PersistentDataType.DOUBLE, (double) offY);
        interaction.getPersistentDataContainer().set(oz, PersistentDataType.DOUBLE, (double) offZ + 0.5);
    }


}
