package AlexTro.frogShip;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkListener implements Listener {

    @EventHandler // ПКМ по кнопке
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getScoreboardTags().contains("ship_firework_button")) {
            spawnFirework(e.getRightClicked().getLocation());
        }
    }

    @EventHandler // ЛКМ по кнопке
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getEntity().getScoreboardTags().contains("ship_firework_button")) {
            spawnFirework(e.getEntity().getLocation());
            e.setCancelled(true); // Чтобы не "убить" хитбокс
        }
    }

    private void spawnFirework(org.bukkit.Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
                .withColor(Color.fromRGB(255, 50, 50), Color.ORANGE) // Насыщенный красный
                .withFade(Color.YELLOW) // Плавный переход в желтый
                .with(FireworkEffect.Type.BALL_LARGE) // Большой бабах
            .withFlicker() // ИСПРАВЛЕНО: добавлено 'with'
            .withTrail()   // ИСПРАВЛЕНО: добавлено 'with'
                .build();

        meta.addEffect(effect);
        meta.setPower(1); // Высота взлета
        fw.setFireworkMeta(meta);
        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
    }
}
