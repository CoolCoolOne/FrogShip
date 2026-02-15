package AlexTro.frogShip;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ShipChorusManager {

    private final FrogShip plugin;

    public ShipChorusManager(FrogShip plugin) {
        this.plugin = plugin;
    }

    public void spawnBackingGroup(World world) {
        // Ищем все вокальные стойки (нижние и верхние)
        List<ArmorStand> vocalSeats = world.getEntitiesByClass(ArmorStand.class).stream()
                .filter(as -> as.getScoreboardTags().contains("ship_seat_vocal_bottom") ||
                        as.getScoreboardTags().contains("ship_seat_vocal_top"))
                .filter(as -> as.getPassengers().isEmpty())
                .toList();

        for (ArmorStand seat : vocalSeats) {
            world.spawn(seat.getLocation(), Frog.class, frog -> {
                frog.setInvulnerable(true);
                frog.setAI(false);
                frog.setCollidable(false);
                frog.setSilent(true);

                // ЛОГИКА ЦВЕТА И РАЗМЕРА
                // ЛОГИКА ЦВЕТА И РАЗМЕРА (СТРОГАЯ)
                if (seat.getScoreboardTags().contains("ship_seat_vocal_bottom")) {
                    // УРОВЕНЬ СВЕТА 2
                    frog.setVariant(Frog.Variant.COLD); // 100% Зеленая
                    if (frog.getAttribute(Attribute.SCALE) != null) {
                        frog.getAttribute(Attribute.SCALE).setBaseValue(1.2);
                    }
                }
                else if (seat.getScoreboardTags().contains("ship_seat_vocal_top")) {
                    // УРОВЕНЬ СВЕТА 3
                    frog.setVariant(Frog.Variant.COLD);      // 100% Белая
                    if (frog.getAttribute(Attribute.SCALE) != null) {
                        frog.getAttribute(Attribute.SCALE).setBaseValue(0.8);
                    }
                }

                frog.getPersistentDataContainer().set(plugin.getShipKey(), PersistentDataType.BYTE, (byte) 1);
                seat.addPassenger(frog);
            });
        }
    }

    public void syncChorus(List<ArmorStand> seats) {
        Frog jason = findJason(seats);
        if (jason == null) return;

        // 1. Генерируем ОДИН шанс на весь хор в этом тике (примерно раз в 3-5 секунд)
        // 0.01 при 20 тиках в секунду даст срабатывание примерно раз в 5 секунд
        boolean shouldCroakNow = java.util.concurrent.ThreadLocalRandom.current().nextDouble() < 0.01;

        // 2. Если шанс выпал — заставляем КВАса и хористов надуться ОДНОВРЕМЕННО
        if (shouldCroakNow) {
            playCroakAnimation(jason); // Главный запевает
        }

        for (ArmorStand seat : seats) {
            if (seat.getScoreboardTags().contains("ship_seat_vocal_bottom") ||
                    seat.getScoreboardTags().contains("ship_seat_vocal_top")) {

                for (Entity p : seat.getPassengers()) {
                    if (p instanceof Frog vocal) {
                        // Поворот головы (всегда за Джейсоном)
                        vocal.setRotation(jason.getLocation().getYaw(), jason.getLocation().getPitch());

                        // Подпевка надувается только если запел Джейсон
                        if (shouldCroakNow) {
                            playCroakAnimation(vocal);
                        }
                    }
                }
            }
        }
    }

    private void playCroakAnimation(Frog frog) {
        try {
            // Используем строку, чтобы IDE не ругалась при компиляции
            frog.playEffect(org.bukkit.EntityEffect.valueOf("FROG_CROAK"));
        } catch (Exception ignored) {
            // Если на сервере нет этого эффекта, просто игнорируем
        }
    }




    private Frog findJason(List<ArmorStand> seats) {
        for (ArmorStand seat : seats) {
            if (seat.getScoreboardTags().contains("ship_seat_dj")) {
                for (Entity p : seat.getPassengers()) {
                    if (p instanceof Frog f && "§6§lКВАс".equals(f.getCustomName())) {
                        return f;
                    }
                }
            }
        }
        return null;
    }
}
