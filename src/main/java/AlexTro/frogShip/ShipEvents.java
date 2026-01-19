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
    // Список игроков, которые прямо сейчас находятся в процессе пересадки
    private final Set<UUID> processingPlayers = new HashSet<>();

    public ShipEvents(FrogShip plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShift(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // 1. Если игрок уже в процессе пересадки — игнорируем событие
        if (processingPlayers.contains(player.getUniqueId())) return;

        if (!(event.getDismounted() instanceof ArmorStand currentSeat)) return;

        // Проверяем, что это именно сиденье корабля
        if (!currentSeat.getScoreboardTags().contains("ship_seat")) return;

        // Отменяем стандартное слезание (игрок остается в "состоянии сидения")
        event.setCancelled(true);

        // Ищем свободные мангровые сиденья
        List<ArmorStand> availableSeats = player.getWorld().getNearbyEntities(player.getLocation(), 10, 5, 10).stream()
                .filter(e -> e instanceof ArmorStand)
                .map(e -> (ArmorStand) e)
                .filter(as -> as.getScoreboardTags().contains("ship_seat_player"))
                .filter(as -> !as.equals(currentSeat))
                .filter(as -> as.getPassengers().isEmpty())
                .collect(Collectors.toList());

        if (availableSeats.isEmpty()) {
            // Чтобы не спамить чат при каждом микро-движении Shift,
            // можно либо убрать сообщение, либо слать его в ActionBar
            player.sendActionBar("§cНет других свободных мангровых мест!");
            return;
        }

        // Выбираем новое место
        ArmorStand nextSeat = availableSeats.get(random.nextInt(availableSeats.size()));

        // 2. Блокируем повторный вход в этот метод
        processingPlayers.add(player.getUniqueId());

        // 3. Выполняем пересадку в следующем тике
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                nextSeat.addPassenger(player);
                player.sendMessage("§bПерепрыгнул на другое сиденье!");
            } finally {
                // В любом случае удаляем игрока из списка обработки
                processingPlayers.remove(player.getUniqueId());
            }
        });
    }
}
