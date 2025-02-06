package au.com.addstar.swaparoo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PlayerKVCacheManager {
    // Inner class to hold the counts and their expiry
    private static class CacheEntry {
        private final Map<String, Integer> mapCounts;
        private final long expiryTime;

        public CacheEntry(Map<String, Integer> counts, long expiryTime) {
            this.mapCounts = counts;
            this.expiryTime = expiryTime;
        }

        public Map<String, Integer> getMapCounts() {
            return mapCounts;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final Map<UUID, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long cacheDurationMillis; // Cache duration in milliseconds

    public PlayerKVCacheManager(long cacheDuration, TimeUnit timeUnit) {
        this.cacheDurationMillis = timeUnit.toMillis(cacheDuration);
    }

    /**
     * Fetches the counts for a player
     *
     * @param playerId the unique ID of the player
     * @return a map of strings and their counts if found, otherwise null
     */
    public Map<String, Integer> getPlayerCounts(UUID playerId) {
        // Check the cache
        CacheEntry entry = cache.get(playerId);
        if (entry != null && !entry.isExpired()) {
            return entry.getMapCounts();
        }
        return null;
    }

    /**
     * Store the counts for a player
     *
     * @param playerId the unique ID of the player
     */
    public void setPlayerCounts(UUID playerId, Map<String, Integer> counts) {
        cache.put(playerId, new CacheEntry(counts, System.currentTimeMillis() + cacheDurationMillis));
    }

    /**
     * Removes a player's entry from the cache.
     *
     * @param playerId the unique ID of the player
     */
    public void invalidateCache(UUID playerId) {
        cache.remove(playerId);
    }

    /**
     * Clears the entire cache.
     */
    public void clearCache() {
        cache.clear();
    }
}
