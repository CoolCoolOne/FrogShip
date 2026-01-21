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

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Settings.load(this);

        shipKey = new NamespacedKey(this, "is_frog_ship");
        
        // Регистрируем команды в отдельном классе
        ShipCommandExecutor executor = new ShipCommandExecutor(this);
        getCommand("spawnship").setExecutor(executor);
        getCommand("rmship").setExecutor(executor);
        getCommand("sitonship").setExecutor(executor);
        getCommand("sitoff").setExecutor(executor); // Регистрируем sitoff
        
        // РЕГИСТРИРУЕМ СОБЫТИЯ (Shift-логика)
        getServer().getPluginManager().registerEvents(new ShipEvents(this), this);
        
        
        cleanAllWorldsFromShips();

        Bukkit.getConsoleSender().sendMessage("§a");
    Bukkit.getConsoleSender().sendMessage("§a  ////////////////////////////////////////");
    Bukkit.getConsoleSender().sendMessage("§a  //                                    //");
    Bukkit.getConsoleSender().sendMessage("§a  //    §2FROG SHIP §aPlugin v0.1       //");
    Bukkit.getConsoleSender().sendMessage("§a  //    §fСтатус: §2ЗАПУЩЕН §a          //");
    Bukkit.getConsoleSender().sendMessage("§a  //                                    //");
    Bukkit.getConsoleSender().sendMessage("§a  ////////////////////////////////////////");
    Bukkit.getConsoleSender().sendMessage("§a");
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
             for (Entity entity : world.getEntitiesByClass(ArmorStand.class)) {
                if (entity.getScoreboardTags().contains("ship_seat")) {
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
        Bukkit.getWorlds().forEach(w -> w.getEntitiesByClass(ArmorStand.class).stream()
            .filter(as -> as.getScoreboardTags().contains("ship_seat"))
            .forEach(Entity::remove));
        activeShips.clear();
    }

    // Геттеры для доступа из других классов
    public NamespacedKey getShipKey() { return shipKey; }
    public List<BlockDisplay> getActiveShips() { return activeShips; }
}
