package me.yourname; // Укажите ваш пакет

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ShipPlugin extends JavaPlugin implements CommandExecutor {

    // Список для отслеживания активных кораблей
    private final List<BlockDisplay> activeShips = new ArrayList<>();

    @Override
    public void onEnable() {
        getCommand("spawnship").setExecutor(this);
        getLogger().info("ShipPlugin запущен!");
    }

    @Override
    public void onDisable() {
        // Очистка всех кораблей при выключении сервера или перезагрузке
        removeAllShips();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        // Удаляем все старые корабли перед спавном нового
        removeAllShips();

        Location startLoc = player.getLocation();
        spawnMovingPlatform(startLoc);
        
        player.sendMessage("§eКорабль заспавнен!");
        return true;
    }

    /**
     * Удаляет все существующие корабли
     */
    private void removeAllShips() {
        for (BlockDisplay ship : activeShips) {
            if (ship != null && !ship.isDead()) {
                ship.getPassengers().forEach(org.bukkit.entity.Entity::remove);
                ship.remove();
            }
        }
        activeShips.clear();
    }

    /**
     * Математика движения (Синусоида по оси X)
     */
    private Location calculateNextLocation(Location startLoc, double time, double range) {
        double offsetX = Math.sin(time) * range;
        return startLoc.clone().add(offsetX, 0, 0);
    }

    /**
     * Создание невидимого шалкера для твердой коллизии
     */
    private Shulker spawnCollision(Location loc) {
        Shulker shulker = (Shulker) loc.getWorld().spawnEntity(loc, EntityType.SHULKER);
        shulker.setInvisible(true);
        shulker.setAI(false);
        shulker.setInvulnerable(true);
        shulker.setSilent(true);
        shulker.setPersistent(false); // Не сохранять в файлах мира
        return shulker;
    }

    /**
     * Основная логика спавна и движения
     */
    private void spawnMovingPlatform(Location startLoc) {
        // Создаем визуальную часть
        BlockDisplay ship = (BlockDisplay) startLoc.getWorld().spawnEntity(startLoc, EntityType.BLOCK_DISPLAY);
        ship.setBlock(Material.OAK_PLANKS.createBlockData());
        activeShips.add(ship);

        // Создаем коллизию и привязываем к визуальной части
        Shulker collision = spawnCollision(startLoc);
        ship.addPassenger(collision);

        // Настройка плавности
        ship.setInterpolationDuration(2);
        ship.setInterpolationDelay(0);

        new BukkitRunnable() {
            double time = 0;
            final double range = 5.0;

            @Override
            public void run() {
                // ПРОВЕРКА: Если чанк выгрузился (игрок ушел далеко)
                if (!ship.getLocation().getChunk().isLoaded()) {
                    ship.remove();
                    collision.remove();
                    activeShips.remove(ship);
                    Bukkit.broadcastMessage("§7Корабль уплыл далеко ...");
                    this.cancel();
                    return;
                }

                // Проверка на удаление (например, через команду /spawnship)
                if (ship.isDead() || !ship.isValid()) {
                    collision.remove();
                    this.cancel();
                    return;
                }

                // Движение
                Location nextLoc = calculateNextLocation(startLoc, time, range);
                ship.teleport(nextLoc);
                
                // Важно обновлять трансформацию для работы интерполяции
                ship.setTransformation(ship.getTransformation());

                time += 0.05; // Скорость
            }
        }.runTaskTimer(this, 0L, 2L);
    }
}
