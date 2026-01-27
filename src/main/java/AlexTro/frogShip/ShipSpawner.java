package AlexTro.frogShip;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.ArmorStand;


public class ShipSpawner {

public static void spawn(FrogShip plugin, Location startLoc) {
    // 1. Загружаем маршрут
    List<Vector> route = new ArrayList<>();
    List<Map<?, ?>> configRoute = plugin.getConfig().getMapList("ship-route");
    for (Map<?, ?> map : configRoute) {
        route.add(new Vector(((Number) map.get("x")).doubleValue(), 
                             ((Number) map.get("y")).doubleValue(), 
                             ((Number) map.get("z")).doubleValue()));
    }
    if (route.isEmpty()) route.add(startLoc.toVector());

    // 2. Создаем корень корабля
    BlockDisplay root = (BlockDisplay) startLoc.getWorld().spawnEntity(startLoc, EntityType.BLOCK_DISPLAY);
    root.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);
    plugin.getActiveShips().add(root);

    // 3. Загружаем блоки и СОБИРАЕМ СИДЕНЬЯ
    List<ArmorStand> shipSeats = loadSchematicBlocks(plugin, root, startLoc);

    int delaySeconds = plugin.getConfig().getInt("ship-start-delay", 30);
    Bukkit.broadcastMessage(String.format("§6[FrogShip] §eКорабль прибыл! Отправление через %d сек.", delaySeconds));

    // 5. Запускаем таймер ожидания
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        if (root.isValid() && !root.isDead()) {
            Bukkit.broadcastMessage("§6[FrogShip] §aВсе по местам! Поплыли.");

            // ПЕРЕДАЕМ список сидений в MoveTask
            new ShipMoveTask(plugin, root, route, shipSeats).runTaskTimer(plugin, 0L, 1L);
        }
    }, 20L * delaySeconds);
}


    // Убедись, что эти импорты есть в начале файла ShipSpawner.jav

    // Полный обновленный метод:
    private static List<ArmorStand> loadSchematicBlocks(FrogShip plugin, BlockDisplay root, Location startLoc) {
        List<ArmorStand> seats = new ArrayList<>(); // Список для сбора всех сидений корабля
        File file = new File(plugin.getDataFolder(), "shipbig2.schem");
        if (!file.exists()) return seats;

        try (ClipboardReader reader = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();
            BlockVector3 min = clipboard.getMinimumPoint();
            BlockVector3 max = clipboard.getMaximumPoint();
            BlockVector3 origin = clipboard.getOrigin();

            for (int x = min.x(); x <= max.x(); x++) {
                for (int y = min.y(); y <= max.y(); y++) {
                    for (int z = min.z(); z <= max.z(); z++) {
                        BlockVector3 pos = BlockVector3.at(x, y, z);
                        var block = clipboard.getFullBlock(pos);

                        if (block.getBlockType().getMaterial().isAir()) continue;

                        float offX = (float) (x - origin.x());
                        float offY = (float) (y - origin.y());
                        float offZ = (float) (z - origin.z());

                        // Вызываем обновленный процесс и получаем стойку, если она создалась
                        ArmorStand seat = ShipBlockProcessor.process(plugin, root, startLoc, block.getAsString(), offX, offY, offZ);

                        if (seat != null) {
                            seats.add(seat); // Сохраняем лягушачье место в наш список
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return seats; // Возвращаем наполненный список сидений
    }

}
