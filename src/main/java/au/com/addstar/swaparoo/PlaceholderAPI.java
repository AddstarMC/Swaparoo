package au.com.addstar.swaparoo;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player; // Added for inventory slot checks
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceholderAPI extends PlaceholderExpansion {
    SwaparooPlugin plugin;
    public PlaceholderAPI(SwaparooPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    // Simple 1s cache for msg: placeholder results (keyed per-player + exact spec string)
    private static class CacheEntry {
        final String value; final long expiresAt;
        CacheEntry(String value, long expiresAt) { this.value = value; this.expiresAt = expiresAt; }
    }
    private final Map<String, CacheEntry> msgCache = new ConcurrentHashMap<>();
    private int msgCacheAccesses = 0; // opportunistic cleanup counter

    @Override
    public @NotNull String getIdentifier() {
        // Placeholder identifiers in PlaceholderAPI are case-sensitive and
        // conventionally lowercase. The placeholders documented in the README
        // use the lowercase prefix "swaparoo", so return that value to ensure
        // the expansion is correctly recognised by PlaceholderAPI.
        return "swaparoo";
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

    /**
     * Scans permission nodes of the form permPrefix+<number> and returns the highest granted value.
     * Parameters:
     *   player     - the player whose permissions to inspect
     *   permPrefix - the base permission prefix (e.g. "creativeplots.") including trailing dot
     *   max        - the maximum numeric value to consider (inclusive)
     *   step       - increment/decrement step size when iterating values (> 0). If invalid (<=0) defaults to 1.
     *   ascending  - if true, iterate from 0..max accumulating the highest granted value; if false, iterate
     *                from max..0 and return immediately on the first (thus highest) match for a minor performance gain.
     * Returns:
     *   The highest value for which player.hasPermission(permPrefix + value) is true; 0 if none matched.
     * Notes:
     *   - Use ascending=true when you might have sparse higher nodes and want to ensure checking all values;
     *     use ascending=false for a fast exit when higher nodes are more commonly granted.
     */
    private int scanPermissionLimit(Player player, String permPrefix, int max, int step, boolean ascending) {
        if (player == null) return 0;
        if (step <= 0) step = 1;
        if (ascending) {
            int found = 0;
            for (int v = 0; v <= max; v += step) {
                if (player.hasPermission(permPrefix + v)) {
                    found = v;
                }
            }
            return found;
        } else {
            for (int v = max; v >= 0; v -= step) {
                if (player.hasPermission(permPrefix + v)) {
                    return v;
                }
            }
            return 0;
        }
    }

    private int getHighestPermissionValue(Player player, String permPrefix, int max, int step) {
        return scanPermissionLimit(player, permPrefix, max, step, false);
    }

    // Remove old validatePermLessThan / validatePermMoreThan and replace with unified comparison.
    private enum PermCompareMode { LESS_THAN, MORE_THAN }

    /**
     * Generic permission comparison.
     * part must start with tokenPrefix followed immediately by a positive integer threshold.
     * LESS_THAN: fail if player has any permission value >= threshold.
     * MORE_THAN: fail if player lacks all permission values > threshold.
     * Returns null on success else a failure message or error code.
     */
    private String validatePermComparison(OfflinePlayer offline, String part, String tokenPrefix,
                                          String permPrefix, int max, int step,
                                          String invalidCode, String label, PermCompareMode mode) {
        if (!part.startsWith(tokenPrefix)) return "INTERNAL_MISMATCH";
        if (!(offline instanceof Player)) return "PLAYER_OFFLINE";
        String raw = part.substring(tokenPrefix.length());
        int threshold;
        try {
            threshold = Integer.parseInt(raw);
            if (threshold < 0) return invalidCode;
        } catch (NumberFormatException e) {
            return invalidCode;
        }
        Player p = (Player) offline;
        if (mode == PermCompareMode.LESS_THAN) {
            // Fail if any permission >= threshold
            for (int v = max; v >= threshold; v -= step) {
                if (p.hasPermission(permPrefix + v)) {
                    int highest = getHighestPermissionValue(p, permPrefix, max, step);
                    return "Sorry, you already have " + highest + "x " + label + ".";
                }
            }
            return null; // success
        } else { // MORE_THAN
            // Success if any permission > threshold
            for (int v = max; v > threshold; v -= step) {
                if (p.hasPermission(permPrefix + v)) {
                    return null; // success
                }
            }
            int highest = getHighestPermissionValue(p, permPrefix, max, step);
            return "Sorry, you only have " + highest + "x " + label + ".";
        }
    }

    /** Counts free (null or air) slots in player's storage contents. */
    private int countFreeSlots(Player player) {
        int free = 0;
        for (int i = 0; i < player.getInventory().getStorageContents().length; i++) {
            if (player.getInventory().getStorageContents()[i] == null || player.getInventory().getStorageContents()[i].getType().isAir()) {
                free++;
            }
        }
        return free;
    }

    private Integer parseNonNegativeInt(String raw) {
        try {
            int v = Integer.parseInt(raw);
            return v >= 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String evaluateMsgSpec(final OfflinePlayer player, final String spec) {
        if (spec.isEmpty()) {
            return "INVALID_FORMAT";
        }
        String[] parts = spec.split(":");
        for (String part : parts) {
            if (part.isEmpty()) continue;

            if (part.startsWith("cost=")) {
                Integer cost = parseNonNegativeInt(part.substring("cost=".length()));
                if (cost == null) return "INVALID_COST";
                int playerGems = plugin.getDM().getStarCount(player.getUniqueId(), "stargems", true);
                if (playerGems < cost) return "Sorry, you only have " + playerGems + " StarGems";
                continue;
            }
            if (part.startsWith("server=")) {
                String requiredServer = part.substring("server=".length());
                if (requiredServer.isEmpty()) return "INVALID_SERVER";
                if (!requiredServer.equalsIgnoreCase(plugin.getServerName())) return "You must be in " + requiredServer + " server to purchase";
                continue;
            }
            if (part.startsWith("inv=")) {
                Integer needed = parseNonNegativeInt(part.substring("inv=".length()));
                if (needed == null) return "INVALID_INV";
                if (!(player instanceof Player)) return "PLAYER_OFFLINE";
                int free = countFreeSlots((Player) player);
                if (free < needed) return "You need at least " + needed + " free inventory slots";
                continue;
            }
            if (part.startsWith("creativeplots<")) {
                String error = validatePermComparison(player, part, "creativeplots<", "creativeplots.", 50, 1, "INVALID_CREATIVEPLOTS", "creative plots", PermCompareMode.LESS_THAN);
                if (error != null) { if (error.equals("INTERNAL_MISMATCH")) return "INVALID_FORMAT"; return error; }
                continue;
            }
            if (part.startsWith("creativeplots>")) {
                String error = validatePermComparison(player, part, "creativeplots>", "creativeplots.", 50, 1, "INVALID_CREATIVEPLOTS", "creative plots", PermCompareMode.MORE_THAN);
                if (error != null) { if (error.equals("INTERNAL_MISMATCH")) return "INVALID_FORMAT"; return error; }
                continue;
            }
            if (part.startsWith("homes<")) {
                String error = validatePermComparison(player, part, "homes<", "gesuit.homes.limits.", 100, 5, "INVALID_HOMES", "homes", PermCompareMode.LESS_THAN);
                if (error != null) { if (error.equals("INTERNAL_MISMATCH")) return "INVALID_FORMAT"; return error; }
                continue;
            }
            if (part.startsWith("vaults<")) {
                String error = validatePermComparison(player, part, "vaults<", "playervaults.amount.", 10, 1, "INVALID_VAULTS", "vaults", PermCompareMode.LESS_THAN);
                if (error != null) { if (error.equals("INTERNAL_MISMATCH")) return "INVALID_FORMAT"; return error; }
                continue;
            }
            if (part.startsWith("cparkour<")) {
                String error = validatePermComparison(player, part, "cparkour<", "creativeparkour.create.", 20, 1, "INVALID_CPARKOUR", "community parkour plots", PermCompareMode.LESS_THAN);
                if (error != null) { if (error.equals("INTERNAL_MISMATCH")) return "INVALID_FORMAT"; return error; }
                continue;
            }
            return "INVALID_KEY";
        }
        return "OK";
    }

    // -------------------------------------------------------------------

    @Override
    public String onRequest(final OfflinePlayer player, final @NotNull String params) {
        if ((player == null) || (!plugin.isEnabled())) {
            return "";
        }

        if (params.startsWith("msg:")) {
            String spec = params.substring(4);
            long now = System.currentTimeMillis();
            String cacheKey = (player instanceof Player ? ((Player) player).getUniqueId() : player.getUniqueId()) + "|" + spec;
            CacheEntry entry = msgCache.get(cacheKey);
            if (entry != null && entry.expiresAt > now) {
                return entry.value;
            }
            String result = evaluateMsgSpec(player, spec);
            msgCache.put(cacheKey, new CacheEntry(result, now + 1000)); // revert TTL to 1s per original requirement
            // Opportunistic cleanup every 100 accesses
            if (++msgCacheAccesses % 100 == 0) {
                long cutoff = System.currentTimeMillis();
                msgCache.entrySet().removeIf(e -> e.getValue().expiresAt <= cutoff);
            }
            return result;
        }
        else if (params.startsWith("keys_") && params.length() > 5) {
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
        else if (params.startsWith("msg_")) {
            // Legacy placeholder: msg_<server>_<cost>
            // Extract server name and stargems cost from name
            String[] legacyParts = params.split("_");
            if (legacyParts.length != 3) {
                return "INVALID_FORMAT";
            }
            String requiredServer = legacyParts[1];
            if (!requiredServer.equalsIgnoreCase(plugin.getServerName())) {
                return "You must be in " + requiredServer + " server to purchase";
            }
            Integer cost = parseNonNegativeInt(legacyParts[2]);
            if (cost == null) return "INVALID_COST";
            int playerGems = plugin.getDM().getStarCount(player.getUniqueId(), "stargems", true);
            if (playerGems < cost) {
                return "Sorry, you only have " + playerGems + " StarGems";
            }
            return "OK";
        }
        else if (params.startsWith("gemcost_") && params.length() > 8) {
            String val = params.substring(8);
            Integer cost = parseNonNegativeInt(val);
            if (cost == null) return "~ERROR_INVALID_COST~";
            return "&e&lCost: &b" + cost + " &eStar&6Gems";
        }

        return "";
    }
}
