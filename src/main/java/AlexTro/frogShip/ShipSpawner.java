//Зависимости: Убедитесь, что в вашем plugin.yml добавлен depend: [WorldEdit] или FastAsyncWorldEdit.

package AlexTro.frogShip;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShipSpawner {

    public static void spawn(FrogShip plugin, Location startLoc) {
        // 1. Загружаем маршрут из конфига
        List<Vector> route = new ArrayList<>();
        List<Map<?, ?>> configRoute = plugin.getConfig().getMapList("ship-route");

        for (Map<?, ?> map : configRoute) {
            double x = ((Number) map.get("x")).doubleValue();
            double y = ((Number) map.get("y")).doubleValue();
            double z = ((Number) map.get("z")).doubleValue();
            route.add(new Vector(x, y, z));
        }

        if (route.isEmpty()) {
            route.add(startLoc.toVector());
        }

        // 2. Создаем корневой объект (невидимый маркер для движения)
        // Используем малый размер, чтобы он не мешал
        BlockDisplay root = (BlockDisplay) startLoc.getWorld().spawnEntity(startLoc, EntityType.BLOCK_DISPLAY);
        root.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);
        plugin.getActiveShips().add(root);

        // 3. Загружаем схематик и создаем блоки
        loadSchematicBlocks(plugin, root, startLoc);

        // 4. Создаем сиденье для игрока
        ArmorStand seat = startLoc.getWorld().spawn(startLoc.clone().add(0, -0.5, 0), ArmorStand.class, s -> {
            s.setVisible(false);
            s.setGravity(false);
            s.setSmall(true);
            s.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);
        });
        plugin.setSeat(seat);
        root.addPassenger(seat); // Привязываем кресло к кораблю

        // 5. Запускаем движение
        new ShipMoveTask(plugin, root, route).runTaskTimer(plugin, 0L, 1L);
    }


    private static void loadSchematicBlocks(FrogShip plugin, BlockDisplay root, Location startLoc) {
        File file = new File(plugin.getDataFolder(), "shipbig2.schem");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FrogShip] Файл shipbig2.schem не найден в папке плагина!");
            return;
        }

        try (ClipboardReader reader = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();
            BlockVector3 min = clipboard.getMinimumPoint();
            BlockVector3 max = clipboard.getMaximumPoint();
            BlockVector3 origin = clipboard.getOrigin();

            // Используем .x(), .y(), .z() для получения int без предупреждений о Deprecated
            for (int x = min.x(); x <= max.x(); x++) {
                for (int y = min.y(); y <= max.y(); y++) {
                    for (int z = min.z(); z <= max.z(); z++) {

                        BlockVector3 pos = BlockVector3.at(x, y, z);
                        BaseBlock block = clipboard.getFullBlock(pos);

                        if (block.getBlockType().getMaterial().isAir()) continue;

                        // Создаем BlockData из строки блока WorldEdit
                        BlockData data;
                        try {
                            data = Bukkit.createBlockData(block.getAsString());
                        } catch (IllegalArgumentException e) {
                            // Если Bukkit не понял строку, берем дефолтный тип блока
                            data = Bukkit.createBlockData(Material.matchMaterial(block.getBlockType().getId()));
                        }

                        // Вычисляем смещение относительно точки Origin
                        float offX = (float) (x - origin.x());
                        float offY = (float) (y - origin.y());
                        float offZ = (float) (z - origin.z());

                        // Спавним BlockDisplay для части корабля
                        BlockDisplay part = (BlockDisplay) startLoc.getWorld().spawnEntity(startLoc, EntityType.BLOCK_DISPLAY);
                        part.setBlock(data);
                        part.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);

                        // Настройка трансформации (смещение)
                        Transformation t = part.getTransformation();
                        t.getTranslation().set(offX, offY, offZ);
                        part.setTransformation(t);

                        NamespacedKey typeKey = new NamespacedKey(plugin, "wheel_type");
                        NamespacedKey offXKey = new NamespacedKey(plugin, "offset_x");
                        NamespacedKey offYKey = new NamespacedKey(plugin, "offset_y");
                        NamespacedKey offZKey = new NamespacedKey(plugin, "offset_z");

                        // Сохраняем базовые координаты (необходимы для логики ShipMoveTask)
                        part.getPersistentDataContainer().set(offXKey, PersistentDataType.FLOAT, offX);
                        part.getPersistentDataContainer().set(offYKey, PersistentDataType.FLOAT, offY);
                        part.getPersistentDataContainer().set(offZKey, PersistentDataType.FLOAT, offZ);

                        // Помечаем блоки как лопасти колеса по материалам
                        Material mat = data.getMaterial();
                        if (mat == Material.SMOOTH_RED_SANDSTONE || mat == Material.RED_CONCRETE || mat == Material.IRON_BLOCK) {
                            part.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "blade");
                        }

                        // ПРОВЕРКА СВЕТА: если блок светящийся, заставляем сущность сиять
                        // 1. Проверяем, является ли это специальным блоком LIGHT (с уровнями)
                        if (data instanceof org.bukkit.block.data.type.Light light) {
                            int level = light.getLevel();
                            part.setBrightness(new org.bukkit.entity.Display.Brightness(level, level));
                        }
                        else if (data.getLightEmission() > 0) {
                            part.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));
                        }


                        // Прикрепляем часть к корневому объекту через систему пассажиров
                        root.addPassenger(part);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}