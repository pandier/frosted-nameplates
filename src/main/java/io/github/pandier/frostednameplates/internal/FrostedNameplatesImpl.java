package io.github.pandier.frostednameplates.internal;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FrostedNameplatesImpl {
    private final FrostedNameplatesPlugin plugin;
    private final NameplateViewershipTracker nameplateViewershipTracker = new NameplateViewershipTracker();
    private final Map<Integer, NameplateImpl> nameplates = new ConcurrentHashMap<>();

    public FrostedNameplatesImpl(@NotNull FrostedNameplatesPlugin plugin) {
        this.plugin = plugin;
    }

    // Main thread
    public @NotNull NameplateImpl getNameplate(@NotNull Player player) {
        return this.nameplates.computeIfAbsent(player.getEntityId(), (uuid) -> {
            NameplateImpl nameplate = new NameplateImpl(this, player.getEntityId());
            nameplate.update(player);
            return nameplate;
        });
    }

    // Any thread
    public @Nullable NameplateImpl getNameplate(int entityId) {
        return this.nameplates.get(entityId);
    }

    public void update() {
        for (Player player : getServer().getOnlinePlayers()) {
            update(player);
        }
    }

    public void update(@NotNull Player player) {
        final NameplateImpl nameplate = this.getNameplate(player.getEntityId());
        if (nameplate == null) return;
        nameplate.update(player);
    }

    public void init() {
        for (Player player : this.getServer().getOnlinePlayers()) {
            this.init(player);
        }
    }

    public void init(@NotNull Player player) {
        this.nameplateViewershipTracker.track(player.getUniqueId());
        this.getNameplate(player);
    }

    public void dispose() {
        for (Player player : this.getServer().getOnlinePlayers()) {
            dispose(player.getUniqueId(), player.getEntityId());
        }
    }

    public void dispose(@NotNull UUID uuid, int entityId) {
        NameplateImpl nameplate = this.nameplates.remove(entityId);
        if (nameplate != null) {
            nameplate.remove();
        }

        Collection<Integer> viewed = this.nameplateViewershipTracker.untrack(uuid);

        for (int viewedId : viewed) {
            NameplateImpl viewedNameplate = this.getNameplate(viewedId);
            if (viewedNameplate == null) continue;
            viewedNameplate.disposeViewer(uuid);
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
