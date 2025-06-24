package io.github.pandier.frostednameplates;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.github.pandier.frostednameplates.util.DataValueCollectionBuilder;
import io.github.pandier.frostednameplates.util.EntityUtil;
import io.github.pandier.frostednameplates.util.PacketConsumer;
import io.github.pandier.frostednameplates.util.PacketFactory;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

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
    private String text = ChatColor.RED + "N/A";

    public Nameplate(int entityId, @NotNull UUID uuid, int targetEntityId) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.targetEntityId = targetEntityId;
    }

    public Nameplate(int targetEntityId) {
        this(EntityUtil.generateEntityId(), UUID.randomUUID(), targetEntityId);
    }

    private byte getFlags(boolean sneaking) {
        // 32 = invisible, 2 = sneaking
        return (byte) (32 | (sneaking ? 2 : 0));
    }

    public void sendSpawnPackets(@NotNull PacketConsumer packetConsumer, @NotNull Vector3d position) {
        packetConsumer.accept(PacketFactory.createSpawnEntityPacket(entityId, uuid, EntityType.TEXT_DISPLAY, position.add(0.0, 1.8, 0.0, new Vector3d())));
        packetConsumer.accept(PacketFactory.createEntityMetadataPacket(entityId, new DataValueCollectionBuilder()
                .add(FLAGS_DATA_INDEX, Byte.class, getFlags(false))
                .add(CUSTOM_NAME_DATA_INDEX, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromLegacyText(text).getHandle()))
                .add(CUSTOM_NAME_VISIBLE_DATA_INDEX, Boolean.class, true)
                // Just setting the background color will make the text display not display anything, while still showing the custom name
                .add(TEXT_DISPLAY_BACKGROUND_DATA_INDEX, Integer.class, 0)));
        packetConsumer.accept(PacketFactory.createSetPassengersPacket(targetEntityId, new int[] { entityId }));
    }

    public void sendUpdatePackets(@NotNull PacketConsumer packetConsumer) {
        packetConsumer.accept(PacketFactory.createEntityMetadataPacket(entityId, new DataValueCollectionBuilder()
                .add(CUSTOM_NAME_DATA_INDEX, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromLegacyText(text).getHandle()))));
    }

    public void sendStatusPackets(@NotNull PacketConsumer packetConsumer, boolean sneaking, boolean invisible) {
        packetConsumer.accept(PacketFactory.createEntityMetadataPacket(entityId, new DataValueCollectionBuilder()
                .add(FLAGS_DATA_INDEX, Byte.class, getFlags(sneaking))
                .add(CUSTOM_NAME_VISIBLE_DATA_INDEX, Boolean.class, !invisible)));
    }

    public void sendRemovePackets(@NotNull PacketConsumer packetConsumer) {
        packetConsumer.accept(PacketFactory.createEntityDestroyPacket(List.of(entityId)));
    }

    public void setText(@NotNull String text) {
        this.text = text;
    }

    @NotNull
    public String getText() {
        return text;
    }
}
