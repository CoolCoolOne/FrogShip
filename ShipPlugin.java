package AlexTro.frogShip;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;

import java.util.ArrayList;
import java.util.List;

public final class FrogShip extends JavaPlugin implements CommandExecutor {

    private final List<BlockDisplay> activeShips = new ArrayList<>();
    private NamespacedKey shipKey;

    @Override
    public void onEnable() {
        shipKey = new NamespacedKey(this, "is_frog_ship");
        getCommand("spawnship").setExecutor(this);
        cleanAllWorldsFromShips();
    }

    @Override
    public void onDisable() {
        removeAllShips();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков!");
            return true;
        }

        // Проверка на наличие 3-х аргументов
        if (args.length < 3) {
            player.sendMessage("§cИспользование: /spawnship <x> <y> <z>");
            return true;
        }

        try {
            // Считываем целые числа
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);

            // Добавляем 0.5, чтобы корабль был в центре блока, а не на углу
            Location targetLoc = new Location(player.getWorld(), x + 0.5, y + 0.5, z + 0.5);

            removeAllShips();
            cleanAllWorldsFromShips();

            spawnMovingPlatform(targetLoc);
            player.sendMessage(String.format("§eКорабль заспавнен на координатах: %d, %d, %d", x, y, z));

        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: Координаты должны быть целыми числами!");
        }

        return true;
    }



    private void removeShip(BlockDisplay ship, BukkitRunnable task) {
    if (ship != null) {
        // Удаляем "детей" (пассажиров)
        ship.getPassengers().forEach(Entity::remove);
        // Удаляем "родителя"
        ship.remove();
        activeShips.remove(ship);
        Bukkit.broadcastMessage("§7Корабль уплыл далеко и исчез...");
    }
    if (task != null) task.cancel();
}

private void removeAllShips() {
    for (BlockDisplay ship : new ArrayList<>(activeShips)) {
        if (ship != null) {
            // Удаляем всех прикрепленных пассажиров перед удалением корня
            ship.getPassengers().forEach(Entity::remove);
            ship.remove();
        }
    }
    activeShips.clear();
}


private void cleanAllWorldsFromShips() {
    Bukkit.getWorlds().forEach(world -> {
        for (Entity entity : world.getEntitiesByClass(BlockDisplay.class)) {
            if (entity.getPersistentDataContainer().has(shipKey, PersistentDataType.BYTE)) {
                // Сначала удаляем пассажиров, если они есть
                entity.getPassengers().forEach(Entity::remove); 
                entity.remove();
            }
        }
    });
}

private BlockDisplay createPart(Location loc, float offsetX, float offsetZ) {
    BlockDisplay part = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
    part.setBlock(Material.OAK_PLANKS.createBlockData());
    part.getPersistentDataContainer().set(shipKey, PersistentDataType.BYTE, (byte) 1);

    Transformation transformation = part.getTransformation();
    // offsetX и offsetZ позволяют блокам встать рядом (на расстоянии 1 блока друг от друга)
    transformation.getTranslation().set(offsetX - 0.5f, -0.5f, offsetZ - 0.5f);
    part.setTransformation(transformation);

    part.setInterpolationDuration(2);
    part.setInterpolationDelay(0);
    return part;
}






        private void spawnMovingPlatform(Location startLoc) {
        // 1. Создаем центральный блок (корень всей конструкции)
        BlockDisplay root = createPart(startLoc, 0, 0);
        activeShips.add(root);

        // 2. Создаем сетку блоков вокруг центра и привязываем их как пассажиров
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // Пропускаем центр, он уже в списке root
                
                // Создаем часть платформы и сажаем её на root
                BlockDisplay part = createPart(startLoc, x, z);
                root.addPassenger(part);
            }
        }

        // 3. Запускаем задачу движения
        new BukkitRunnable() {
            double time = 0;
            final double range = 5.0; // Амплитуда движения
            final double maxDistanceSq = Math.pow(64, 2); // 4 чанка (64 блока)

            @Override
            public void run() {
                // Проверка на валидность центрального блока и загрузку чанка
                if (root.isDead() || !root.isValid() || !root.getLocation().getChunk().isLoaded()) {
                    removeShip(root, this);
                    return;
                }

                // Проверка: есть ли игроки в радиусе 4 чанков
                boolean playerNearby = root.getWorld().getPlayers().stream()
                        .anyMatch(p -> p.getLocation().distanceSquared(root.getLocation()) <= maxDistanceSq);

                if (!playerNearby) {
                    removeShip(root, this);
                    return;
                }

                // Рассчитываем новую позицию для центрального блока
                double offsetX = Math.sin(time) * range;
                Location nextLoc = startLoc.clone().add(offsetX, 0, 0);

                // Телепортируем ТОЛЬКО центральный блок (пассажиры переместятся автоматически)
                root.teleport(nextLoc);
                
                // Обновляем интерполяцию для всех 9 блоков, чтобы движение было плавным
                updateInterpolation(root);

                // Увеличиваем время (скорость движения)
                time += 0.03;
            }

            /**
             * Вспомогательный метод для обновления интерполяции у всей структуры
             */
            private void updateInterpolation(BlockDisplay rootEntity) {
                // Обновляем главный блок
                rootEntity.setInterpolationDuration(2);
                rootEntity.setInterpolationDelay(0);
                
                // Обновляем всех его пассажиров
                for (org.bukkit.entity.Entity passenger : rootEntity.getPassengers()) {
                    if (passenger instanceof BlockDisplay bd) {
                        bd.setInterpolationDuration(2);
                        bd.setInterpolationDelay(0);
                    }
                }
            }
            
        }.runTaskTimer(this, 0L, 2L);
    }

    
}
