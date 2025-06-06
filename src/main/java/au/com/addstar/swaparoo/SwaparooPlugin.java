package au.com.addstar.swaparoo;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;

public final class SwaparooPlugin extends JavaPlugin implements Listener {
    public static SwaparooPlugin instance;
    private static Config config;
    private static DataManager dataManager;
    private static StarManager starManager;
    private static BuyManager buyManager;
    private final MiniMessage miniMessage;   // MiniMessage Parser
    private final String serverName;

    public SwaparooPlugin() {
        instance = this;
        this.miniMessage = MiniMessage.miniMessage();
        this.serverName = findServerName();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            logMsg("PlaceholderAPI found! Registering events and placeholders...");
            Bukkit.getPluginManager().registerEvents(this, this);
            new PlaceholderAPI(this).register();
        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            getLogger().warning("Could not find PlaceholderAPI! This is required, disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialise configuration
        config = new Config(this);

        // Initialise DataManager
        dataManager = new DataManager(this);
        try {
            dataManager.initialise();
        } catch (SQLException e) {
            SwaparooPlugin.logMsg("TreasuredDB: Failed to initialize database connection pool! " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        starManager = new StarManager(this);
        buyManager = new BuyManager(this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        SwaparooCommand swaparooCommand = new SwaparooCommand(this);
        getCommand("swaparoo").setExecutor(swaparooCommand);
        getCommand("swaparoo").setTabCompleter(swaparooCommand);

        getCommand("stargems").setExecutor(new StarCommand(this));
        getCommand("stargems").setAliases(Arrays.asList("gems", "dust", "stardust"));

        getCommand("buyconfirm").setExecutor(new ConfirmCommand(this));
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.close();
        }
    }

    public DataManager getDM() {
        return dataManager;
    }

    public MiniMessage getMM() {
        return this.miniMessage;
    }

    public StarManager getSM() {
        return starManager;
    }

    public BuyManager getBM() {
        return buyManager;
    }

    public static Config getConfigs() {
        return config;
    }

    public static void logMsg(String msg) {
        instance.getLogger().info("[Swaparoo] " + msg);
    }

    public static void errMsg(String msg) {
        instance.getLogger().severe("[Swaparoo] " + msg);
    }

    public static void debugMsg(String msg) {
        if (config.getDebug())
            // Correct plugin name in debug messages
            instance.getLogger().info("[Swaparoo] DEBUG: " + msg);
    }

    public static boolean isDebug() {
        return config.getDebug();
    }

    public void sendMsg(Player player, String message) {
        player.sendMessage(miniMessage.deserialize(message));
    }

    public void sendMsg(CommandSender sender, String message) {
        sender.sendMessage(miniMessage.deserialize(message));
    }

    public String getServerName() {
        return serverName;
    }

    public static String findServerName() {
        // Define expected base directory and get the current working directory
        String basePath = "/data/srv-";
        String currentDir = new File("").getAbsolutePath();

        // Check if the current directory contains the expected base path
        if (currentDir.contains(basePath)) {
            // Find the index where the base path ends
            int startIndex = currentDir.indexOf(basePath) + basePath.length();

            // Extract the substring starting from the end of the base path
            String remainingPath = currentDir.substring(startIndex);

            // Split by the file separator or other expected delimiters if needed
            return remainingPath.split("/")[0]; // Gets the part before the next "/"
        } else {
            // Handle cases where the path does not match the expected format
            logMsg("Error: Current directory does not match expected path format.");
            return null;
        }
    }
}
