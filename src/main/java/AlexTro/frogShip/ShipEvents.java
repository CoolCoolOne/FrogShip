// src/main/java/AlexTro/frogShip/ShipEvents.java

package AlexTro.frogShip;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ShipEvents implements Listener {

    private final FrogShip plugin;
    private final Random random = new Random();

    public ShipEvents(FrogShip plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShift(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDismounted() instanceof ArmorStand currentSeat)) return;

        // Проверяем, что игрок сидит именно на сиденье корабля
        if (!currentSeat.getScoreboardTags().contains("ship_seat")) return;

        // Если игрок нажал shift, мы отменяем стандартное слезание (в 2026 году это основной способ перехвата)
        event.setCancelled(true);

        // Ищем ВСЕ мангровые сиденья в радиусе 10 блоков (игроков сажаем только на них)
        List<ArmorStand> availableSeats = player.getWorld().getNearbyEntities(player.getLocation(), 10, 5, 10).stream()
                .filter(e -> e instanceof ArmorStand)
                .map(e -> (ArmorStand) e)
                .filter(as -> as.getScoreboardTags().contains("ship_seat_player")) // Только мангровые
                .filter(as -> !as.equals(currentSeat)) // Исключаем текущее
                .filter(as -> as.getPassengers().isEmpty()) // Только свободные
                .collect(Collectors.toList());

        if (availableSeats.isEmpty()) {
            player.sendMessage("§cНет других свободных мангровых сидений для перехода!");
            return;
        }

        // Выбираем случайное сиденье из списка
        ArmorStand nextSeat = availableSeats.get(random.nextInt(availableSeats.size()));
        
        // Пересаживаем
        nextSeat.addPassenger(player);
        player.sendMessage("§bПерепрыгнул на другое мангровое сиденье!");
    }
}
