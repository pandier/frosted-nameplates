package io.github.pandier.frostednameplates;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import io.github.pandier.frostednameplates.util.PacketConsumer;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.List;
import java.util.function.BiConsumer;

@ApiStatus.Internal
public final class FnpPacketAdapters {
    private final FrostedNameplates plugin;

    public FnpPacketAdapters(@NotNull FrostedNameplates plugin) {
        this.plugin = plugin;
    }

    public void register(@NotNull ProtocolManager protocolManager) {
        listenOutgoing(protocolManager, PacketType.Play.Server.SPAWN_ENTITY, this::onSpawnEntity);
        listenOutgoing(protocolManager, PacketType.Play.Server.ENTITY_METADATA, this::onEntityMetadata);
        listenOutgoing(protocolManager, PacketType.Play.Server.ENTITY_DESTROY, this::onDestroyEntities);
    }

    private void listenOutgoing(@NotNull ProtocolManager protocolManager, @NotNull PacketType type, @NotNull BiConsumer<PacketEvent, PacketContainer> executor) {
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, type) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isAsync()) {
                    plugin.getLogger().warning("Received asynchronous packet '" + event.getPacketType() + "', ignoring");
                    return;
                }
                executor.accept(event, event.getPacket());
            }
        });
    }

    private void onSpawnEntity(PacketEvent event, PacketContainer container) {
        final EntityType entityType = container.getEntityTypeModifier().read(0);
        if (entityType != EntityType.PLAYER) return;
        final int targetEntityId = container.getIntegers().read(0);
        final Vector3d position = new Vector3d(container.getDoubles().read(0), container.getDoubles().read(1), container.getDoubles().read(2));
        plugin.showNameplate(event.getPlayer(), PacketConsumer.scheduled(event), targetEntityId, position);
    }

    private void onEntityMetadata(PacketEvent event, PacketContainer container) {
        final int entityId = container.getIntegers().read(0);
        final Nameplate nameplate = plugin.getNameplate(entityId);
        if (nameplate == null) return;
        final List<WrappedDataValue> values = container.getDataValueCollectionModifier().read(0);
        final WrappedDataValue flagsData = values.stream().filter(value -> value.getIndex() == 0).findFirst().orElse(null);
        if (flagsData == null) return;
        final byte flags = (byte) flagsData.getValue();
        boolean sneaking = (flags & 2) != 0;
        boolean invisible = (flags & 32) != 0;
        nameplate.sendStatusPackets(PacketConsumer.scheduled(event), sneaking, invisible);
    }

    private void onDestroyEntities(PacketEvent event, PacketContainer container) {
        final List<Integer> targetEntityIds = container.getIntLists().read(0);
        for (int targetEntityId : targetEntityIds) {
            plugin.hideNameplate(event.getPlayer(), PacketConsumer.scheduled(event), targetEntityId);
        }
    }
}
