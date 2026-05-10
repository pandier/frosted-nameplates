package io.github.pandier.frostednameplates.internal.packet;

import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.pandier.frostednameplates.internal.FrostedNameplatesImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
public final class FnpPacketListener extends PacketListenerAbstract {
    private final FrostedNameplatesImpl fn;
    private final Map<UUID, FnpChannel> channels = new ConcurrentHashMap<>();

    public FnpPacketListener(@NotNull FrostedNameplatesImpl fn) {
        super(PacketListenerPriority.NORMAL);
        this.fn = fn;
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        if (!this.fn.getPlugin().isEnabled()) return;

        if (event.getUser().getUUID() == null) return;

        switch (event.getPacketType()) {
            case PacketType.Play.Server.SPAWN_ENTITY -> getChannel(event.getUser()).onSpawnEntity(event, new WrapperPlayServerSpawnEntity(event));
            case PacketType.Play.Server.ENTITY_METADATA -> getChannel(event.getUser()).onEntityMetadata(new WrapperPlayServerEntityMetadata(event));
            case PacketType.Play.Server.DESTROY_ENTITIES -> getChannel(event.getUser()).onDestroyEntities(new WrapperPlayServerDestroyEntities(event));
            case PacketType.Play.Server.SET_PASSENGERS -> getChannel(event.getUser()).onSetPassengers(event, new WrapperPlayServerSetPassengers(event));
            default -> {}
        }
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void onUserDisconnect(@NotNull UserDisconnectEvent event) {
        if (!this.fn.getPlugin().isEnabled()) return;

        if (event.getUser().getUUID() == null) return;

        FnpChannel channel = this.channels.remove(event.getUser().getUUID());
        if (channel != null) {
            channel.onDisconnect();
        }
    }

    private FnpChannel getChannel(User user) {
        return this.channels.computeIfAbsent(user.getUUID(), (ignored) -> new FnpChannel(this.fn, user));
    }
}
