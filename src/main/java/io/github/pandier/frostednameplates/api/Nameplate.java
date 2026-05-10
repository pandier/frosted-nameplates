package io.github.pandier.frostednameplates.api;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Nameplate {

    void setVisible(boolean visible);

    boolean isVisible();

    void setTextOverride(@Nullable Component text);

    @Nullable Component getTextOverride();

    Component getText();
}
