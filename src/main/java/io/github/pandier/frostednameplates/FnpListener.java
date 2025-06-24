package io.github.pandier.frostednameplates;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class FnpListener implements Listener {
    private final FrostedNameplates plugin;

    public FnpListener(@NotNull FrostedNameplates plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        plugin.dispose(event.getPlayer());
    }
}
