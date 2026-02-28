package io.github.pandier.frostednameplates.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface FrostedNameplates {
    @NotNull Nameplate getNameplate(@NotNull Player player);
}
