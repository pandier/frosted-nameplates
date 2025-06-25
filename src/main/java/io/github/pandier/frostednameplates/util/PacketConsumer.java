package io.github.pandier.frostednameplates.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@ApiStatus.Internal
@FunctionalInterface
public interface PacketConsumer extends Consumer<PacketWrapper<?>> {

    @NotNull
    static PacketConsumer after(@NotNull PacketSendEvent event, @NotNull User user) {
        return packet -> event.getTasksAfterSend().add(() -> user.sendPacket(packet));
    }

    @NotNull
    static PacketConsumer after(@NotNull PacketSendEvent event) {
        return after(event, event.getUser());
    }

    @SuppressWarnings("ConstantValue")
    @NotNull
    static PacketConsumer player(@NotNull Player player) {
        final User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user == null) return x -> {};
        return user(user);
    }

    @NotNull
    static PacketConsumer user(@NotNull User user) {
        return user::sendPacket;
    }
}
