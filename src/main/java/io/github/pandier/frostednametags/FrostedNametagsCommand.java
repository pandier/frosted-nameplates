package io.github.pandier.frostednametags;

import io.github.pandier.frostednametags.util.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class FrostedNametagsCommand implements CommandExecutor, TabCompleter {
    private static final String PRIMARY_COLOR = TextUtil.colorHex("5f9fe8");
    private static final String SECONDARY_COLOR = TextUtil.colorHex("1c70ed");
    private static final String ICON = "❄";
    private static final String INFO_PREFIX = PRIMARY_COLOR;

    private final FrostedNametags plugin;

    public FrostedNametagsCommand(@NotNull FrostedNametags plugin) {
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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            final String arg = args[0].toLowerCase(Locale.ROOT);
            return Stream.of("reload").filter(subcommand -> subcommand.startsWith(arg)).toList();
        }
        return List.of();
    }

    private void help(@NotNull CommandSender sender, @NotNull String label) {
        final String message = "\n " +
                PRIMARY_COLOR + ICON + " Frosted " + SECONDARY_COLOR + "Nametags " + ChatColor.GRAY + "v" + plugin.getDescription().getVersion() +
                "\n by " + String.join(", ", plugin.getDescription().getAuthors()) + "\n " +
                PRIMARY_COLOR + "\n /" + label + " reload" + ChatColor.GRAY + " - Reloads the plugin's configuration" +
                "\n ";
        sender.sendMessage(message);
    }
}
