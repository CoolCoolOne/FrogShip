package AlexTro.frogShip;

import org.bukkit.configuration.file.FileConfiguration;

public class Settings {
    public static int bubbleCount;
    public static int smokeCount;
    public static int lanternCount;
    public static int frogGlowCount;

    public static void load(FrogShip plugin) {
        plugin.saveDefaultConfig(); // Создаст файл, если его нет
        plugin.reloadConfig();      // Обновит данные из файла
        FileConfiguration config = plugin.getConfig();

        bubbleCount = config.getInt("effects.water_bubbles_amount", 1);
        smokeCount = config.getInt("effects.smoke_amount", 1);
        lanternCount = config.getInt("effects.lantern_glow_amount", 1);
        frogGlowCount = config.getInt("effects.frog_glow_amount", 1);
        musicNoteCount = config.getInt("effects.music_note_amount", 1);
    musicNoteChance = config.getDouble("effects.music_note_chance", 0.01);
    }
}
