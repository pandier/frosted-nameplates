package io.github.pandier.frostednameplates.internal;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.github.pandier.frostednameplates.internal.command.FnpCommand;
import io.github.pandier.frostednameplates.internal.config.FnpConfig;
import io.github.pandier.frostednameplates.internal.integration.MiniPlaceholdersIntegration;
import io.github.pandier.frostednameplates.internal.integration.PlaceholderAPIIntegration;
import io.github.pandier.frostednameplates.internal.listener.FnpListener;
import io.github.pandier.frostednameplates.internal.packet.FnpPacketListener;
import net.kyori.adventure.text.Component;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FrostedNameplatesPlugin extends JavaPlugin implements Listener {
    private static final String MINIMUM_MINECRAFT_VERSION = "1.19.4";

    private final FnpConfig config = new FnpConfig();
    private final FnpPacketListener packetListener = new FnpPacketListener(this);
    private FrostedNameplatesImpl fn;

    private PlaceholderAPIIntegration placeholderAPIIntegration;
    private MiniPlaceholdersIntegration miniPlaceholdersIntegration;

    private @Nullable BukkitTask updateTask;

    @Override
    public void onLoad() {
        // Check the Minecraft version
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_19_4))
            throw new IllegalStateException("FrostedNameplates requires Minecraft " + MINIMUM_MINECRAFT_VERSION + " or higher");

        // Check if we are on Paper
        try {
            Class.forName("io.papermc.paper.event.entity.EntityMoveEvent");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("FrostedNameplates requires Paper");
        }
    }

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

    private void setupCommands() {
        final PluginCommand command = getCommand("frostednameplates");
        if (command == null) throw new IllegalStateException("Missing command 'frostednameplates'");
        final FnpCommand instance = new FnpCommand(this);
        command.setExecutor(instance);
        command.setTabCompleter(instance);
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
