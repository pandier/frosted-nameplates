package io.github.pandier.frostednameplates.internal;

import io.github.pandier.frostednameplates.api.FrostedNameplates;
import io.github.pandier.frostednameplates.internal.packet.render.NameplateRenderer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
public class FrostedNameplatesImpl implements FrostedNameplates {
    private final FrostedNameplatesPlugin plugin;
    private final NameplateRenderer nameplateRenderer = new NameplateRenderer();
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

    // Thread-safe
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
        this.getNameplate(player);
    }

    public void dispose() {
        for (Player player : this.getServer().getOnlinePlayers()) {
            dispose(player.getEntityId());
        }
    }

    public void dispose(int entityId) {
        NameplateImpl nameplate = this.nameplates.remove(entityId);
        if (nameplate != null) {
            nameplate.remove();
        }
    }

    public @NotNull NameplateRenderer getNameplateRenderer() {
        return nameplateRenderer;
    }

    public @NotNull Server getServer() {
        return this.plugin.getServer();
    }

    public @NotNull FrostedNameplatesPlugin getPlugin() {
        return this.plugin;
    }
}
