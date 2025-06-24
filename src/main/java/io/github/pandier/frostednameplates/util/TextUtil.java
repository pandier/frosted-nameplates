package io.github.pandier.frostednameplates.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class TextUtil {

    public static @NotNull String colorHex(int hex) {
        return colorHex(String.format("%06X", hex & 0xFFFFFF));
    }

    public static @NotNull String colorHex(@NotNull String hex) {
        if (hex.startsWith("#"))
            hex = hex.substring(1);
        if (hex.length() != 6)
            throw new IllegalArgumentException("Hex color value must be 6 characters long");
        final StringBuilder builder = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        for (char c : hex.toCharArray())
            builder.append(ChatColor.COLOR_CHAR).append(c);
        return builder.toString();
    }
}
