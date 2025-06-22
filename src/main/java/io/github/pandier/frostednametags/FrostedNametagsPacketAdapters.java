package io.github.pandier.frostednametags;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import io.github.pandier.frostednametags.util.PacketConsumer;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.List;
import java.util.function.BiConsumer;

@ApiStatus.Internal
public final class FrostedNametagsPacketAdapters {
    private final FrostedNametags plugin;

    public FrostedNametagsPacketAdapters(@NotNull FrostedNametags plugin, @NotNull ProtocolManager protocolManager) {
        this.plugin = plugin;

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
        plugin.showNametag(event.getPlayer(), PacketConsumer.scheduled(event), targetEntityId, position);
    }

    private void onEntityMetadata(PacketEvent event, PacketContainer container) {
        final int entityId = container.getIntegers().read(0);
        final ArmorStandNametag nametag = plugin.getNametag(entityId);
        if (nametag == null) return;
        final List<WrappedDataValue> values = container.getDataValueCollectionModifier().read(0);
        final WrappedDataValue flagsData = values.stream().filter(value -> value.getIndex() == 0).findFirst().orElse(null);
        if (flagsData == null) return;
        final byte flags = (byte) flagsData.getValue();
        boolean sneaking = (flags & 2) != 0;
        nametag.sendSneakingPackets(PacketConsumer.scheduled(event), sneaking);
    }

    private void onDestroyEntities(PacketEvent event, PacketContainer container) {
        final List<Integer> targetEntityIds = container.getIntLists().read(0);
        for (int targetEntityId : targetEntityIds) {
            plugin.hideNametag(event.getPlayer(), PacketConsumer.scheduled(event), targetEntityId);
        }
    }

//    public FrostedNametagsPacketListener(@NotNull FrostedNametags.Internal plugin) {
//        this.plugin = plugin;
//    }
//
//    @Override
//    public void onPacketSend(PacketSendEvent event) {
//        switch (event.getPacketType()) {
//            case PacketType.Play.Server.SPAWN_ENTITY -> onSpawnEntity(event, new WrapperPlayServerSpawnEntity(event));
//            case PacketType.Play.Server.ENTITY_METADATA -> onEntityMetadata(event, new WrapperPlayServerEntityMetadata(event));
//            case PacketType.Play.Server.DESTROY_ENTITIES -> onDestroyEntities(event, new WrapperPlayServerDestroyEntities(event));
//            default -> {}
//        }
//    }
//
//    private void onSpawnEntity(PacketSendEvent event, WrapperPlayServerSpawnEntity packet) {
//        if (packet.getEntityType() != EntityTypes.PLAYER) return; // Only customize nametags of players
//        final int targetEntityId = packet.getEntityId();
//        final User user = event.getUser();
//        final Vector3d position = packet.getPosition();
//        event.getTasksAfterSend().add(() -> plugin.showNametag(targetEntityId, user, position.add(0, 1.8, 0)));
//    }
//
//    private void onEntityMetadata(PacketSendEvent event, WrapperPlayServerEntityMetadata packet) {
//        final ArmorStandNametag nametag = plugin.getNametag(packet.getEntityId());
//        if (nametag == null) return;
//        final EntityData<?> flagsData = packet.getEntityMetadata().stream()
//                .filter(data -> data.getIndex() == 0)
//                .findFirst()
//                .orElse(null);
//        if (flagsData == null || flagsData.getType() != EntityDataTypes.BYTE) return;
//        final byte flags = (byte) flagsData.getValue();
//        boolean sneaking = (flags & 2) != 0;
//        final User user = event.getUser();
//        event.getTasksAfterSend().add(() -> nametag.sendSneakingPackets(user, sneaking));
//    }
//
//    private void onDestroyEntities(PacketSendEvent event, WrapperPlayServerDestroyEntities packet) {
//        final int[] targetEntityIds = packet.getEntityIds();
//        final User user = event.getUser();
//        event.getTasksAfterSend().add(() -> {
//            for (int targetEntityId : targetEntityIds) {
//                plugin.hideNametag(targetEntityId, user);
//            }
//        });
//    }
//
//    @Override
//    public void onUserDisconnect(UserDisconnectEvent event) {
//        plugin.dispose(event.getUser());
//    }
//
//    @Override
//    public void onPacketSending(PacketEvent packetEvent) {
//
//    }
//
//    @Override
//    public void onPacketReceiving(PacketEvent packetEvent) {
//
//    }
//
//    @Override
//    public ListeningWhitelist getSendingWhitelist() {
//        return null;
//    }
//
//    @Override
//    public ListeningWhitelist getReceivingWhitelist() {
//        return null;
//    }
//
//    @Override
//    public Plugin getPlugin() {
//        return null;
//    }
}
