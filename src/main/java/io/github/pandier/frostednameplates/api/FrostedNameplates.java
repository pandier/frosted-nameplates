package io.github.pandier.frostednameplates.api;

import io.github.pandier.frostednameplates.internal.FrostedNameplatesPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FrostedNameplates {

    Nameplate getNameplate(Player player);

    static @UnknownNullability FrostedNameplates get() {
        return FrostedNameplatesPlugin.getFn();
    }
}
