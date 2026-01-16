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
        File file = new File(plugin.getDataFolder(), "ship.schem");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FrogShip] Файл ship.schem не найден в папке плагина!");
            return;
        }

        try (ClipboardReader reader = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();
            BlockVector3 min = clipboard.getMinimumPoint();
            BlockVector3 max = clipboard.getMaximumPoint();
            BlockVector3 origin = clipboard.getOrigin();

            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        
                        BlockVector3 pos = BlockVector3.at(x, y, z);
                        BaseBlock block = clipboard.getFullBlock(pos);

                        if (block.getBlockType().getMaterial().isAir()) continue;

                        // Превращаем блок WorldEdit в Bukkit BlockData
                        BlockData data = Bukkit.createBlockData(block.getAsString());
                        
                        // Вычисляем смещение относительно точки Origin
                        float offX = x - origin.getX();
                        float offY = y - origin.getY();
                        float offZ = z - origin.getZ();

                        // Создаем BlockDisplay для каждой части
                        BlockDisplay part = (BlockDisplay) startLoc.getWorld().spawnEntity(startLoc, EntityType.BLOCK_DISPLAY);
                        part.setBlock(data);
                        part.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);

                        // Настройка позиции (трансформации)
                        Transformation t = part.getTransformation();
                        t.getTranslation().set(offX, offY, offZ);
                        part.setTransformation(t);

                        // ПРОВЕРКА СВЕТА: Если блок светится сам по себе, заставляем сущность светиться
                        if (data.getMaterial().getEmissionLightLevel() > 0) {
                            part.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));
                        }

                        // Привязываем к корню
                        root.addPassenger(part);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
