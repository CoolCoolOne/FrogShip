package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShipSpawner {

    public static void spawn(FrogShip plugin, Location startLoc) {
        // 1. Загружаем маршрут из конфига
        List<Vector> route = new ArrayList<>();
        List<Map<?, ?>> configRoute = plugin.getConfig().getMapList("ship-route");

        for (Map<?, ?> map : configRoute) {
            // Используем ((Number) ...).doubleValue(), это сработает и для 300427, и для 300427.0
            double x = ((Number) map.get("x")).doubleValue();
            double y = ((Number) map.get("y")).doubleValue();
            double z = ((Number) map.get("z")).doubleValue();
            route.add(new Vector(x, y, z));
        }

        // Если конфиг пуст, добавим хотя бы стартовую точку, чтобы не было ошибки
        if (route.isEmpty()) {
            route.add(startLoc.toVector());
        }

        // 2. Создаем корень
        BlockDisplay root = createPart(plugin, startLoc, 0, 0);
        plugin.getActiveShips().add(root);

        // 3. Создаем платформу 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                root.addPassenger(createPart(plugin, startLoc, x, z));
            }
        }
        // Приделываем светокамень
        BlockDisplay glow = createPart(plugin, startLoc, 0, 1);
        glow.setBlock(Material.GLOWSTONE.createBlockData());

// ВКЛЮЧАЕМ СВЕТ
// 15, 15 — это максимальная яркость (как у солнца или лавы)
        glow.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));

        Transformation t = glow.getTransformation();
        t.getScale().set(0.5f, 0.5f, 0.5f);
        t.getTranslation().set(-0.25f, 0.0f, 0.75f); // Немного подправленное смещение для центра
        glow.setTransformation(t);

        root.addPassenger(glow);

        glow.setTransformation(t);
        root.addPassenger(glow);

        // 4. Создаем сиденье
        ArmorStand seat = startLoc.getWorld().spawn(startLoc.clone().add(0, -0.5, 0), ArmorStand.class, s -> {
            s.setVisible(false);
            s.setGravity(false);
            s.setSmall(true);
            s.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);
        });
        plugin.setSeat(seat);

        // 5. ЗАПУСКАЕМ ДВИЖЕНИЕ (теперь передаем route вместо startLoc)
        new ShipMoveTask(plugin, root, route).runTaskTimer(plugin, 0L, 1L);
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
