package au.com.addstar.swaparoo;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    private static File confFile;
    private static Boolean debugEnabled = false;
    private static YamlConfiguration config;

    public Config(SwaparooPlugin plugin) {
        // Create config dir if it doesn't exist
        plugin.getDataFolder().mkdir();

        // Create default config file if it doesn't exist
        confFile = new File(plugin.getDataFolder(), "config.yml");
        if (!confFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        // Load config from file
        reload();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(confFile);
        debugEnabled = config.getBoolean("debug", false);
    }

    public Boolean getDebug() {
        return debugEnabled;
    }

    public void setDebug(Boolean debug) {
        debugEnabled = debug;
    }

}
