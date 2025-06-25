package io.github.pandier.frostednameplates.integration;

import io.github.pandier.frostednameplates.FrostedNameplates;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIIntegration {
    private final boolean enabled;

    public PlaceholderAPIIntegration(@NotNull FrostedNameplates plugin) {
        this.enabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public @NotNull String setPlaceholders(@Nullable Player player, @NotNull String text) {
        if (!enabled) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
