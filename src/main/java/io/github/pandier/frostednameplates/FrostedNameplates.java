package io.github.pandier.frostednameplates;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import io.github.pandier.frostednameplates.config.FnpConfig;
import io.github.pandier.frostednameplates.integration.MiniPlaceholdersIntegration;
import io.github.pandier.frostednameplates.integration.PlaceholderAPIIntegration;
import io.github.pandier.frostednameplates.util.PacketConsumer;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class FrostedNameplates extends JavaPlugin implements Listener {
    private static final String MINIMUM_MINECRAFT_VERSION = "1.19.4";

    private final FnpConfig config = new FnpConfig();
    private final FnpPacketListener packetListener = new FnpPacketListener(this);

    private PlaceholderAPIIntegration placeholderAPIIntegration;
    private MiniPlaceholdersIntegration miniPlaceholdersIntegration;

    private final Map<Nameplate, List<UUID>> viewers = new ConcurrentHashMap<>();
    private final Map<UUID, List<Nameplate>> viewed = new ConcurrentHashMap<>();
    private final Map<Integer, Nameplate> nameplates = new ConcurrentHashMap<>();
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
        this.placeholderAPIIntegration = new PlaceholderAPIIntegration(this);
        this.miniPlaceholdersIntegration = new MiniPlaceholdersIntegration(this);

        getServer().getPluginManager().registerEvents(new FnpListener(this), this);

        PacketEvents.getAPI().getEventManager().registerListener(packetListener);

        saveDefaultConfig();
        reloadConfig();

        setupCommands();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);

        for (Player player : getServer().getOnlinePlayers()) {
            removeNameplate(player.getEntityId());
        }

        this.viewers.clear();
        this.viewed.clear();
        this.nameplates.clear();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.config.load(getConfig());

        restartUpdateTask();

        // Update all nameplates immediately
        getServer().getScheduler().runTask(this, this::updateNameplates);
    }

    private void restartUpdateTask() {
        if (this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }
        final int updateInterval = this.config.getUpdateInterval();
        if (updateInterval > 0) {
            this.updateTask = getServer().getScheduler().runTaskTimer(this, this::updateNameplates, updateInterval, updateInterval);
        }
    }

    private void setupCommands() {
        final PluginCommand command = getCommand("frostednameplates");
        if (command == null) throw new IllegalStateException("Missing command 'frostednameplates'");
        final FnpCommand instance = new FnpCommand(this);
        command.setExecutor(instance);
        command.setTabCompleter(instance);
    }

    private @NotNull Component createNameplateText(@NotNull Player player) {
        final String text = placeholderAPIIntegration.setPlaceholders(player, this.config.getNameplate());
        return config.getFormatter().format(text, player, this);
    }

    private @Nullable Player getPlayerFromId(@NotNull World world, int playerId) {
        final Entity entity = SpigotConversionUtil.getEntityById(world, playerId);
        if (!(entity instanceof Player player)) return null;
        return player;
    }

    @NotNull Nameplate getOrCreateNameplate(@NotNull World world, int targetEntityId) {
        return nameplates.computeIfAbsent(targetEntityId, k -> {
            final Nameplate nameplate = new Nameplate(targetEntityId);
            final Player player = getPlayerFromId(world, targetEntityId);
            if (player != null) nameplate.setText(createNameplateText(player));
            return nameplate;
        });
    }

    @Nullable Nameplate getNameplate(int targetEntityId) {
        return nameplates.get(targetEntityId);
    }

    void removeNameplate(int targetEntityId) {
        final Nameplate nameplate = this.nameplates.remove(targetEntityId);
        if (nameplate == null) return;
        final List<UUID> viewers = this.viewers.remove(nameplate);
        if (viewers != null) {
            for (UUID viewerUuid : viewers) {
                // This can happen somehow...
                if (viewerUuid == null) continue;
                this.viewed.getOrDefault(viewerUuid, new ArrayList<>()).remove(nameplate);
                final Player viewer = getServer().getPlayer(viewerUuid);
                if (viewer == null) continue;
                nameplate.sendRemovePackets(PacketConsumer.player(viewer));
            }
        }
    }

    void updateNameplates() {
        for (Player player : getServer().getOnlinePlayers()) {
            updateNameplate(player);
        }
    }

    void updateNameplate(@NotNull Player player) {
        final Nameplate nameplate = getNameplate(player.getEntityId());
        if (nameplate == null) return;
        final List<UUID> viewers = this.viewers.get(nameplate);
        if (viewers == null || viewers.isEmpty()) return;
        final Component text = createNameplateText(player);
        if (nameplate.getText().equals(text)) return;
        nameplate.setText(text);
        viewers.removeIf(viewerUuid -> {
            // This can happen somehow...
            if (viewerUuid == null) return true;
            final Player viewer = getServer().getPlayer(viewerUuid);
            if (viewer == null) return true;
            nameplate.sendUpdatePackets(PacketConsumer.player(viewer));
            return false;
        });
    }

    void showNameplate(@NotNull PacketConsumer packetConsumer, @NotNull World world, int targetEntityId, @NotNull Vector3d position) {
        final Nameplate nameplate = getOrCreateNameplate(world, targetEntityId);
        this.viewers.computeIfAbsent(nameplate, k -> new ArrayList<>()).add(packetConsumer.getUUID());
        this.viewed.computeIfAbsent(packetConsumer.getUUID(), k -> new ArrayList<>()).add(nameplate);
        nameplate.sendSpawnPackets(packetConsumer, position);
    }

    void hideNameplate(@NotNull PacketConsumer packetConsumer, int targetEntityId) {
        final Nameplate nameplate = getNameplate(targetEntityId);
        if (nameplate == null) return;
        this.viewers.getOrDefault(nameplate, new ArrayList<>()).remove(packetConsumer.getUUID());
        this.viewed.getOrDefault(packetConsumer.getUUID(), new ArrayList<>()).remove(nameplate);
        nameplate.sendRemovePackets(packetConsumer);
    }

    void dispose(@NotNull Player player) {
        final List<Nameplate> viewedNameplates = this.viewed.remove(player.getUniqueId());
        if (viewedNameplates != null) {
            for (Nameplate nameplate : viewedNameplates) {
                this.viewers.getOrDefault(nameplate, new ArrayList<>()).remove(player.getUniqueId());
            }
        }
        removeNameplate(player.getEntityId());
    }

    public MiniPlaceholdersIntegration getMiniPlaceholdersIntegration() {
        return miniPlaceholdersIntegration;
    }
}
