package io.github.pandier.frostednameplates.internal;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record NameplateState(Component text, boolean visible) {
    public static final NameplateState DEFAULT = new NameplateState(Component.empty(), true);

    public NameplateState withText(Component text) {
        return new NameplateState(text, this.visible);
    }

    public NameplateState withVisible(boolean visible) {
        return new NameplateState(this.text, visible);
    }
}
