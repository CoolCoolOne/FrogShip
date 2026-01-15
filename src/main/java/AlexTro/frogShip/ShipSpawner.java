package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;

public class ShipSpawner {

    public static void spawn(FrogShip plugin, Location startLoc) {
        // Создаем корень
        BlockDisplay root = createPart(plugin, startLoc, 0, 0);
        plugin.getActiveShips().add(root);

        // Создаем платформу 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                root.addPassenger(createPart(plugin, startLoc, x, z));
            }
        }

        // Создаем сиденье
        ArmorStand seat = startLoc.getWorld().spawn(startLoc.clone().add(0, -0.5, 0), ArmorStand.class, s -> {
            s.setVisible(false);
            s.setGravity(false);
            s.setSmall(true);
            s.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);
        });
        plugin.setSeat(seat);

        // ЗАПУСКАЕМ ДВИЖЕНИЕ (выносим в отдельный класс)
        new ShipMoveTask(plugin, root, startLoc).runTaskTimer(plugin, 0L, 2L);
    }

    private static BlockDisplay createPart(FrogShip plugin, Location loc, float ox, float oz) {
        BlockDisplay bd = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
        bd.setBlock(Material.OAK_PLANKS.createBlockData());
        bd.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);
        Transformation t = bd.getTransformation();
        t.getTranslation().set(ox - 0.5f, -0.5f, oz - 0.5f);
        bd.setTransformation(t);
        return bd;
    }
}
