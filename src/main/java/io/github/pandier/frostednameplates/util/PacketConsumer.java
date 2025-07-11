package io.github.pandier.frostednameplates.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

@ApiStatus.Internal
public interface PacketConsumer extends Consumer<PacketWrapper<?>> {

    @NotNull UUID getUUID();

    @NotNull
    static PacketConsumer user(@NotNull User user) {
        return new PacketConsumer() {
            @Override
            public @NotNull UUID getUUID() {
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
        return new PacketConsumer() {
            @Override
            public @NotNull UUID getUUID() {
                return player.getUniqueId();
            }

            @Override
            public void accept(PacketWrapper<?> packetWrapper) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packetWrapper);
            }
        };
    }

    @NotNull
    static PacketConsumer after(@NotNull PacketSendEvent event) {
        return after(event, event.getUser());
    }

    @NotNull
    static PacketConsumer after(@NotNull PacketSendEvent event, @NotNull User user) {
        return new PacketConsumer() {
            @Override
            public @NotNull UUID getUUID() {
                return user.getUUID();
            }

            @Override
            public void accept(PacketWrapper<?> packetWrapper) {
                event.getTasksAfterSend().add(() -> user.sendPacket(packetWrapper));
            }
        };
    }
}
