package au.com.addstar.swaparoo;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    private static File confFile;
    private static Boolean debugEnabled = false;
    private static Boolean dustEnabled = false;
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

    public Boolean isDustEnabled() {
        return dustEnabled;
    }

    public void setDustEnabled(Boolean dustEnabled) {
        Config.dustEnabled = dustEnabled;
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(confFile);
        debugEnabled = config.getBoolean("debug", false);
        dustEnabled = config.getBoolean("dust-enabled", false);
    }

    public Boolean getDebug() {
        return debugEnabled;
    }

    public void setDebug(Boolean debug) {
        debugEnabled = debug;
    }

}
