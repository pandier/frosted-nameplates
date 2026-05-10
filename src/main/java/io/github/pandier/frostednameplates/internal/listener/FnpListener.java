package io.github.pandier.frostednameplates.internal.listener;

import io.github.pandier.frostednameplates.internal.FrostedNameplatesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class FnpListener implements Listener {
    private final FrostedNameplatesPlugin plugin;

    public FnpListener(@NotNull FrostedNameplatesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        plugin.getFn().init(event.getPlayer());
    }

    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        int entityId = event.getPlayer().getEntityId();
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getFn().dispose(entityId));
    }
}
