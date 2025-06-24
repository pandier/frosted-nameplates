package io.github.pandier.frostednameplates;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FnpCommand implements CommandExecutor, TabCompleter {
    private static final String PRIMARY_COLOR = "x5f9fe8".chars().mapToObj(x -> "" + ChatColor.COLOR_CHAR + (char) x).collect(Collectors.joining());
    private static final String SECONDARY_COLOR = "x1c70ed".chars().mapToObj(x -> "" + ChatColor.COLOR_CHAR + (char) x).collect(Collectors.joining());
    private static final String ICON = "❄";
    private static final String INFO_PREFIX = PRIMARY_COLOR;

    private final FrostedNameplates plugin;

    public FnpCommand(@NotNull FrostedNameplates plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            help(sender, label);
            return true;
        }

        final String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (subcommand.equals("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(INFO_PREFIX + "Successfully reloaded the plugin's configuration");
            return true;
        }

        help(sender, label);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            final String arg = args[0].toLowerCase(Locale.ROOT);
            return Stream.of("reload").filter(subcommand -> subcommand.startsWith(arg)).toList();
        }
        return List.of();
    }

    private void help(@NotNull CommandSender sender, @NotNull String label) {
        final String message = "\n " +
                PRIMARY_COLOR + ICON + " Frosted " + SECONDARY_COLOR + "Nameplates " + ChatColor.GRAY + "v" + plugin.getDescription().getVersion() +
                "\n by " + String.join(", ", plugin.getDescription().getAuthors()) + "\n " +
                PRIMARY_COLOR + "\n /" + label + " reload" + ChatColor.GRAY + " - Reloads the plugin's configuration" +
                "\n ";
        sender.sendMessage(message);
    }
}
