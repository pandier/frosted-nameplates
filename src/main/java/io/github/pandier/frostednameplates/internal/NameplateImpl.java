package io.github.pandier.frostednameplates.internal;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ApiStatus.Internal
public class NameplateImpl {
    private final FrostedNameplatesImpl fn;
    private final int targetId;

    private final ReentrantReadWriteLock removeLock = new ReentrantReadWriteLock();
    private final Set<NameplateSubscriber> subscribers = ConcurrentHashMap.newKeySet();

    private NameplateState state = NameplateState.DEFAULT;
    private volatile boolean removed = false;

    public NameplateImpl(@NotNull FrostedNameplatesImpl fn, int targetId) {
        this.fn = fn;
        this.targetId = targetId;
    }

    // Thread-safe
    public @Nullable NameplateState subscribe(@NotNull NameplateSubscriber subscriber) {
        if (this.removed) return null;

        this.removeLock.readLock().lock();
        try {
            if (this.removed) return null;

            this.subscribers.add(subscriber);

            return this.state;
        } finally {
            this.removeLock.readLock().unlock();
        }
    }

    // Thread-safe
    public void unsubscribe(@NotNull NameplateSubscriber subscriber) {
        if (this.removed) return;

        this.removeLock.readLock().lock();
        try {
            if (this.removed) return;

            this.subscribers.remove(subscriber);
        } finally {
            this.removeLock.readLock().unlock();
        }
    }

    // Main thread
    public void remove() {
        this.removeLock.writeLock().lock();
        try {
            this.removed = true;
        } finally {
            this.removeLock.writeLock().unlock();
        }

        for (NameplateSubscriber subscriber : this.subscribers) {
            subscriber.onNameplateRemove(this.targetId);
        }
        this.subscribers.clear();
    }

    // Main thread
    public void update(@NotNull Player player) {
        if (this.removed) return;

        Component newText = this.fn.getPlugin().createNameplateText(player);

        if (this.state.text().equals(newText))
            return;

        this.changeState(this.state.withText(newText));
    }

    // Main thread
    private void changeState(NameplateState newState) {
        this.state = newState;

        for (NameplateSubscriber subscriber : this.subscribers) {
            subscriber.onNameplateChange(this.targetId, newState);
        }
    }
}
