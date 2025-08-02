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
        setStars(playerid, type, balance + amount, amount, notifyPlayer);
        return true;
    }

    public boolean takeStars(UUID playerid, String type, int amount, boolean notifyPlayer) {
        Integer balance = dataManager.getStarCount(playerid, type, false);
        if (balance == null || balance < amount) {
            return false;
        }
        setStars(playerid, type, balance - amount, (-amount), notifyPlayer);
        return true;
    }

    public boolean setStars(UUID playerid, String type, int newBalance, int change, boolean notifyPlayer) {
        if (dataManager.setStarCount(playerid, type, newBalance)) {
            if (notifyPlayer || SwaparooPlugin.isDebug()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Player player = plugin.getServer().getPlayer(playerid);
                    String name = player != null ? player.getName() : "unknown";
                    SwaparooPlugin.debugMsg("Player " + name + " (" + playerid + ") now has " + newBalance + " " + type);

                    if (notifyPlayer && player != null) {
                        String typeFormatted = type.contains("gems") ? "<yellow>Star<gold>Gems</gold></yellow>" : "<yellow>Star<white>Dust</white></yellow>";
                        if (change > 0) {
                            plugin.sendMsg(player, "<light_purple>►► <green>You have received <aqua>" + change + " " + typeFormatted);
                            SwaparooPlugin.logMsg("Player " + name + " (" + playerid + ") received " + change + " " + type + " (balance: " + newBalance + ")");
                        } else if (change < 0) {
                            plugin.sendMsg(player, "<light_purple>►► <red>You have lost <aqua>" + (-change) + " " + typeFormatted);
                            SwaparooPlugin.logMsg("Player " + name + " (" + playerid + ") lost " + (-change) + " " + type + " (balance: " + newBalance + ")");
                        }
                        plugin.sendMsg(player, "<light_purple>►► <green>Your balance is now <aqua>" + newBalance + " " + typeFormatted);
                    }
                });
            }
            return true;
        } else {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Player player = plugin.getServer().getPlayer(playerid);
                String name = player != null ? player.getName() : "unknown";
                SwaparooPlugin.errMsg("Failed to set " + type + " for player " + name + " (" + playerid + ") to " + newBalance);
            });
            return false;
        }
    }
}
