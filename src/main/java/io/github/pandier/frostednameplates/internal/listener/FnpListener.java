package io.github.pandier.frostednameplates.internal.listener;

import io.github.pandier.frostednameplates.internal.FrostedNameplatesImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class FnpListener implements Listener {
    private final FrostedNameplatesImpl fn;

    public FnpListener(@NotNull FrostedNameplatesImpl fn) {
        this.fn = fn;
    }

    @EventHandler
    private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        this.fn.init(event.getPlayer());
    }

    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        int entityId = event.getPlayer().getEntityId();
        this.fn.getServer().getScheduler().runTask(this.fn.getPlugin(), () -> this.fn.dispose(entityId));
    }
}
