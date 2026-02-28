package io.github.pandier.frostednameplates.internal;

import com.github.retrooper.packetevents.util.Vector3d;
import io.github.pandier.frostednameplates.internal.packet.entity.NameplateEntity;
import io.github.pandier.frostednameplates.internal.packet.PacketConsumer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ApiStatus.Internal
public class NameplateImpl {
    private final FrostedNameplatesImpl fn;
    private final ReentrantReadWriteLock removeLock = new ReentrantReadWriteLock();
    private final NameplateEntity entity;
    private final int targetId;
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
    private volatile Component text = Component.empty(); // only update on main thread
    private volatile boolean removed = false;

    public NameplateImpl(@NotNull FrostedNameplatesImpl fn, int targetId) {
        this.fn = fn;
        this.entity = new NameplateEntity(targetId);
        this.targetId = targetId;
    }

    // PacketConsumer's thread
    public void show(@NotNull PacketConsumer consumer, @NotNull Vector3d position) {
        this.removeLock.readLock().lock();
        try {
            if (this.removed) return;

            this.viewers.add(consumer.getUniqueId());
            this.fn.getNameplateViewershipTracker().show(consumer.getUniqueId(), this.targetId);

            Component text = this.text;
            this.entity.show(consumer, position, text);

            // Make sure that they're seeing the latest version
            while (this.text != text) {
                text = this.text;
                this.entity.updateText(consumer, text);
            }
        } finally {
            this.removeLock.readLock().unlock();
        }
    }

    // PacketConsumer's thread
    public void updateStatus(@NotNull PacketConsumer consumer, boolean sneaking, boolean invisible) {
        this.entity.updateStatus(consumer, sneaking, invisible);
    }

    // PacketConsumer's thread
    public void hide(@NotNull PacketConsumer consumer) {
        this.removeLock.readLock().lock();
        try {
            if (this.removed) return;

            this.viewers.remove(consumer.getUniqueId());
            this.fn.getNameplateViewershipTracker().hide(consumer.getUniqueId(), this.targetId);
            this.entity.hide(consumer);
        } finally {
            this.removeLock.readLock().unlock();
        }
    }

    // Main thread
    public void disposeViewer(@NotNull UUID uuid) {
        if (this.removed) return;
        this.viewers.remove(uuid);
    }

    // Main thread
    public void remove() {
        this.removeLock.writeLock().lock();
        try {
            this.removed = true;
        } finally {
            this.removeLock.writeLock().unlock();
        }

        for (UUID uuid : this.viewers) {
            this.fn.getNameplateViewershipTracker().hide(uuid, this.targetId);

            Player player = this.fn.getServer().getPlayer(uuid);
            if (player != null) {
                this.entity.hide(PacketConsumer.player(player));
            }
        }

        this.viewers.clear();
    }

    // Main thread
    public void update(@NotNull Player player) {
        if (this.removed) return;

        Component newText = this.fn.getPlugin().createNameplateText(player);

        if (this.text.equals(newText))
            return;

        this.text = newText;

        this.viewers.removeIf(uuid -> {
            Player viewer = this.fn.getServer().getPlayer(uuid);
            if (viewer == null) return true;
            this.entity.updateText(PacketConsumer.player(viewer), text);
            return false;
        });
    }

    public int getEntityId() {
        return this.entity.getId();
    }
}
