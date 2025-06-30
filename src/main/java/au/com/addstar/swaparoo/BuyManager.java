package au.com.addstar.swaparoo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BuyManager {
    private final SwaparooPlugin plugin;
    private final Map<UUID, StoredPurchase> pendingPurchases = new ConcurrentHashMap<>();

    private static class StoredPurchase {
        private final int cost;
        private final String packageId;
        private final String packageName;
        private final String params;
        private final int expiry;
        private final String playername;

        public StoredPurchase(String playername, int cost, String packageId, String packageName, String params) {
            this.cost = cost;
            this.packageId = packageId;
            this.packageName = packageName;
            this.params = params;
            this.playername = playername;
            this.expiry = (int) (System.currentTimeMillis() / 1000) + 30;
        }

        public int getCost() {
            return cost;
        }

        public String getPackageId() {
            return packageId;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getParams() {
            return params;
        }

        public String getPlayerName() {
            return playername;
        }
    }

    public BuyManager(SwaparooPlugin plugin) {
        this.plugin = plugin;

        // Run a task every 5 seconds to remove expired commands
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::removeExpiredPurchases, 100, 100);
    }

    public void buyPackage(Player buyer, int cost, String packageid, String packagename, String params) {
        StoredPurchase storedCommand = new StoredPurchase(buyer.getName(), cost, packageid, packagename, params);
        pendingPurchases.put(buyer.getUniqueId(), storedCommand);

        SwaparooPlugin.debugMsg("=== Command to have been purchased ===");
        SwaparooPlugin.debugMsg("Player " + buyer.getName());
        SwaparooPlugin.debugMsg("Gem cost: " + cost);
        SwaparooPlugin.debugMsg("Package ID: '" + packageid + "'");
        SwaparooPlugin.debugMsg("Package Name: '" + packagename + "'");
        SwaparooPlugin.debugMsg("Package Params: " + params);

        String link = "<hover:show_text:\"<green>√</green> <yellow>Click to purchase</yellow>\"><click:run_command:/buyconfirm><bold><light_purple>►►<gold><underlined>CLICK HERE</underlined></gold>◄◄</light_purple></bold></click></hover>";
        plugin.sendMsg(buyer, "<gradient:#ffbb00:#ff0f1f>★━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━★</gradient>");
        plugin.sendMsg(buyer, "<yellow>  You are about to purchase a package:</yellow>");
        plugin.sendMsg(buyer, "<green>     Package ID: <aqua>" + packageid + "</aqua></green>");
        plugin.sendMsg(buyer, "<green>     Package Name: <aqua>" + packagename + "</aqua></green>");
        plugin.sendMsg(buyer, "<green>     Purchase Cost: <aqua>" + cost + " <yellow>Star<gold>Gems</aqua></green><newline>");
        plugin.sendMsg(buyer, "<green>   " + link + " <yellow>to confirm your purchase.</green>");
        plugin.sendMsg(buyer, "<gradient:#ffbb00:#ff0f1f>★━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━★</gradient>");
        return;
    }

    public void confirmPurchase(Player buyer) {
        StoredPurchase storedCommand = pendingPurchases.get(buyer.getUniqueId());
        if (storedCommand == null) {
            plugin.sendMsg(buyer, "<red>There is nothing pending to purchase.");
            return;
        }

        // Always remove the pending purchase (if it fails later)
        pendingPurchases.remove(buyer.getUniqueId());

        // Check if the player has enough gems
        int cost = storedCommand.getCost();
        if (plugin.getDM().getStarCount(buyer.getUniqueId(), "stargems", false) < cost) {
            plugin.sendMsg(buyer, "<red>You do not have enough <yellow>Star<gold>Gems</gold></yellow> to purchase this.");
            return;
        }

        // Take the gems from the player and run the command
        String packId = storedCommand.getPackageId();
        String packName = storedCommand.getPackageName();
        String params = storedCommand.getParams();
        if (plugin.getSM().takeStars(buyer.getUniqueId(), "stargems", cost, false)) {
            String cmd = "runalias srv_starshop " + buyer.getName() + " " + packId + " " + params;
            SwaparooPlugin.debugMsg("Executing command: " + cmd);
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
            plugin.getDM().recordTransaction(buyer.getUniqueId(), "buy", cost, 0, packId, packName);
            plugin.sendMsg(buyer, "<dark_grey>[<green><bold>√</bold><dark_grey>]</dark_grey> You have successfully purchased the package.");
        }
    }

    private void removeExpiredPurchases() {
        // Remove any expired pending purchases and also notify the player
        pendingPurchases.forEach((uuid, storedCommand) -> {
            if (storedCommand.expiry < (int) (System.currentTimeMillis() / 1000)) {
                SwaparooPlugin.logMsg("Expiring pending StarGems purchase for " + storedCommand.getPlayerName());
                pendingPurchases.remove(uuid);
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    plugin.sendMsg(player, "<dark_grey>[<red><bold>x</bold><dark_grey>]</dark_grey> Your purchase has timed out.");
                }
            }
        });
    }
}
