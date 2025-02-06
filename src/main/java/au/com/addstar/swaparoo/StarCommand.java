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
            plugin.sendMsg(sender, "<light_purple>═════════════════════════</light_purple>");
            plugin.sendMsg(sender, "<light_purple>►► <green>You have <aqua>" + gems + " <yellow>Star<gold>Gems</gold></yellow>");
            plugin.sendMsg(sender, "<light_purple>►► <green>You have <aqua>" + dust + " <yellow>Star<white>Dust</white></yellow>");
            plugin.sendMsg(sender, "<light_purple>═════════════════════════</light_purple>");
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
