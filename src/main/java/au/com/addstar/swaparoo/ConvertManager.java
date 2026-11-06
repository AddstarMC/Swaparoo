package au.com.addstar.swaparoo;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;
import java.util.UUID;

public class ConvertManager {
    public static final int MIN_BALANCE = 100;
    private static final String STARGEMS = "<bold><yellow>Star<gold>Gems</gold></yellow></bold>";
    private static final String MSG_UNAVAILABLE = "<red>Point conversion is unavailable right now. Please try again later.";
    private static final String MSG_OFF = "<red>Point conversion is currently turned off.";

    private final SwaparooPlugin plugin;
    private volatile boolean warnedTierOverflow = false;

    public record ConvertData(List<DataManager.ConversionTier> tiers, DataManager.UnscramblePlayer stats, int gems) {
        /** The player's tier, or null if they haven't reached Level 1. */
        public DataManager.ConversionTier currentTier() {
            DataManager.ConversionTier current = null;
            for (DataManager.ConversionTier tier : tiers) {
                if (tier.minTotalPoints() <= stats.totalPoints()) {
                    current = tier;
                }
            }
            return current;
        }

        /** The next tier above the player, or null if they're at the top. */
        public DataManager.ConversionTier nextTier() {
            for (DataManager.ConversionTier tier : tiers) {
                if (tier.minTotalPoints() > stats.totalPoints()) {
                    return tier;
                }
            }
            return null;
        }

        /** The balance needed to convert at the given tier, which always buys at least one gem. */
        public int requiredPoints(DataManager.ConversionTier tier) {
            return Math.max(MIN_BALANCE, tier.pointsPerGem());
        }

        public boolean canConvert() {
            DataManager.ConversionTier tier = currentTier();
            return tier != null && stats.points() >= requiredPoints(tier);
        }

        public int gemsToReceive() {
            return stats.points() / currentTier().pointsPerGem();
        }

        public int remainder() {
            return stats.points() % currentTier().pointsPerGem();
        }
    }

    public ConvertManager(SwaparooPlugin plugin) {
        this.plugin = plugin;
    }

    public void openGui(Player player) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            ConvertData data = load(player.getUniqueId());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (player.isOnline() && isAvailable(player, data)) {
                    player.openInventory(new ConvertGui(plugin, player, data).getInventory());
                }
            });
        });
    }

    public void handleConvertClick(Player player, ConvertGui gui) {
        if (gui.getData().canConvert()) {
            switchMenu(player, gui, new ConvertConfirmGui(plugin, gui));
        }
    }

    public void handleConfirmClick(Player player, ConvertConfirmGui gui) {
        if (!gui.isBusy()) {
            gui.setBusy();
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> convert(player, gui));
        }
    }

    public void handleCancelClick(Player player, ConvertConfirmGui gui) {
        if (!gui.isBusy()) {
            gui.setBusy();
            switchMenu(player, gui, gui.getParent());
        }
    }

    // Menus can't safely be changed during a click event, so this waits a tick
    private void switchMenu(Player player, InventoryHolder from, InventoryHolder to) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (isViewing(player, from)) {
                player.openInventory(to.getInventory());
            }
        });
    }

    // Runs off the main thread
    private void convert(Player player, ConvertConfirmGui gui) {
        UUID playerid = player.getUniqueId();

        // Re-read so that a reload or a win since the GUI opened takes effect
        ConvertData data = load(playerid);
        if (data == null || !data.canConvert()) {
            showData(player, gui, data);
            return;
        }

        int points = data.stats().points();
        int rate = data.currentTier().pointsPerGem();
        int gems = data.gemsToReceive();
        int remainder = data.remainder();

        // Take the points first. If a failure stops us here the player keeps their points,
        // and a failure after this can be fixed from the log without ever creating free gems.
        if (!plugin.getDM().setUnscramblePoints(playerid, points, remainder)) {
            showData(player, gui, load(playerid));
            return;
        }

        if (!plugin.getDM().addStarCount(playerid, "stargems", gems)) {
            SwaparooPlugin.errMsg("Unscramble conversion failed for " + player.getName() + " (" + playerid + "): took "
                    + (points - remainder) + " points (balance was " + points + ", now " + remainder + ") but could not give "
                    + gems + " stargems. Give the gems manually.");
            finish(player, gui, "<red>Something went wrong giving your " + STARGEMS + "<red>. Please contact staff so they can fix this for you.");
            return;
        }

        plugin.getDM().recordTransaction(playerid, "convert", gems, 0, "unscramble", "Unscramble points");
        SwaparooPlugin.logMsg("Player " + player.getName() + " (" + playerid + ") converted " + (points - remainder)
                + " Unscramble points to " + gems + " stargems at " + rate + " per gem (balance was " + points + ", now " + remainder + ")");
        finish(player, gui,
                "<light_purple>►► <green>Converted <aqua>" + (points - remainder) + " <green>Unscramble points",
                "<light_purple>►► <green>You received <aqua>" + gems + " " + STARGEMS,
                "<light_purple>►► <green>Unscramble points remaining: <aqua>" + remainder,
                "<light_purple>►► <green>Your balance is now <aqua>" + (data.gems() + gems) + " " + STARGEMS);
    }

    private ConvertData load(UUID playerid) {
        List<DataManager.ConversionTier> tiers = plugin.getDM().getConversionTiers();
        DataManager.UnscramblePlayer stats = plugin.getDM().getUnscramblePlayer(playerid);
        if (tiers == null || stats == null) {
            return null;
        }
        if (tiers.size() > ConvertGui.LEVEL_COUNT && !warnedTierOverflow) {
            warnedTierOverflow = true;
            plugin.getLogger().warning("Unscramble has " + tiers.size() + " conversion tiers but the GUI only shows the first " + ConvertGui.LEVEL_COUNT);
        }
        Integer gems = plugin.getDM().getStarCount(playerid, "stargems", false);
        return new ConvertData(tiers, stats, gems != null ? gems : 0);
    }

    private boolean isAvailable(Player player, ConvertData data) {
        if (data == null) {
            plugin.sendMsg(player, MSG_UNAVAILABLE);
            return false;
        }
        if (data.tiers().isEmpty()) {
            plugin.sendMsg(player, MSG_OFF);
            return false;
        }
        return true;
    }

    // Goes back to the main menu with fresh data, without converting anything
    private void showData(Player player, ConvertConfirmGui gui, ConvertData data) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!isViewing(player, gui)) {
                return;
            }
            if (isAvailable(player, data)) {
                player.openInventory(new ConvertGui(plugin, player, data).getInventory());
            } else {
                player.closeInventory();
            }
        });
    }

    private void finish(Player player, ConvertConfirmGui gui, String... messages) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (isViewing(player, gui)) {
                player.closeInventory();
            }
            if (player.isOnline()) {
                for (String message : messages) {
                    plugin.sendMsg(player, message);
                }
            }
        });
    }

    private boolean isViewing(Player player, InventoryHolder menu) {
        return player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() == menu;
    }
}
