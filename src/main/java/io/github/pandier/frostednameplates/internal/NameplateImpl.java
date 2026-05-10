package io.github.pandier.frostednameplates.internal;

import io.github.pandier.frostednameplates.api.Nameplate;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ApiStatus.Internal
@NullMarked
public class NameplateImpl implements Nameplate {
    private final FrostedNameplatesImpl fn;
    private final int targetId;

    private final ReentrantReadWriteLock removeLock = new ReentrantReadWriteLock();
    private final Set<NameplateSubscriber> subscribers = ConcurrentHashMap.newKeySet();

    private NameplateState state = NameplateState.DEFAULT;
    private @Nullable Component textOverride = null;

    private volatile boolean removed = false;

    public NameplateImpl(FrostedNameplatesImpl fn, int targetId) {
        this.fn = fn;
        this.targetId = targetId;
    }

    // Thread-safe
    public @Nullable NameplateState subscribe(NameplateSubscriber subscriber) {
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
    public void unsubscribe(NameplateSubscriber subscriber) {
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
    public void update(Player player) {
        if (this.removed || this.textOverride != null) return;

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

    @Override
    public void setVisible(boolean visible) {
        if (this.state.visible() == visible) return;

        this.changeState(this.state.withVisible(visible));
    }

    @Override
    public boolean isVisible() {
        return this.state.visible();
    }

    @Override
    public void setTextOverride(@Nullable Component text) {
        if (Objects.equals(this.textOverride, text)) return;

        this.textOverride = text;
        this.changeState(this.state.withText(text));
    }

    @Override
    public @Nullable Component getTextOverride() {
        return this.textOverride;
    }

    @Override
    public Component getText() {
        return this.state.text();
    }
}
