package io.github.pandier.frostednameplates.formatter;

import io.github.pandier.frostednameplates.FrostedNameplates;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum Formatter {
    MINIMESSAGE {
        @Override
        public @NotNull Component format(@NotNull String text, @NotNull Player player, @NotNull FrostedNameplates plugin) {
            final TagResolver tagResolver = plugin.getMiniPlaceholdersIntegration().tagResolver(player);
            return MiniMessage.miniMessage().deserialize(text.replace('§', '?'), tagResolver);
        }
    },
    LEGACY {
        @Override
        public @NotNull Component format(@NotNull String text, @NotNull Player player, @NotNull FrostedNameplates plugin) {
            return getLegacySerializer().deserialize(text.replace('&', '§'));
        }
    },
    HYBRID {
        @Override
        public @NotNull Component format(@NotNull String text, @NotNull Player player, @NotNull FrostedNameplates plugin) {
            return MINIMESSAGE.format(MiniMessage.miniMessage().serialize(LEGACY.format(text, player, plugin)), player, plugin);
        }
    };

    public static final Map<String, Formatter> BY_NAME = new HashMap<>();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexCharacter('#')
            .useUnusualXRepeatedCharacterHexFormat()
            .hexColors()
            .build();

    static {
        for (Formatter formatter : Formatter.values()) {
            BY_NAME.put(formatter.name().toLowerCase(Locale.ROOT), formatter);
        }
    }

    private static LegacyComponentSerializer getLegacySerializer() {
        return LEGACY_SERIALIZER;
    }

    public abstract @NotNull Component format(@NotNull String text, @NotNull Player player, @NotNull FrostedNameplates plugin);
}
