package AlexTro.frogShip;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

import java.util.*;
import java.util.stream.Collectors;

public class ShipEvents implements Listener {

    private final FrogShip plugin;
    private final Random random = new Random();
    
    // Используем Map для хранения времени последней пересадки (Cooldown)
    private final Map<UUID, Long> lastTransfer = new HashMap<>();

    public ShipEvents(FrogShip plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShift(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Обработка принудительного выхода
        if (player.getScoreboardTags().contains("is_leaving_ship")) {
            player.removeScoreboardTag("is_leaving_ship");
            return;
        }

        // ПРОВЕРКА КУЛДАУНА (исправляет двойное сообщение)
        long now = System.currentTimeMillis();
        if (lastTransfer.containsKey(player.getUniqueId())) {
            if (now - lastTransfer.get(player.getUniqueId()) < 200) { // 200 мс достаточно
                event.setCancelled(true); // Всё равно отменяем выход
                return;
            }
        }

        if (!(event.getDismounted() instanceof ArmorStand currentSeat)) return;
        if (!currentSeat.getScoreboardTags().contains("ship_seat")) return;

        // Отменяем выход
        event.setCancelled(true);

        // Поиск мест
        List<ArmorStand> availableSeats = player.getWorld().getNearbyEntities(player.getLocation(), 10, 5, 10).stream()
                .filter(e -> e instanceof ArmorStand)
                .map(e -> (ArmorStand) e)
                .filter(as -> as.getScoreboardTags().contains("ship_seat_player"))
                .filter(as -> !as.equals(currentSeat))
                .filter(as -> as.getPassengers().isEmpty())
                .collect(Collectors.toList());

        if (availableSeats.isEmpty()) {
            player.sendActionBar("§cНет других свободных мангровых мест!");
            return;
        }

        // Записываем время текущей пересадки
        lastTransfer.put(player.getUniqueId(), now);

        ArmorStand nextSeat = availableSeats.get(random.nextInt(availableSeats.size()));

        // Пересадка
        Bukkit.getScheduler().runTask(plugin, () -> {
            nextSeat.addPassenger(player);
            player.sendMessage("§bПерепрыгнул на другое сиденье! Сойти: /sitoff");
        });
        
        // Очистка мапы через 5 секунд, чтобы не забивать память
        Bukkit.getScheduler().runTaskLater(plugin, () -> lastTransfer.remove(player.getUniqueId()), 100L);
    }
}
