package au.com.addstar.swaparoo;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwaparooCommand implements CommandExecutor, TabCompleter {
    private final SwaparooPlugin plugin;

    public SwaparooCommand(SwaparooPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            plugin.sendMsg(sender,"<red>Usage: /swaparoo <reload|debug|keys|stargems|stardust> ...");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("swaparoo.command.reload")) {
                    plugin.sendMsg(sender,"<red>You do not have permission to use this command.");
                    return true;
                }
                SwaparooPlugin.getConfigs().reload();
                plugin.sendMsg(sender,"<yellow>Swaparoo configuration reloaded.");
                return true;
            case "debug":
                if (!sender.hasPermission("swaparoo.command.debug")) {
                    plugin.sendMsg(sender,"<red>You do not have permission to use this command.");
                    return true;
                }
                SwaparooPlugin.getConfigs().setDebug(!SwaparooPlugin.getConfigs().getDebug());
                plugin.sendMsg(sender, "<yellow>Swaparoo debug mode set to " + SwaparooPlugin.getConfigs().getDebug());
                return true;
            case "keys":
                if (!sender.hasPermission("swaparoo.command.keys")) {
                    plugin.sendMsg(sender,"<red>You do not have permission to use this command.");
                    return true;
                }
                return true;
            case "stargems":
            case "stardust":
            case "gems":
            case "dust":
                if (!sender.hasPermission("swaparoo.command.stargems")) {
                    plugin.sendMsg(sender,"<red>You do not have permission to use this command.");
                    return true;
                }
                String startype = subCommand.equals("gems") ? "stargems" : subCommand.equals("dust") ? "stardust" : subCommand;
                if (args.length == 4) {
                    String action = args[1].toLowerCase();
                    int amount = Integer.parseInt(args[3]);

                    // Check if the amount is a positive number
                    if (amount < 0) {
                        plugin.sendMsg(sender,"<red>Amount must be a positive number.");
                        return true;
                    }
                    // Check if the player is online
                    Player player = Bukkit.getPlayer(args[2]);
                    if (player == null) {
                        plugin.sendMsg(sender,"<red>Player " + args[2] + " is not online.");
                        return true;
                    }

                    switch (action) {
                        case "add" -> {
                            // Add stardust to the player
                            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                                plugin.getSM().giveStars(player.getUniqueId(), startype, amount, true));
                            return true;
                        }
                        case "remove" -> {
                            // Remove stardust from the player
                            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                                    plugin.getSM().takeStars(player.getUniqueId(), startype, amount, true));
                            return true;
                        }
                        case "set" -> {
                            // Set the stardust for the player
                            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                                    plugin.getSM().setStars(player.getUniqueId(), startype, amount, true));
                            return true;
                        }
                    };
                }

                // Give command help if the arguments weren't correct
                plugin.sendMsg(sender,"<red>Usage: /swaparoo " + startype + " <add|remove|set> <player> <amount>");
                return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "debug", "keys", "stargems", "stardust", "gems", "dust"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("stargems") || args[0].equalsIgnoreCase("stardust") || args[0].equalsIgnoreCase("gems") || args[0].equalsIgnoreCase("dust"))) {
            completions.addAll(Arrays.asList("add", "remove", "set"));
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("stargems") || args[0].equalsIgnoreCase("stardust") || args[0].equalsIgnoreCase("gems") || args[0].equalsIgnoreCase("dust"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }
        return completions;
    }
}
