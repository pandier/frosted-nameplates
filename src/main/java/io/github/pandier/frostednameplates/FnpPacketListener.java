package io.github.pandier.frostednameplates;

import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.pandier.frostednameplates.util.PacketConsumer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@ApiStatus.Internal
public final class FnpPacketListener extends PacketListenerAbstract {
    private final FrostedNameplates plugin;

    public FnpPacketListener(@NotNull FrostedNameplates plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        if (!plugin.isEnabled()) return;

        if (event.getPacketType().equals(PacketType.Play.Server.SPAWN_ENTITY)) {
            onSpawnEntity(event, new WrapperPlayServerSpawnEntity(event));
        } else if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
            onEntityMetadata(event, new WrapperPlayServerEntityMetadata(event));
        } else if (event.getPacketType().equals(PacketType.Play.Server.DESTROY_ENTITIES)) {
            onDestroyEntities(event, new WrapperPlayServerDestroyEntities(event));
        } else if (event.getPacketType().equals(PacketType.Play.Server.SET_PASSENGERS)) {
            onSetPassengers(event, new WrapperPlayServerSetPassengers(event));
        }
    }

    private void onSpawnEntity(PacketSendEvent event, WrapperPlayServerSpawnEntity packet) {
        if (packet.getEntityType() != EntityTypes.PLAYER) return;
        final int targetEntityId = packet.getEntityId();
        final Player player = event.getPlayer();
        plugin.showNameplate(PacketConsumer.after(event), player.getWorld(), targetEntityId, packet.getPosition());
    }

    private void onEntityMetadata(PacketSendEvent event, WrapperPlayServerEntityMetadata packet) {
        final Nameplate nameplate = plugin.getNameplate(packet.getEntityId());
        if (nameplate == null) return;
        final EntityData<?> flagsData = packet.getEntityMetadata().stream()
                .filter(value -> value.getIndex() == 0)
                .findFirst()
                .orElse(null);
        if (flagsData == null) return;
        final byte flags = (byte) flagsData.getValue();
        boolean sneaking = (flags & 2) != 0;
        boolean invisible = (flags & 32) != 0;
        nameplate.sendStatusPackets(PacketConsumer.after(event), sneaking, invisible);
    }

    private void onSetPassengers(PacketSendEvent event, WrapperPlayServerSetPassengers packet) {
        final int targetEntityId = packet.getEntityId();
        final Nameplate nameplate = plugin.getNameplate(targetEntityId);
        if (nameplate == null) return;

        final int[] passengers = packet.getPassengers();

        int nameplateEntityId = nameplate.getEntityId();
        if (Arrays.stream(passengers).anyMatch(id -> nameplateEntityId == id)) return;

        int[] newPassengers = new int[passengers.length + 1];
        newPassengers[0] = nameplateEntityId;
        System.arraycopy(passengers, 0, newPassengers, 1, passengers.length);

        packet.setPassengers(newPassengers);
    }

    private void onDestroyEntities(PacketSendEvent event, WrapperPlayServerDestroyEntities packet) {
        for (int entityId : packet.getEntityIds()) {
            plugin.hideNameplate(PacketConsumer.after(event), entityId);
        }
    }
}
