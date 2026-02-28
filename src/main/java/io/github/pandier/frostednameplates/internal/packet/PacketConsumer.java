package io.github.pandier.frostednameplates.internal.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

@ApiStatus.Internal
public interface PacketConsumer extends Consumer<PacketWrapper<?>> {

    @NotNull UUID getUniqueId();

    @NotNull
    static PacketConsumer user(@NotNull User user) {
        return new PacketConsumer() {
            @Override
            public @NotNull UUID getUniqueId() {
                return user.getUUID();
            }

            @Override
            public void accept(PacketWrapper<?> packetWrapper) {
                user.sendPacket(packetWrapper);
            }
        };
    }

    @NotNull
    static PacketConsumer player(@NotNull Player player) {
        final Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
        return new PacketConsumer() {
            @Override
            public @NotNull UUID getUniqueId() {
                return player.getUniqueId();
            }

            @Override
            public void accept(PacketWrapper<?> packetWrapper) {
                PacketEvents.getAPI().getProtocolManager().sendPacket(channel, packetWrapper);
            }
        };
    }
}
