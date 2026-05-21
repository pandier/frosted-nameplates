package io.github.pandier.frostednameplates.internal.integration;

import io.github.pandier.frostednameplates.internal.FrostedNameplatesPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class PlaceholderAPIIntegration {
    private final boolean enabled;

    public PlaceholderAPIIntegration(@NotNull FrostedNameplatesPlugin plugin) {
        this.enabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public @NotNull String setPlaceholders(@Nullable Player player, @NotNull String text) {
        if (!enabled) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public @NotNull String setPlaceholders(@Nullable Player viewer, @NotNull Player target, @NotNull String text) {
        if (!enabled) return text;

        String result = PlaceholderAPI.setPlaceholders(target, text);
        if (viewer != null) {
            result = PlaceholderAPI.setRelationalPlaceholders(viewer, target, result);
        }
        return result;
    }
}
