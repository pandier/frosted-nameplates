package io.github.pandier.frostednameplates.internal.integration;

import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.pandier.frostednameplates.internal.FrostedNameplatesPlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

public class MiniPlaceholdersIntegration {
    private final boolean enabled;

    public MiniPlaceholdersIntegration(@NotNull FrostedNameplatesPlugin plugin) {
        this.enabled = plugin.getServer().getPluginManager().isPluginEnabled("MiniPlaceholders");
    }

    public @NotNull TagResolver tagResolver(@NotNull Audience audience) {
        if (!enabled) return TagResolver.empty();
        return MiniPlaceholders.getAudienceGlobalPlaceholders(audience);
    }
}
