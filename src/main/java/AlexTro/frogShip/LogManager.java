package AlexTro.frogShip;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    private final FrogShip plugin;
    private final File logFile;

    public LogManager(FrogShip plugin) {
        this.plugin = plugin;
        // Файл будет создан в папке /plugins/FrogShip/logs.txt
        this.logFile = new File(plugin.getDataFolder(), "logs.txt");
        createFile();
    }

    private void createFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void log(String playerName, String commandName) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = String.format("[%s] Игрок: %s | Команда: /%s", timeStamp, playerName, commandName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logEntry);
            writer.newLine();
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось записать лог: " + e.getMessage());
        }
    }
}
