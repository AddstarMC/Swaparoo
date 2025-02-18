package au.com.addstar.swaparoo;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPI extends PlaceholderExpansion {
    SwaparooPlugin plugin;
    public PlaceholderAPI(SwaparooPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "Swaparoo";
    }

    @Override
    public @NotNull String getAuthor() {
        return "AddstarMC";
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(final OfflinePlayer player, final @NotNull String params) {
        if ((player == null) || (!plugin.isEnabled())) {
            return "";
        }

        if (params.startsWith("keys_") && params.length() > 5) {
            // Extract the keu name from the params (remove the prefix)
            String key = params.substring(5);
            Integer count = plugin.getDM().getTreasureCount(player.getUniqueId(), key, true);
            //SwaparooPlugin.debugMsg("PlaceholderAPI: onRequest for player " + player.getName() + " and Treasures key " + key + " = " + count);
            return count != null ? String.valueOf(count) : "";
        }
        else if (params.equalsIgnoreCase("stargems") || params.equalsIgnoreCase("stardust")) {
            Integer count = plugin.getDM().getStarCount(player.getUniqueId(), params.toLowerCase(), true);
            //SwaparooPlugin.debugMsg("PlaceholderAPI: onRequest for player " + player.getName() + " and " + params.toLowerCase() + " = " + count);
            return count != null ? String.valueOf(count) : "0";
        }
        else if (params.equalsIgnoreCase("server")) {
            return plugin.getServerName();
        }
        else if (params.startsWith("gemcost_") && params.length() > 8) {
            String val = params.substring(8);
            try {
                int cost = Integer.parseInt(val);
                return "&e&lCost: &b" + cost + " &eStar&6Gems";
            } catch (Exception e) {
                return "~ERROR_INVALID_COST~";
            }
        }

        return "";
    }
}
