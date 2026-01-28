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
    private LogManager logManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Settings.load(this);
        this.logManager = new LogManager(this);

        shipKey = new NamespacedKey(this, "is_frog_ship");

        getCommand("frogship").setExecutor(new ShipHelpExecutor());

        // Регистрируем команды в отдельном классе
        ShipCommandExecutor shipExecutor = new ShipCommandExecutor(this);
        getCommand("spawnship").setExecutor(shipExecutor);
        getCommand("rmship").setExecutor(shipExecutor);
        getCommand("spawnfrogs").setExecutor(shipExecutor);
        getCommand("spawnjason").setExecutor(shipExecutor);

        // 2. Взаимодействие (Игроки + Командные блоки)
        ShipInteractionExecutor interactExecutor = new ShipInteractionExecutor(this);
        getCommand("sitonship").setExecutor(interactExecutor);
        getCommand("embark").setExecutor(interactExecutor);
        getCommand("sitoff").setExecutor(interactExecutor);

        // 3. Звуковое сопровождение
        ShipAudioExecutor audioExecutor = new ShipAudioExecutor(this);
        getCommand("audioship").setExecutor(audioExecutor);
        getCommand("stopaudioship").setExecutor(audioExecutor);

        // РЕГИСТРИРУЕМ СОБЫТИЯ (Shift-логика)
        getServer().getPluginManager().registerEvents(new ShipEvents(this), this);
        getServer().getPluginManager().registerEvents(new FireworkListener(), this);

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
            for (ArmorStand as : world.getEntitiesByClass(ArmorStand.class)) {
                if (as.getScoreboardTags().contains("ship_seat")) {
                    // Удаляем лягушек
                    as.getPassengers().forEach(passenger -> {
                        if (!(passenger instanceof org.bukkit.entity.Player)) {
                            passenger.remove();
                        }
                    });
                    as.remove();
                }
            }
            for (org.bukkit.entity.Interaction inter : world.getEntitiesByClass(org.bukkit.entity.Interaction.class)) {
                if (inter.getScoreboardTags().contains("ship_firework_button")) {
                    inter.remove();
                }
            }
        });
    }

    public void removeAllShips() {
        // Чистим активные из списка
        for (BlockDisplay ship : new ArrayList<>(activeShips)) {
            if (ship != null) {
                ship.getPassengers().forEach(Entity::remove);
                ship.remove();
            }
        }

        // Глобальная зачистка по всем мирам для надежности
        Bukkit.getWorlds().forEach(w -> {
            // Удаляем сиденья
            w.getEntitiesByClass(ArmorStand.class).stream()
                    .filter(as -> as.getScoreboardTags().contains("ship_seat"))
                    .forEach(as -> {
                        as.getPassengers().forEach(p -> {
                            if (!(p instanceof org.bukkit.entity.Player))
                                p.remove();
                        });
                        as.remove();
                    });

            // НОВОЕ: Удаляем хитбоксы кнопок
            w.getEntitiesByClass(org.bukkit.entity.Interaction.class).stream()
                    .filter(inter -> inter.getScoreboardTags().contains("ship_firework_button"))
                    .forEach(Entity::remove);
        });

        activeShips.clear();
    }

    public LogManager getLogManager() {
        return logManager;
    }

    // Геттеры для доступа из других классов
    public NamespacedKey getShipKey() {
        return shipKey;
    }

    public List<BlockDisplay> getActiveShips() {
        return activeShips;
    }

}
