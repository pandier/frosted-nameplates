package io.github.pandier.frostednameplates;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.github.pandier.frostednameplates.util.PacketConsumer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.*;

public final class FrostedNameplates extends JavaPlugin implements Listener {
    private final FnpConfig config = new FnpConfig();

    private final Map<Nameplate, List<UUID>> viewers = new HashMap<>();
    private final Map<UUID, List<Nameplate>> viewed = new HashMap<>();
    private final Map<Integer, Nameplate> nameplates = new HashMap<>();

    private ProtocolManager protocolManager;
    private @Nullable BukkitTask updateTask;

    @Override
    public void onEnable() {
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        getServer().getPluginManager().registerEvents(new FnpListener(this), this);
        new FnpPacketAdapters(this).register(this.protocolManager);

        saveDefaultConfig();
        reloadConfig();

        setupCommands();
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

    private @NotNull String createNameplateText(@NotNull Player player) {
        return PlaceholderAPI.setPlaceholders(player, this.config.getNameplate());
    }

    private @Nullable Player getPlayerFromId(@NotNull World world, int playerId) {
        final Entity entity = protocolManager.getEntityFromID(world, playerId);
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
                this.viewed.getOrDefault(viewerUuid, new ArrayList<>()).remove(nameplate);
                final Player player = getServer().getPlayer(viewerUuid);
                if (player == null) continue;
                nameplate.sendRemovePackets(PacketConsumer.player(player));
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
        final String text = createNameplateText(player);
        viewers.removeIf(viewerUuid -> {
            final Player viewer = getServer().getPlayer(viewerUuid);
            if (viewer == null) return true;
            if (nameplate.getText().equals(text)) return false;
            nameplate.setText(text);
            nameplate.sendUpdatePackets(PacketConsumer.player(viewer));
            return false;
        });
    }

    void showNameplate(@NotNull Player player, @NotNull PacketConsumer packetConsumer, int targetEntityId, @NotNull Vector3d position) {
        final Nameplate nameplate = getOrCreateNameplate(player.getWorld(), targetEntityId);
        this.viewers.computeIfAbsent(nameplate, k -> new ArrayList<>()).add(player.getUniqueId());
        this.viewed.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(nameplate);
        nameplate.sendSpawnPackets(packetConsumer, position);
    }

    void hideNameplate(@NotNull Player player, @NotNull PacketConsumer packetConsumer, int targetEntityId) {
        final Nameplate nameplate = getNameplate(targetEntityId);
        if (nameplate == null) return;
        this.viewers.getOrDefault(nameplate, new ArrayList<>()).remove(player.getUniqueId());
        this.viewed.getOrDefault(player.getUniqueId(), new ArrayList<>()).remove(nameplate);
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
}
