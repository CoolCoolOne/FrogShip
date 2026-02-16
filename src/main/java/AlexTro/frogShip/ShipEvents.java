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

        // 1. Пропускаем, если игрок реально выходит через команду
        if (player.getScoreboardTags().contains("is_leaving_ship")) {
            player.removeScoreboardTag("is_leaving_ship");
            return;
        }

        // 2. Валидация сиденья
        if (!(event.getDismounted() instanceof ArmorStand currentSeat)) return;
        if (!currentSeat.getScoreboardTags().contains("ship_seat")) return;

        // 3. Кулдаун (защита от спама)
        long now = System.currentTimeMillis();
        if (lastTransfer.getOrDefault(player.getUniqueId(), 0L) > now - 300) {
            return;
        }
        lastTransfer.put(player.getUniqueId(), now);

        // 4. Поиск нового места
        List<ArmorStand> availableSeats = player.getWorld().getNearbyEntities(player.getLocation(), 15, 5, 15).stream()
                .filter(e -> e instanceof ArmorStand as && as.getScoreboardTags().contains("ship_seat_player"))
                .map(e -> (ArmorStand) e)
                .filter(as -> !as.equals(currentSeat) && as.getPassengers().isEmpty())
                .collect(Collectors.toList());

        if (availableSeats.isEmpty()) {
            player.sendActionBar("§cНет других свободных мест!");
            return;
        }

        ArmorStand nextSeat = availableSeats.get(random.nextInt(availableSeats.size()));

        // 5. САМОЕ ВАЖНОЕ: Пересадка в следующем тике
        // НЕ отменяем event, даем игроку слезть, и тут же сажаем обратно
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (nextSeat.isValid() && nextSeat.getPassengers().isEmpty()) {
                nextSeat.addPassenger(player);
                player.sendActionBar("§bПерепрыгнул! Cлезть: /sitoff");
            }
        });
    }

}
