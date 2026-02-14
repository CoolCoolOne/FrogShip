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
                frog.setAI(true);
                frog.setCollidable(false);
                frog.setSilent(true);

                // ЛОГИКА ЦВЕТА И РАЗМЕРА
                if (seat.getScoreboardTags().contains("ship_seat_vocal_bottom")) {
                    frog.setVariant(Frog.Variant.TEMPERATE); // ЗЕЛЕНАЯ (Нижняя)
                    if (frog.getAttribute(Attribute.SCALE) != null) {
                        frog.getAttribute(Attribute.SCALE).setBaseValue(1.1);
                    }
                } else {
                    frog.setVariant(Frog.Variant.COLD); // БЕЛАЯ (Верхняя)
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
        Frog jason = null;

        // 1. Ищем КВАса по имени
        for (ArmorStand seat : seats) {
            if (seat.getScoreboardTags().contains("ship_seat_dj")) {
                for (Entity p : seat.getPassengers()) {
                    if (p instanceof Frog f && "§6§lКВАс".equals(f.getCustomName())) {
                        jason = f;
                        break;
                    }
                }
            }
        }

        if (jason == null) return;

        // 2. Синхронизируем всех хористов
        for (ArmorStand seat : seats) {
            if (seat.getScoreboardTags().contains("ship_seat_vocal_bottom") ||
                    seat.getScoreboardTags().contains("ship_seat_vocal_top")) {

                for (Entity p : seat.getPassengers()) {
                    if (p instanceof Frog vocal) {
                        // Поворачиваем голову точно туда же, куда смотрит КВАс
                        vocal.setRotation(jason.getLocation().getYaw(), jason.getLocation().getPitch());
                    }
                }
            }
        }
    }
}
