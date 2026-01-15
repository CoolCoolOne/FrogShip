// src/main/java/AlexTro/frogShip/FrogShip.java

package AlexTro.frogShip;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class FrogShip extends JavaPlugin {

    // Эти переменные остаются здесь, так как корабль всегда один
    private final List<BlockDisplay> activeShips = new ArrayList<>();
    private NamespacedKey shipKey;
    private ArmorStand seat;

    @Override
    public void onEnable() {
        shipKey = new NamespacedKey(this, "is_frog_ship");
        
        // Регистрируем команды в отдельном классе
        ShipCommandExecutor executor = new ShipCommandExecutor(this);
        getCommand("spawnship").setExecutor(executor);
        getCommand("sitonship").setExecutor(executor);
        
        cleanAllWorldsFromShips();
    }

    @Override
    public void onDisable() {
        removeAllShips();
    }

    // --- Вспомогательные методы (можно оставить тут) ---

    public void cleanAllWorldsFromShips() {
        Bukkit.getWorlds().forEach(world -> {
            for (Entity entity : world.getEntitiesByClass(BlockDisplay.class)) {
                if (entity.getPersistentDataContainer().has(shipKey, org.bukkit.persistence.PersistentDataType.BYTE)) {
                    entity.getPassengers().forEach(Entity::remove);
                    entity.remove();
                }
            }
        });
    }

    public void removeAllShips() {
        for (BlockDisplay ship : new ArrayList<>(activeShips)) {
            if (ship != null) {
                ship.getPassengers().forEach(Entity::remove);
                ship.remove();
            }
        }
        if (seat != null) seat.remove(); // Удаляем сиденье
        activeShips.clear();
    }

    // Геттеры для доступа из других классов
    public NamespacedKey getShipKey() { return shipKey; }
    public List<BlockDisplay> getActiveShips() { return activeShips; }
    public ArmorStand getSeat() { return seat; }
    public void setSeat(ArmorStand seat) { this.seat = seat; }
}
