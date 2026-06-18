package camchua.serverstaff;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;

public class FileManager {

    private static HashMap<Files, File> file = new HashMap<Files, File>();
    private static HashMap<Files, FileConfiguration> configuration = new HashMap<Files, FileConfiguration>();

    public static void setup(Plugin plugin) {
        file = new HashMap<Files, File>();
        configuration = new HashMap<Files, FileConfiguration>();
        boolean legacy = Utils.isLegacy();
        for(Files f : Files.values()) {
            String location = legacy ? f.getLegacyLocation() : f.getLocation();
            File fl = new File(plugin.getDataFolder(), location);
            if(!fl.exists()) {
                fl.getParentFile().mkdirs();
                plugin.saveResource(location, false);
            }
            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(fl);
            } catch(Exception ex) {

            }
            file.put(f, fl);
            configuration.put(f, config);
        }
    }

    public static FileConfiguration getFileConfig(Files f) {
        return configuration.get(f);
    }

    public static void saveFileConfig(FileConfiguration data, Files f) {
        try {
            data.save(file.get(f));
        } catch(Exception ex) {

        }
    }

    public static void loadFileConfig(FileConfiguration data, Files f) {
        try {
            data.load(file.get(f));
            configuration.replace(f, data);
        } catch(Exception ex) {

        }
    }

    public enum Files {

        CONFIG("config.yml", "config.yml"),
        GUI("gui-1.13.yml", "gui.yml"),
        STAFFONLINE("staffonline.yml", "staffonline.yml"),
        REPORT("report.yml", "report.yml"),
        MESSAGES("messages.yml", "messages.yml"),
        DATA("data.yml", "data.yml"),
        ;

        private String location;
        private String legacyLocation;

        Files(String l, String legacyLocation) {
            this.location = l;
            this.legacyLocation = legacyLocation;
        }

        public String getLocation() {
            return this.location;
        }

        public String getLegacyLocation() {
            return this.legacyLocation;
        }

    }

}
