package io.github.pandier.frostednameplates.internal.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pandier.frostednameplates.internal.FrostedNameplatesImpl;
import io.github.pandier.frostednameplates.internal.FrostedNameplatesPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.text;

@ApiStatus.Internal
public final class FnpCommand {
    private static final TextColor INFO_COLOR = TextColor.color(0xC5DDF9);
    private static final TextColor PRIMARY_COLOR = TextColor.color(0x55A3FC);
    private static final TextColor SECONDARY_COLOR = TextColor.color(0x1C70ED);
    private static final Component PREFIX = text()
            .color(INFO_COLOR)
            .append(text("[❄] ", PRIMARY_COLOR))
            .build();

    @SuppressWarnings("UnstableApiUsage")
    public static LiteralCommandNode<CommandSourceStack> create(FrostedNameplatesImpl fn) {
        return literal("frostednameplates")
                .requires(source -> source.getSender().hasPermission("frostednameplates.command"))
                .executes(ctx -> help(fn, ctx.getSource().getSender()))
                .then(literal("update")
                        .then(argument("players", ArgumentTypes.players())
                                .executes(ctx -> {
                                    List<Player> players = ctx.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
                                    return update(fn, ctx.getSource().getSender(), players);
                                }))
                        .executes(ctx -> updateAll(fn, ctx.getSource().getSender())))
                .then(literal("reload")
                        .executes(ctx -> reload(fn, ctx.getSource().getSender())))
                .build();
    }

    private static int update(FrostedNameplatesImpl fn, CommandSender sender, List<Player> players) {
        for (Player player : players) {
            fn.update(player);
        }
        sender.sendMessage(PREFIX
                .append(text("Updated nameplate for "))
                .append(text(players.size() == 1 ? players.getFirst().getName() : players.size() + " players", PRIMARY_COLOR)));
        return players.size();
    }


    private static int updateAll(FrostedNameplatesImpl fn, CommandSender sender) {
        fn.update();
        sender.sendMessage(PREFIX.append(text("Updated all nameplates")));
        return 1;
    }

    private static int reload(FrostedNameplatesImpl fn, CommandSender sender) {
        fn.getPlugin().reloadConfig();
        sender.sendMessage(PREFIX.append(text("Successfully reloaded the plugin's configuration")));
        return 1;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static int help(FrostedNameplatesImpl fn, CommandSender sender) {
        sender.sendMessage(Component.text()
                .color(INFO_COLOR)
                .content("\n ")
                .append(text("❄ Frosted ", PRIMARY_COLOR))
                .append(text("Nameplates", SECONDARY_COLOR))
                .append(text(" v" + fn.getPlugin().getPluginMeta().getVersion() + "\n"))
                .append(text(" by " + String.join(", ", fn.getPlugin().getPluginMeta().getAuthors()) + "\n\n"))
                .append(text(" /fnp reload", PRIMARY_COLOR))
                .append(text(" - Reloads the plugin's configuration\n"))
                .append(text(" /fnp update [players]", PRIMARY_COLOR))
                .append(text(" - Updates nameplates of the specified players or all players\n"))
                .build());
        return 1;
    }
}
