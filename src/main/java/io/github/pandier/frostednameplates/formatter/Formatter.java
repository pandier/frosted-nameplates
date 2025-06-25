package io.github.pandier.frostednameplates.formatter;

import io.github.pandier.frostednameplates.FrostedNameplates;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

public enum Formatter {
    MINIMESSAGE((plugin, x) -> MiniMessage.miniMessage().deserialize(x.replace('§', '?'))),
    LEGACY((plugin, x) -> getLegacySerializer().deserialize(x.replace('&', '§'))),
    HYBRID((plugin, x) -> MINIMESSAGE.format(plugin, MiniMessage.miniMessage().serialize(LEGACY.format(plugin, x))));

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

    private final BiFunction<FrostedNameplates, String, Component> formatter;

    Formatter(BiFunction<FrostedNameplates, String, Component> formatter) {
        this.formatter = formatter;
    }

    public @NotNull Component format(@NotNull FrostedNameplates plugin, @NotNull String text) {
        return formatter.apply(plugin, text);
    }
}
