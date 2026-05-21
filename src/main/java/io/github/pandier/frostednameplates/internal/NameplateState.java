package io.github.pandier.frostednameplates.internal;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record NameplateState(Component text, boolean visible, boolean textOverridden) {
    public static final NameplateState DEFAULT = new NameplateState(Component.empty(), true, false);

    public NameplateState withText(Component text) {
        return new NameplateState(text, this.visible, this.textOverridden);
    }

    public NameplateState withText(Component text, boolean textOverridden) {
        return new NameplateState(text, this.visible, textOverridden);
    }

    public NameplateState withVisible(boolean visible) {
        return new NameplateState(this.text, visible, this.textOverridden);
    }
}
