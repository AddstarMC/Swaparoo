package au.com.addstar.swaparoo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final SwaparooPlugin plugin;

    public PlayerListener(SwaparooPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //if (!plugin.getDM().hasPlayer(event.getPlayer().getUniqueId())) {
        //    plugin.getDM().createPlayer(event.getPlayer().getUniqueId());
        //}
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove the player from the cache
        SwaparooPlugin.debugMsg("PlayerListener: Clearing cache for " + event.getPlayer().getName());
        plugin.getDM().clearCache(event.getPlayer().getUniqueId());
    }
}
