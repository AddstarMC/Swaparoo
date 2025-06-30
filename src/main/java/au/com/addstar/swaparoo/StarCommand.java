package au.com.addstar.swaparoo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StarCommand implements CommandExecutor {
    private final SwaparooPlugin plugin;

    public StarCommand(SwaparooPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if ((args.length == 0) && (sender instanceof Player)) {
            Player player = (Player) sender;
            Integer gems = plugin.getDM().getStarCount(player.getUniqueId(), "stargems", false);
            Integer dust = plugin.getDM().getStarCount(player.getUniqueId(), "stardust", true);
            plugin.sendMsg(sender, "<gradient:#8A2BE2:#00FFFF>★━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━★</gradient>");
            plugin.sendMsg(sender, "<light_purple><bold>   ►►</bold></light_purple> <green>You have</green> <aqua>" + gems + " <yellow>Star<gold>Gems</gold></yellow> <yellow>★</yellow>");
            if (SwaparooPlugin.getConfigs().isDustEnabled())
                plugin.sendMsg(sender, "<light_purple><bold>   ►►</bold></light_purple> <green>You have</green> <aqua>" + dust + " <yellow>Star<white>Dust</white></yellow> <gray>✦</gray>");
            plugin.sendMsg(sender, "<gradient:#8A2BE2:#00FFFF>★━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━★</gradient>");
            return true;
        }
        else if ((args.length > 0) && args[0].equalsIgnoreCase("history") && (sender instanceof Player)) {
            Player player = (Player) sender;
            int page = 1;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                    if (page < 1) page = 1;
                } catch (NumberFormatException ignored) {
                }
            }
            int offset = (page - 1) * 10;
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                var list = plugin.getDM().getTransactions(player.getUniqueId(), offset, 10);
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (list.isEmpty()) {
                        plugin.sendMsg(player, "<yellow>No transactions found.</yellow>");
                        return;
                    }
                    plugin.sendMsg(player, "<yellow>Displaying your StarGems usage history:</yellow>");
                    java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MMM/yyyy HH:mm");
                    for (var t : list) {
                        String date = t.time().toLocalDateTime().format(fmt);
                        StringBuilder msg = new StringBuilder("<light_purple>► </light_purple><gray>" + date + ":</gray> ");
                        String action = t.action();
                        if (action.equalsIgnoreCase("add")) {
                            if (t.stargems() > 0) msg.append("<green>Received <aqua>").append(t.stargems())
                                    .append("</aqua> <yellow>Star<gold>Gems</gold></yellow>");
                            if (t.stardust() > 0) {
                                if (t.stargems() > 0) msg.append(", ");
                                msg.append("<green>Received <aqua>").append(t.stardust())
                                    .append("</aqua> <yellow>Star<white>Dust</white></yellow>");
                            }
                        } else if (action.equalsIgnoreCase("remove")) {
                            if (t.stargems() > 0) msg.append("<red>Spent <aqua>").append(t.stargems())
                                    .append("</aqua> <yellow>Star<gold>Gems</gold></yellow>");
                            if (t.stardust() > 0) {
                                if (t.stargems() > 0) msg.append(", ");
                                msg.append("<red>Spent <aqua>").append(t.stardust())
                                    .append("</aqua> <yellow>Star<white>Dust</white></yellow>");
                            }
                        } else if (action.equalsIgnoreCase("set")) {
                            if (t.stargems() > 0) msg.append("<yellow>Balance set to <aqua>").append(t.stargems())
                                    .append("</aqua> <yellow>Star<gold>Gems</gold></yellow>");
                            if (t.stardust() > 0) {
                                if (t.stargems() > 0) msg.append(", ");
                                msg.append("<yellow>Balance set to <aqua>").append(t.stardust())
                                    .append("</aqua> <yellow>Star<white>Dust</white></yellow>");
                            }
                        } else if (action.equalsIgnoreCase("buy")) {
                            msg.append("<red>Spent <aqua>").append(t.stargems()).append("</aqua>");
                            if (t.packageName() != null) {
                                msg.append(": <white>").append(t.packageName()).append("</white>");
                            }
                        }
                        plugin.sendMsg(player, msg.toString());
                    }
                });
            });
            return true;
        }
        else if ((args.length > 0) && (sender.hasPermission("swaparoo.command.balance.other"))) {
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                plugin.sendMsg(sender, "<red>Player is not online.");
                return true;
            }
            Integer gems = plugin.getDM().getStarCount(target.getUniqueId(), "stargems", false);
            Integer dust = plugin.getDM().getStarCount(target.getUniqueId(), "stardust", true);
            plugin.sendMsg(sender, "<light_purple>►► <green>" + target.getName()
                    + " has <aqua>" + gems + " <yellow>Star<gold>Gems</gold></yellow>"
                    + " and <aqua>" + dust + " <yellow>Star<white>Dust</white></yellow>");
        } else {
            plugin.sendMsg(sender, "<red>Sorry, you do not have access to this command.");
        }
        return true;
    }
}
