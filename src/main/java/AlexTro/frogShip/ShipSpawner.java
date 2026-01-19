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

public class ShipSpawner {

    public static void spawn(FrogShip plugin, Location startLoc) {
        List<Vector> route = new ArrayList<>();
        List<Map<?, ?>> configRoute = plugin.getConfig().getMapList("ship-route");
        for (Map<?, ?> map : configRoute) {
            route.add(new Vector(((Number) map.get("x")).doubleValue(), ((Number) map.get("y")).doubleValue(), ((Number) map.get("z")).doubleValue()));
        }
        if (route.isEmpty()) route.add(startLoc.toVector());

        BlockDisplay root = (BlockDisplay) startLoc.getWorld().spawnEntity(startLoc, EntityType.BLOCK_DISPLAY);
        root.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);
        plugin.getActiveShips().add(root);

        loadSchematicBlocks(plugin, root, startLoc);

        new ShipMoveTask(plugin, root, route).runTaskTimer(plugin, 0L, 1L);
    }

    private static void loadSchematicBlocks(FrogShip plugin, BlockDisplay root, Location startLoc) {
        File file = new File(plugin.getDataFolder(), "shipbig2.schem");
        if (!file.exists()) return;

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

                        // Вся магия обработки теперь здесь:
                        ShipBlockProcessor.process(plugin, root, startLoc, block.getAsString(), offX, offY, offZ);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
