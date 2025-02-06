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
        Player player = plugin.getServer().getPlayer(playerid);
        String name = player != null ? player.getName() : "unknown";
        if (dataManager.setStarCount(playerid, type, amount)) {
            SwaparooPlugin.debugMsg("Player " + name + " (" + playerid + ") now has " + amount + " " + type);

            if (notifyPlayer) {
                Integer starCount = dataManager.getStarCount(playerid, type, false);
                String typeFormatted = type.contains("gems") ? "<yellow>Star<gold>Gems</gold></yellow>" : "<yellow>Star<white>Dust</white></yellow>";
                if (player != null) {
                    plugin.sendMsg(player, "<light_purple>►► <green>You now have <aqua>" + starCount + " " + typeFormatted);
                }
            }
            return true;
        } else {
            SwaparooPlugin.errMsg("Failed to set " + type + " for player " + name + " (" + playerid + ") to " + amount);
            return false;
        }
    }
}
