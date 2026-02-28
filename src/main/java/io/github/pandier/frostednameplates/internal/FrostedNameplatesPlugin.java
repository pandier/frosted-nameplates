package io.github.pandier.frostednameplates.internal;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.pandier.frostednameplates.internal.command.FnpCommand;
import io.github.pandier.frostednameplates.internal.config.FnpConfig;
import io.github.pandier.frostednameplates.internal.integration.MiniPlaceholdersIntegration;
import io.github.pandier.frostednameplates.internal.integration.PlaceholderAPIIntegration;
import io.github.pandier.frostednameplates.internal.listener.FnpListener;
import io.github.pandier.frostednameplates.internal.packet.FnpPacketListener;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FrostedNameplatesPlugin extends JavaPlugin implements Listener {
    private final FnpConfig config = new FnpConfig();
    private final FnpPacketListener packetListener = new FnpPacketListener(this);
    private FrostedNameplatesImpl fn;

    private PlaceholderAPIIntegration placeholderAPIIntegration;
    private MiniPlaceholdersIntegration miniPlaceholdersIntegration;

    private @Nullable BukkitTask updateTask;

    @Override
    public void onEnable() {
        this.fn = new FrostedNameplatesImpl(this);
        this.placeholderAPIIntegration = new PlaceholderAPIIntegration(this);
        this.miniPlaceholdersIntegration = new MiniPlaceholdersIntegration(this);

        getServer().getPluginManager().registerEvents(new FnpListener(this), this);

        PacketEvents.getAPI().getEventManager().registerListener(packetListener);

        this.fn.init();

        saveDefaultConfig();
        reloadConfig();

        setupCommands();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);

        this.fn.dispose();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.config.load(getConfig());

        restartUpdateTask();

        // Update all nameplates immediately
        getServer().getScheduler().runTask(this, () -> this.fn.update());
    }

    private void restartUpdateTask() {
        if (this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }
        final int updateInterval = this.config.getUpdateInterval();
        if (updateInterval > 0) {
            this.updateTask = getServer().getScheduler().runTaskTimer(this, () -> this.fn.update(), updateInterval, updateInterval);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void setupCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(FnpCommand.create(this), List.of("fnp"));
        });
    }

    public @NotNull Component createNameplateText(@NotNull Player player) {
        final String text = placeholderAPIIntegration.setPlaceholders(player, this.config.getNameplate());
        return config.getFormatter().format(text, player, this);
    }

    public MiniPlaceholdersIntegration getMiniPlaceholdersIntegration() {
        return miniPlaceholdersIntegration;
    }

    public FrostedNameplatesImpl getFn() {
        return fn;
    }
}
