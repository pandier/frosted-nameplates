package io.github.pandier.frostednameplates.internal.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.pandier.frostednameplates.internal.FrostedNameplatesImpl;
import io.github.pandier.frostednameplates.internal.NameplateImpl;
import io.github.pandier.frostednameplates.internal.NameplateState;
import io.github.pandier.frostednameplates.internal.NameplateSubscriber;
import io.github.pandier.frostednameplates.internal.packet.render.RenderedNameplateState;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
public class FnpChannel implements NameplateSubscriber {
    private final FrostedNameplatesImpl fn;
    private final User user;

    private final Map<Integer, RenderedNameplateState> nameplates = new ConcurrentHashMap<>();

    public FnpChannel(FrostedNameplatesImpl fn, User user) {
        this.fn = fn;
        this.user = user;
    }

    public void onSpawnEntity(PacketSendEvent event, WrapperPlayServerSpawnEntity packet) {
        if (packet.getEntityType() != EntityTypes.PLAYER) return;

        int targetId = packet.getEntityId();

        // subscribe to nameplate

        NameplateImpl nameplate = this.fn.getNameplate(targetId);
        if (nameplate == null) return;

        NameplateState state = nameplate.subscribe(this);
        if (state == null) return;

        this.fn.getPlugin().getSLF4JLogger().debug("User {} subscribed to nameplate of {}", user.getUUID(), targetId);

        // render the new nameplate

        RenderedNameplateState renderState = this.fn.getNameplateRenderer().create(this.user, targetId, packet.getPosition(), state, renderText(targetId, state));
        this.nameplates.put(targetId, renderState);

        event.getTasksAfterSend().add(() -> {
            user.sendPacket(new WrapperPlayServerSetPassengers(renderState.targetId, new int[] { renderState.entityId }));
        });
    }

    public void onEntityMetadata(WrapperPlayServerEntityMetadata packet) {
        EntityData<?> flagsData = packet.getEntityMetadata().stream()
                .filter(value -> value.getIndex() == 0)
                .findFirst()
                .orElse(null);
        if (flagsData == null) return;

        int targetId = packet.getEntityId();
        RenderedNameplateState renderState = this.nameplates.get(targetId);
        if (renderState == null) return;

        byte flags = (byte) flagsData.getValue();
        boolean sneaking = (flags & 2) != 0;
        boolean invisible = (flags & 32) != 0;

        this.fn.getNameplateRenderer().updateStatus(this.user, renderState, sneaking, invisible);
    }

    public void onSetPassengers(PacketSendEvent event, WrapperPlayServerSetPassengers packet) {
        int targetId = packet.getEntityId();

        RenderedNameplateState renderState = this.nameplates.get(targetId);
        if (renderState == null) return;

        int[] passengers = packet.getPassengers();

        // check if the nameplate entitiy is already a passanger

        int nameplateEntityId = renderState.entityId;
        if (Arrays.stream(passengers).anyMatch(id -> nameplateEntityId == id)) return;

        // if not, add it to the array

        int[] newPassengers = new int[passengers.length + 1];
        newPassengers[0] = nameplateEntityId;
        System.arraycopy(passengers, 0, newPassengers, 1, passengers.length);

        // update the packet

        packet.setPassengers(newPassengers);
        event.markForReEncode(true);
    }

    public void onDestroyEntities(WrapperPlayServerDestroyEntities packet) {
        for (int targetId : packet.getEntityIds()) {
            this.removeNameplate(targetId);

            NameplateImpl nameplate = this.fn.getNameplate(targetId);
            if (nameplate != null) {
                nameplate.unsubscribe(this);

                this.fn.getPlugin().getSLF4JLogger().debug("User {} unsubscribed from nameplate of {}", user.getUUID(), targetId);
            }
        }
    }

    public void onDisconnect() {
        for (RenderedNameplateState renderState : this.nameplates.values()) {
            NameplateImpl nameplate = this.fn.getNameplate(renderState.targetId);
            if (nameplate != null) {
                nameplate.unsubscribe(this);
            }
            // no rendering needed, because the user is disconnecting
        }
        this.nameplates.clear();

        this.fn.getPlugin().getSLF4JLogger().debug("User {} disconnected", user.getUUID());
    }

    @Override
    public void onNameplateChange(int id, NameplateState newState) {
        execute(() -> {
            RenderedNameplateState renderState = this.nameplates.get(id);
            if (renderState == null) return;

            this.fn.getNameplateRenderer().update(this.user, renderState, newState, renderText(id, newState));

            this.fn.getPlugin().getSLF4JLogger().debug("User {} received nameplate update of {}", user.getUUID(), id);
        });
    }

    @Override
    public void onNameplateRemove(int id) {
        execute(() -> {
            this.removeNameplate(id);
            // don't need to unsubscribe here, because the nameplate is getting removed (which removes all of its subscribers)

            this.fn.getPlugin().getSLF4JLogger().debug("User {} received nameplate remove of {}", user.getUUID(), id);
        });
    }

    private void removeNameplate(int id) {
        RenderedNameplateState renderState = this.nameplates.remove(id);
        if (renderState == null) return;

        this.fn.getNameplateRenderer().remove(this.user, renderState);
    }

    private void execute(Runnable runnable) {
        PacketEvents.getAPI().getNettyManager().getChannelOperator().runInEventLoop(this.user.getChannel(), runnable);
    }

    private Component renderText(int targetId, NameplateState state) {
        if (state.textOverridden()) return state.text();

        Player viewer = this.fn.getServer().getPlayer(this.user.getUUID());
        Player target = this.fn.getServer().getOnlinePlayers().stream()
                .filter(player -> player.getEntityId() == targetId)
                .findFirst()
                .orElse(null);

        if (viewer == null || target == null) return state.text();

        return this.fn.getPlugin().createNameplateText(viewer, target);
    }
}
