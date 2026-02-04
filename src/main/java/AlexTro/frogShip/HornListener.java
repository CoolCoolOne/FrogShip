package AlexTro.frogShip;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.Bukkit;

public class HornListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getScoreboardTags().contains("ship_horn_button")) {
            Bukkit.broadcastMessage("§aГудок!"); // Проверка
            playHorn(e.getRightClicked().getLocation());
        }
    }

    @EventHandler // ЛКМ по хитбоксу
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getEntity().getScoreboardTags().contains("ship_horn_button")) {
            Bukkit.broadcastMessage("§aГудок!"); // Проверка
            playHorn(e.getEntity().getLocation());
            e.setCancelled(true); // Отменяем урон, чтобы хитбокс не "дергался"
        }
    }

    private void playHorn(org.bukkit.Location loc) {
        // 1.0f - громкость, 1.0f - тональность
        loc.getWorld().playSound(loc, "item.goat_horn.sound.4", SoundCategory.NEUTRAL, 1.0f, 1.0f);
    }
}
