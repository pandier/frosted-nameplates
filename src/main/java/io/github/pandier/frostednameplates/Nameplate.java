package io.github.pandier.frostednameplates;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.pandier.frostednameplates.util.PacketConsumer;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApiStatus.Internal
public class Nameplate {
    private static final int FLAGS_DATA_INDEX = 0;
    private static final int CUSTOM_NAME_DATA_INDEX = 2;
    private static final int CUSTOM_NAME_VISIBLE_DATA_INDEX = 3;
    private static final int TEXT_DISPLAY_BACKGROUND_DATA_INDEX = 25;

    private final int entityId;
    private final UUID uuid;
    private final int targetEntityId;
    private Component text = Component.empty();

    public Nameplate(int entityId, @NotNull UUID uuid, int targetEntityId) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.targetEntityId = targetEntityId;
    }

    public Nameplate(int targetEntityId) {
        this(SpigotReflectionUtil.generateEntityId(), UUID.randomUUID(), targetEntityId);
    }

    private byte getFlags(boolean sneaking) {
        // 32 = invisible, 2 = sneaking
        return (byte) (32 | (sneaking ? 2 : 0));
    }

    public void sendSpawnPackets(@NotNull PacketConsumer packetConsumer, @NotNull Vector3d position) {
        packetConsumer.accept(new WrapperPlayServerSpawnEntity(entityId, Optional.of(uuid), EntityTypes.TEXT_DISPLAY, position.add(0.0, 1.8, 0.0), 0f, 0f, 0f, 0, Optional.of(new Vector3d())));
        packetConsumer.accept(new WrapperPlayServerEntityMetadata(entityId, List.of(
                new EntityData<>(FLAGS_DATA_INDEX, EntityDataTypes.BYTE, getFlags(false)),
                new EntityData<>(CUSTOM_NAME_DATA_INDEX, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(this.text)),
                new EntityData<>(CUSTOM_NAME_VISIBLE_DATA_INDEX, EntityDataTypes.BOOLEAN, true),
                // Just setting the background color will make the text display not display anything, while still showing the custom name
                new EntityData<>(TEXT_DISPLAY_BACKGROUND_DATA_INDEX, EntityDataTypes.INT, 0)
        )));
        packetConsumer.accept(new WrapperPlayServerSetPassengers(targetEntityId, new int[] { entityId }));
    }

    public void sendUpdatePackets(@NotNull PacketConsumer packetConsumer) {
        packetConsumer.accept(new WrapperPlayServerEntityMetadata(entityId, List.of(
                new EntityData<>(CUSTOM_NAME_DATA_INDEX, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(this.text))
        )));
    }

    public void sendStatusPackets(@NotNull PacketConsumer packetConsumer, boolean sneaking, boolean invisible) {
        packetConsumer.accept(new WrapperPlayServerEntityMetadata(entityId, List.of(
                new EntityData<>(FLAGS_DATA_INDEX, EntityDataTypes.BYTE, getFlags(sneaking)),
                new EntityData<>(CUSTOM_NAME_VISIBLE_DATA_INDEX, EntityDataTypes.BOOLEAN, !invisible)
        )));
    }

    public void sendRemovePackets(@NotNull PacketConsumer packetConsumer) {
        packetConsumer.accept(new WrapperPlayServerDestroyEntities(entityId));
    }

    public void setText(@NotNull Component text) {
        this.text = text;
    }

    @NotNull
    public Component getText() {
        return text;
    }

    public int getEntityId() {
        return entityId;
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }
}
