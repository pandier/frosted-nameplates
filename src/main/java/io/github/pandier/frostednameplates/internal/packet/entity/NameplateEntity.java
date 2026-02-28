package io.github.pandier.frostednameplates.internal.packet.entity;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.pandier.frostednameplates.internal.packet.PacketConsumer;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApiStatus.Internal
public class NameplateEntity {
    private static final int FLAGS_DATA_INDEX = 0;
    private static final int CUSTOM_NAME_DATA_INDEX = 2;
    private static final int CUSTOM_NAME_VISIBLE_DATA_INDEX = 3;
    private static final int TEXT_DISPLAY_BACKGROUND_DATA_INDEX = 25;

    private final int targetId;
    private final UUID uuid;
    private final int id;

    public NameplateEntity(int targetId) {
        this.targetId = targetId;
        this.uuid = UUID.randomUUID();
        this.id = SpigotReflectionUtil.generateEntityId();
    }

    private byte getFlags(boolean sneaking) {
        // 32 = invisible, 2 = sneaking
        return (byte) (32 | (sneaking ? 2 : 0));
    }

    public void show(@NotNull PacketConsumer consumer, @NotNull Vector3d position, @NotNull Component text) {
        consumer.accept(new WrapperPlayServerSpawnEntity(id, Optional.of(uuid), EntityTypes.TEXT_DISPLAY,
                position.add(0.0, 1.8, 0.0), 0f, 0f, 0f, 0, Optional.of(new Vector3d())));
        consumer.accept(new WrapperPlayServerEntityMetadata(id, List.of(
                new EntityData<>(FLAGS_DATA_INDEX, EntityDataTypes.BYTE, getFlags(false)),
                new EntityData<>(CUSTOM_NAME_DATA_INDEX, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(text)),
                new EntityData<>(CUSTOM_NAME_VISIBLE_DATA_INDEX, EntityDataTypes.BOOLEAN, true),
                // Just setting the background color will make the text display not display anything, while still showing the custom name
                new EntityData<>(TEXT_DISPLAY_BACKGROUND_DATA_INDEX, EntityDataTypes.INT, 0)
        )));
        consumer.accept(new WrapperPlayServerSetPassengers(targetId, new int[] { id }));
    }

    public void updateText(@NotNull PacketConsumer consumer, @NotNull Component text) {
        consumer.accept(new WrapperPlayServerEntityMetadata(id, List.of(
                new EntityData<>(CUSTOM_NAME_DATA_INDEX, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(text))
        )));
    }

    public void updateStatus(@NotNull PacketConsumer consumer, boolean sneaking, boolean invisible) {
        consumer.accept(new WrapperPlayServerEntityMetadata(id, List.of(
                new EntityData<>(FLAGS_DATA_INDEX, EntityDataTypes.BYTE, getFlags(sneaking)),
                new EntityData<>(CUSTOM_NAME_VISIBLE_DATA_INDEX, EntityDataTypes.BOOLEAN, !invisible)
        )));
    }

    public void hide(@NotNull PacketConsumer consumer) {
        consumer.accept(new WrapperPlayServerDestroyEntities(id));
    }

    public int getId() {
        return id;
    }
}
