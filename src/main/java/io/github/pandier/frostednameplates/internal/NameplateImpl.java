package io.github.pandier.frostednameplates.internal;

import com.github.retrooper.packetevents.util.Vector3d;
import io.github.pandier.frostednameplates.api.Nameplate;
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
import java.util.function.Function;

@ApiStatus.Internal
public class NameplateImpl implements Nameplate {
    private final FrostedNameplatesImpl fn;
    private final Function<Player, Component> nameplateTextFactory;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
    private final NameplateEntity entity;
    private final int targetId;
    private volatile Component text = Component.empty(); // only update on main thread
    private volatile boolean removed = false;

    public NameplateImpl(@NotNull FrostedNameplatesImpl fn, @NotNull Function<Player, Component> nameplateTextFactory, int targetId) {
        this.fn = fn;
        this.nameplateTextFactory = nameplateTextFactory;
        this.entity = new NameplateEntity(targetId);
        this.targetId = targetId;
    }

    // PacketConsumer's thread
    public void show(@NotNull PacketConsumer consumer, @NotNull Vector3d position) {
        this.lock.readLock().lock();

        try {
            if (this.removed) return;

            this.viewers.add(consumer.getUniqueId());
            this.fn.getNameplateViewershipTracker().show(consumer.getUniqueId(), this.targetId);

            Component text = this.text;
            this.entity.show(consumer, position, text);

            this.fn.getServer().getScheduler().runTask(this.fn.getPlugin(), () -> {
                if (this.removed || this.text.equals(text)) return;
                this.entity.updateText(consumer, this.text);
            });
        } finally {
            this.lock.readLock().unlock();
        }
    }

    // PacketConsumer's thread
    public void updateStatus(@NotNull PacketConsumer consumer, boolean sneaking, boolean invisible) {
        this.entity.updateStatus(consumer, sneaking, invisible);
    }

    // PacketConsumer's thread
    public void hide(@NotNull PacketConsumer consumer) {
        this.lock.readLock().lock();

        try {
            if (this.removed) return;

            this.removeViewerInternal(consumer.getUniqueId());
            this.entity.hide(consumer);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    // Main thread
    public void removeViewer(@NotNull UUID uuid) {
        this.lock.readLock().lock();

        try {
            if (this.removed) return;

            this.removeViewerInternal(uuid);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    private void removeViewerInternal(@NotNull UUID uuid) {
        this.viewers.remove(uuid);
        this.fn.getNameplateViewershipTracker().hide(uuid, this.targetId);
    }

    // Main thread
    public synchronized void remove() {
        // No other operation with viewers should be running while removing the nameplate
        this.lock.writeLock().lock();

        try {
            this.removed = true;

            for (UUID uuid : this.viewers) {
                this.removeViewerInternal(uuid);

                Player player = fn.getServer().getPlayer(uuid);
                if (player != null) {
                    this.entity.hide(PacketConsumer.player(player));
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    // Main thread
    public void update(@NotNull Player player) {
        Component newText = this.nameplateTextFactory.apply(player);

        if (this.text.equals(newText))
            return;

        this.text = newText;

        if (!this.removed) {
            this.viewers.removeIf(uuid -> {
                Player viewer = this.fn.getServer().getPlayer(uuid);
                if (viewer == null) return true;
                this.entity.updateText(PacketConsumer.player(viewer), text);
                return false;
            });
        }
    }

    public int getEntityId() {
        return this.entity.getId();
    }
}
