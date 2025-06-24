package io.github.pandier.frostednameplates.util;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.ScheduledPacket;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

@ApiStatus.Internal
@FunctionalInterface
public interface PacketConsumer extends Consumer<PacketContainer> {

    @NotNull
    static PacketConsumer scheduled(@NotNull PacketEvent event, @NotNull Player player) {
        return packet -> event.schedule(ScheduledPacket.fromFiltered(packet, player));
    }

    @NotNull
    static PacketConsumer scheduled(@NotNull PacketEvent event) {
        return scheduled(event, event.getPlayer());
    }

    @NotNull
    static PacketConsumer player(@NotNull Player player) {
        ProtocolManager protocolManager = Objects.requireNonNull(ProtocolLibrary.getProtocolManager(), "ProtocolLibrary.getProtocolManager()");
        return packet -> protocolManager.sendServerPacket(player, packet);
    }
}
