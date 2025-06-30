package au.com.addstar.swaparoo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DataManager {
    private final SwaparooPlugin plugin;
    private final StarGemsDB starGemsDB;
    private final TreasuresDB treasuresDB;
    private final PlayerKVCacheManager keyCacheManager = new PlayerKVCacheManager(750, TimeUnit.MILLISECONDS);
    private final PlayerKVCacheManager starCacheManager = new PlayerKVCacheManager(750, TimeUnit.MILLISECONDS);
    private final String[] treasureKeys = {"stone", "iron", "gold", "diamond", "emerald"};
    private final String[] starTypes = {"stargems", "stardust"};

    private void createTransactionsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                "ID INT AUTO_INCREMENT PRIMARY KEY," +
                "PLAYER_UUID VARCHAR(36)," +
                "TYPE VARCHAR(10)," +
                "STARGEMS INT DEFAULT 0," +
                "STARDUST INT DEFAULT 0," +
                "PACKAGE_ID VARCHAR(64)," +
                "PACKAGE_NAME VARCHAR(64)," +
                "TRANSACTION_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        starGemsDB.executeUpdate(sql);
    }

    public void recordTransaction(UUID playerid, String type, int gems, int dust, String packageId, String packageName) {
        String query = "INSERT INTO transactions (PLAYER_UUID, TYPE, STARGEMS, STARDUST, PACKAGE_ID, PACKAGE_NAME) VALUES (?,?,?,?,?,?)";
        try {
            starGemsDB.executeUpdate(query, playerid.toString(), type, gems, dust, packageId, packageName);
        } catch (SQLException e) {
            SwaparooPlugin.errMsg("StarGemsDB: Failed to record transaction for player " + playerid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public DataManager(SwaparooPlugin plugin) {
        this.plugin = plugin;
        starGemsDB = new StarGemsDB(plugin);
        treasuresDB = new TreasuresDB(plugin);
    }

    public void initialise() throws SQLException {
        // Initialise the database connection pools
        starGemsDB.initializePool();
        treasuresDB.initializePool();
        createTransactionsTable();
    }

    public void close() {
        starGemsDB.closePool();
        treasuresDB.closePool();
    }

    // Clear the cache for a player
    public void clearCache(UUID playerid) {
        keyCacheManager.invalidateCache(playerid);
        starCacheManager.invalidateCache(playerid);
    }

    /**
     * Get the treasure count for a player and key combination.
     *
     * @param playerid UUID of the player
     * @param key the treasure key to increment
     * @return the new treasure count for the key
     */
    public Integer getTreasureCount(UUID playerid, String key, boolean useCache) {
        key = key.toLowerCase();

        // If requested, check the cache and return the key if it exists
        if (useCache) {
            Map<String, Integer> keys = keyCacheManager.getPlayerCounts(playerid);
            if (keys != null && keys.containsKey(key)) {
                return keys.get(key);
            }
        }

        // Fetch the key count from the database and update the cache
        try {
            try (Connection conn = treasuresDB.getConnection();
                 ResultSet result = treasuresDB.executeQuery(conn, "SELECT * FROM Treasures WHERE PLAYER=? LIMIT 1", playerid.toString())) {
                if (result.next()) {
                    try {
                        // Build a key count map from the result set
                        Map<String, Integer> keyCounts = new HashMap<>();
                        for (String k : treasureKeys) {
                            keyCounts.put(k, result.getInt(k));
                        }
                        // Store the key count in the cache
                        keyCacheManager.setPlayerCounts(playerid, keyCounts);
                        return keyCounts.get(key);
                    } catch (SQLException e) {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            SwaparooPlugin.errMsg("TreasuresDB: Failed to get treasure count for player " + playerid + " and key " + key + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Increments the treasure count for a player and key by the specified amount.
     *
     * @param playerid UUID of the player
     * @param key the treasure key to increment
     * @param amount the amount to increment by
     * @return the new treasure count for the key
     */
    public Integer incTreasureCount(UUID playerid, String key, int amount) {
        try {
            treasuresDB.executeUpdate("UPDATE Treasures SET " + key + " = " + key + " + " + amount + " WHERE PLAYER=?", playerid.toString());
            return getTreasureCount(playerid, key, false);
        } catch (SQLException e) {
            SwaparooPlugin.errMsg("TreasuresDB: Failed to get treasure count for player " + playerid + " and key " + key + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the StarGem count for a player.
     *
     * @param playerid UUID of the player
     * @param startype the treasure key to increment
     * @return the new treasure count for the key
     */
    public Integer getStarCount(UUID playerid, String startype, boolean useCache) {
        // If requested, check the cache and return the key if it exists
        startype = startype.toLowerCase();
        if (useCache) {
            Map<String, Integer> types = starCacheManager.getPlayerCounts(playerid);
            if (types != null && types.containsKey(startype)) {
                return types.get(startype);
            }
        }

        // Fetch the gem/dust count from the database and update the cache
        try {
            try (Connection conn = starGemsDB.getConnection();
                ResultSet result = starGemsDB.executeQuery(conn, "SELECT * FROM players WHERE PLAYER_UUID=? LIMIT 1", playerid.toString())) {
                if (result.next()) {
                    try {
                        // Build a key count map from the result set
                        Map<String, Integer> starCounts = new HashMap<>();
                        for (String k : starTypes) {
                            starCounts.put(k, result.getInt(k));
                        }
                        // Store the gem/dust count in the cache
                        starCacheManager.setPlayerCounts(playerid, starCounts);
                        return starCounts.get(startype);
                    } catch (SQLException e) {
                        SwaparooPlugin.errMsg("StarGemsDB: Failed to process " + startype + " record for player " + playerid + ": " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            SwaparooPlugin.errMsg("StarGemsDB: Failed to get " + startype + " count for player " + playerid + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Set the StarGems/StarDust balance for a player by the specified amount.
     *
     * @param playerid UUID of the player
     * @param amount the amount to set the balance to
     * @return the new balance for the player
     */
    public boolean setStarCount(UUID playerid, String startype, int amount) {
        String query = "INSERT INTO players (PLAYER_UUID, " + startype + ") VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE " + startype + "=?";
        try {
            starGemsDB.executeUpdate(query, playerid.toString(), amount, amount);
            return true;
        } catch (SQLException e) {
            SwaparooPlugin.errMsg("StarGemsDB: Failed to get " + startype + " count for player " + playerid + ": " + e.getMessage());
            SwaparooPlugin.errMsg("Query: " + query);
            e.printStackTrace();
        }
        return false;
    }
}
