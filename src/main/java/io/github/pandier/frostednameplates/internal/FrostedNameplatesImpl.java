package io.github.pandier.frostednameplates.internal;

import io.github.pandier.frostednameplates.api.FrostedNameplates;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FrostedNameplatesImpl implements FrostedNameplates {
    private final FrostedNameplatesPlugin plugin;
    private final NameplateViewershipTracker nameplateViewershipTracker = new NameplateViewershipTracker();
    private final Map<Integer, NameplateImpl> nameplates = new ConcurrentHashMap<>();

    public FrostedNameplatesImpl(@NotNull FrostedNameplatesPlugin plugin) {
        this.plugin = plugin;
    }

    // Main thread
    @Override
    public @NotNull NameplateImpl getNameplate(@NotNull Player player) {
        // TODO: Throw exception if not main thread
        return this.nameplates.computeIfAbsent(player.getEntityId(), (uuid) -> {
            NameplateImpl nameplate = new NameplateImpl(this, this.plugin::createNameplateText, player.getEntityId());
            nameplate.update(player);
            return nameplate;
        });
    }

    // Any thread
    public @Nullable NameplateImpl getNameplate(int entityId) {
        return this.nameplates.get(entityId);
    }

    // Main thread
    public void update() {
        for (Player player : getServer().getOnlinePlayers()) {
            update(player);
        }
    }

    // Main thread
    public void update(@NotNull Player player) {
        final NameplateImpl nameplate = this.getNameplate(player.getEntityId());
        if (nameplate == null) return;
        nameplate.update(player);
    }

    // Main thread
    public void init() {
        for (Player player : this.getServer().getOnlinePlayers()) {
            this.init(player);
        }
    }

    // Main thread
    public void init(@NotNull Player player) {
        this.nameplateViewershipTracker.track(player.getUniqueId());
        this.getNameplate(player);
    }

    // Main thread
    public void dispose() {
        for (Player player : this.getServer().getOnlinePlayers()) {
            dispose(player.getUniqueId(), player.getEntityId());
        }
    }

    // Main thread
    public void dispose(@NotNull UUID uuid, int entityId) {
        Collection<Integer> viewed = this.nameplateViewershipTracker.untrack(uuid);

        for (int viewedId : viewed) {
            NameplateImpl viewedNameplate = this.getNameplate(viewedId);
            if (viewedNameplate == null) continue;
            viewedNameplate.removeViewer(uuid);
        }

        NameplateImpl nameplate = this.nameplates.remove(entityId);
        if (nameplate != null) {
            nameplate.remove();
        }
    }

    public @NotNull NameplateViewershipTracker getNameplateViewershipTracker() {
        return this.nameplateViewershipTracker;
    }

    public @NotNull Server getServer() {
        return this.plugin.getServer();
    }

    public @NotNull FrostedNameplatesPlugin getPlugin() {
        return this.plugin;
    }
}
