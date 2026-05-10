package io.github.pandier.frostednameplates.internal;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.pandier.frostednameplates.api.FrostedNameplates;
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
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
public final class FrostedNameplatesPlugin extends JavaPlugin implements Listener {
    private static FrostedNameplatesImpl fn = null;

    private final FnpConfig config = new FnpConfig();

    private FnpPacketListener packetListener;
    private PlaceholderAPIIntegration placeholderAPIIntegration;
    private MiniPlaceholdersIntegration miniPlaceholdersIntegration;

    private @Nullable BukkitTask updateTask;

    @Override
    public void onEnable() {
        fn = new FrostedNameplatesImpl(this);

        this.placeholderAPIIntegration = new PlaceholderAPIIntegration(this);
        this.miniPlaceholdersIntegration = new MiniPlaceholdersIntegration(this);
        this.packetListener = new FnpPacketListener(fn);

        getServer().getPluginManager().registerEvents(new FnpListener(fn), this);

        PacketEvents.getAPI().getEventManager().registerListener(this.packetListener);

        fn.init();

        saveDefaultConfig();
        reloadConfig();

        setupCommands();
    }

    @Override
    public void onDisable() {
        if (this.packetListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(this.packetListener);
        }

        if (fn != null) {
            fn.dispose();
            fn = null;
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.config.load(getConfig());

        restartUpdateTask();

        // Update all nameplates immediately
        getServer().getScheduler().runTask(this, () -> fn.update());
    }

    private void restartUpdateTask() {
        if (this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }
        int updateInterval = this.config.getUpdateInterval();
        if (updateInterval > 0) {
            this.updateTask = getServer().getScheduler().runTaskTimer(this, () -> fn.update(), updateInterval, updateInterval);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void setupCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(FnpCommand.create(fn), List.of("fnp"));
        });
    }

    public Component createNameplateText(Player player) {
        String text = placeholderAPIIntegration.setPlaceholders(player, this.config.getNameplate());
        return config.getFormatter().format(text, player, this);
    }

    public MiniPlaceholdersIntegration getMiniPlaceholdersIntegration() {
        return miniPlaceholdersIntegration;
    }

    public static FrostedNameplates getFn() {
        return fn;
    }
}
