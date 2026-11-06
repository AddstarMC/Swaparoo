package au.com.addstar.swaparoo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ConvertCommand implements CommandExecutor {
    private final SwaparooPlugin plugin;

    public ConvertCommand(SwaparooPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!sender.hasPermission("swaparoo.command.usconvert")) {
            plugin.sendMsg(sender,"<red>You do not have permission to use this command.");
            return true;
        }
        if (!(sender instanceof Player)) {
            plugin.sendMsg(sender,"<red>This command can only be run by a player.");
            return true;
        }

        plugin.getCM().openGui((Player) sender);
        return true;
    }
}
