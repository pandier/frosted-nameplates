package io.github.pandier.frostednameplates.internal;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface NameplateSubscriber {

    void onNameplateChange(int id, NameplateState newState);

    void onNameplateRemove(int id);
}
