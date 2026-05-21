package io.github.pandier.frostednameplates.internal.packet.render;

import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

@ApiStatus.Internal
public class RenderedNameplateState {
    public final int entityId;
    public final UUID entityUuid;
    public final int targetId;
    public Component text = Component.empty();
    public boolean visible = true;
    public boolean invisibleStatus = false;

    public RenderedNameplateState(int targetId) {
        this.entityId = SpigotReflectionUtil.generateEntityId();
        this.entityUuid = UUID.randomUUID();
        this.targetId = targetId;
    }

    public boolean isVisible() {
        return this.visible && !this.invisibleStatus;
    }
}
