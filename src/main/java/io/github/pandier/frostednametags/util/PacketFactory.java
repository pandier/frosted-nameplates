package io.github.pandier.frostednametags.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.List;
import java.util.UUID;

@ApiStatus.Internal
public class PacketFactory {

    public static PacketContainer createSpawnEntityPacket(int id, UUID uuid, EntityType entityType, Vector3d position) {
        return createSpawnEntityPacket(id, uuid, entityType, position, (byte) 0, (byte) 0, (byte) 0, new Vector3i(0, 0, 0), 0);
    }

    public static PacketContainer createSpawnEntityPacket(int id, UUID uuid, EntityType entityType, Vector3d position, byte pitch, byte yaw, byte headYaw, Vector3i velocity, int data) {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        container.getIntegers()
                .write(0, id)
                .write(1, velocity.x)
                .write(2, velocity.y)
                .write(3, velocity.z)
                .write(4, data);
        container.getUUIDs().write(0, uuid);
        container.getEntityTypeModifier().write(0, entityType);
        container.getDoubles()
                .write(0, position.x)
                .write(1, position.y)
                .write(2, position.z);
        container.getBytes()
                .write(0, pitch)
                .write(1, yaw)
                .write(2, headYaw);
        return container;
    }

    public static PacketContainer createEntityMetadataPacket(int id, DataValueCollectionBuilder builder) {
        return createEntityMetadataPacket(id, builder.build());
    }

    public static PacketContainer createEntityMetadataPacket(int id, List<WrappedDataValue> watcher) {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        container.getIntegers().write(0, id);
        container.getDataValueCollectionModifier().write(0, watcher);
        return container;
    }

    public static PacketContainer createSetPassengersPacket(int id, int[] passengers) {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.MOUNT);
        container.getIntegers().write(0, id);
        container.getIntegerArrays().write(0, passengers);
        return container;
    }

    public static PacketContainer createEntityDestroyPacket(List<Integer> ids) {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        container.getIntLists().write(0, ids);
        return container;
    }
}
