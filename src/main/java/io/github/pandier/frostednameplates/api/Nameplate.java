package io.github.pandier.frostednameplates.api;

import net.kyori.adventure.text.Component;

public interface Nameplate {

    void setText(Component text);

    Component getText();
}
