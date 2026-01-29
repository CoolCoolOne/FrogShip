package AlexTro.frogShip;

import org.bukkit.Bukkit;
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
            spawnFireworkSeries(e.getRightClicked().getLocation());
        }
    }

    @EventHandler // ЛКМ по кнопке
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getEntity().getScoreboardTags().contains("ship_firework_button")) {
            spawnFireworkSeries(e.getEntity().getLocation());
            e.setCancelled(true); // Чтобы не "убить" хитбокс
        }
    }

private void spawnFireworkSeries(org.bukkit.Location loc) {
    // Получаем доступ к плагину без конструктора
    FrogShip plugin = FrogShip.getPlugin(FrogShip.class);

    for (int i = 0; i < 5; i++) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Firework fw = loc.getWorld().spawn(loc, Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();

            // Power 3 — летит долго и высоко
            meta.setPower(2); 

            meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.RED, Color.ORANGE)
                    .withFade(Color.YELLOW)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withFlicker()
                    .build());

            fw.setFireworkMeta(meta);
            loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
        }, i * 20L); // Задержка между залпами (1.0 сек)
    }
}

}
