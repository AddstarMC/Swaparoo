package au.com.addstar.swaparoo;

import org.bukkit.entity.Player;

import java.util.UUID;

public class StarManager {
    private final SwaparooPlugin plugin;
    private DataManager dataManager;

    public StarManager(SwaparooPlugin plugin) {
        this.plugin = plugin;
        dataManager = plugin.getDM();
    }

    public boolean giveStars(UUID playerid, String type, int amount, boolean notifyPlayer) {
        Integer balance = dataManager.getStarCount(playerid, type, false);
        if (balance == null) {
            return false;
        }
        setStars(playerid, type, balance + amount, notifyPlayer);
        return true;
    }

    public boolean takeStars(UUID playerid, String type, int amount, boolean notifyPlayer) {
        Integer balance = dataManager.getStarCount(playerid, type, false);
        if (balance == null || balance < amount) {
            return false;
        }
        setStars(playerid, type, balance - amount, notifyPlayer);
        return true;
    }

    public boolean setStars(UUID playerid, String type, int amount, boolean notifyPlayer) {
        if (dataManager.setStarCount(playerid, type, amount)) {
            if (notifyPlayer || SwaparooPlugin.isDebug()) {
                int finalAmount = amount;
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Player player = plugin.getServer().getPlayer(playerid);
                    String name = player != null ? player.getName() : "unknown";
                    SwaparooPlugin.debugMsg("Player " + name + " (" + playerid + ") now has " + finalAmount + " " + type);

                    if (notifyPlayer && player != null) {
                        String typeFormatted = type.contains("gems") ? "<yellow>Star<gold>Gems</gold></yellow>" : "<yellow>Star<white>Dust</white></yellow>";
                        plugin.sendMsg(player, "<light_purple>►► <green>You now have <aqua>" + finalAmount + " " + typeFormatted);
                    }
                });
            }
            return true;
        } else {
            int finalAmount = amount;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Player player = plugin.getServer().getPlayer(playerid);
                String name = player != null ? player.getName() : "unknown";
                SwaparooPlugin.errMsg("Failed to set " + type + " for player " + name + " (" + playerid + ") to " + finalAmount);
            });
            return false;
        }
    }
}
