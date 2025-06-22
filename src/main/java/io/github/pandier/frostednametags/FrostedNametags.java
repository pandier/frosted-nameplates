package io.github.pandier.frostednametags;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.github.pandier.frostednametags.util.PacketConsumer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.*;

public final class FrostedNametags extends JavaPlugin implements Listener {
    private final FrostedNametagsConfig config = new FrostedNametagsConfig();

    private final Map<ArmorStandNametag, List<UUID>> viewers = new HashMap<>();
    private final Map<UUID, List<ArmorStandNametag>> viewed = new HashMap<>();
    private final Map<Integer, ArmorStandNametag> nametags = new HashMap<>();

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        saveDefaultConfig();
        reloadConfig();

        this.protocolManager = ProtocolLibrary.getProtocolManager();
        new FrostedNametagsPacketAdapters(this, this.protocolManager);

        // TODO: Ability to configure this
        getServer().getScheduler().runTaskTimer(this, this::updateNametags, 100, 100);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.config.load(getConfig());
    }

    private @Nullable Player getPlayerFromId(@NotNull World world, int playerId) {
        final Entity entity = protocolManager.getEntityFromID(world, playerId);
        if (!(entity instanceof Player player)) return null;
        return player;
    }

    private @NotNull String createNametagText(@NotNull Player player) {
        return PlaceholderAPI.setPlaceholders(player, this.config.getNametag());
    }

    @NotNull ArmorStandNametag getOrCreateNametag(@NotNull World world, int targetEntityId) {
        return nametags.computeIfAbsent(targetEntityId, k -> {
            final ArmorStandNametag nametag = new ArmorStandNametag(targetEntityId);
            final Player player = getPlayerFromId(world, targetEntityId);
            if (player != null) nametag.setText(createNametagText(player));
            return nametag;
        });
    }

    @Nullable ArmorStandNametag getNametag(int targetEntityId) {
        return nametags.get(targetEntityId);
    }

    void removeNametag(int targetEntityId) {
        final ArmorStandNametag nametag = this.nametags.remove(targetEntityId);
        if (nametag == null) return;
        final List<UUID> viewers = this.viewers.remove(nametag);
        if (viewers != null) {
            for (UUID viewerUuid : viewers) {
                this.viewed.getOrDefault(viewerUuid, new ArrayList<>()).remove(nametag);
                final Player player = getServer().getPlayer(viewerUuid);
                if (player == null) continue;
                nametag.sendRemovePackets(PacketConsumer.player(player));
            }
        }
    }

    void updateNametags() {
        for (Player player : getServer().getOnlinePlayers()) {
            updateNametag(player);
        }
    }

    void updateNametag(@NotNull Player player) {
        final String text = createNametagText(player);
        final ArmorStandNametag nametag = getNametag(player.getEntityId());
        if (nametag == null) return;
        final List<UUID> viewers = this.viewers.get(nametag);
        if (viewers == null) return;
        viewers.removeIf(viewerUuid -> {
            final Player viewer = getServer().getPlayer(viewerUuid);
            if (viewer == null) return true;
            if (nametag.getText().equals(text)) return false;
            nametag.setText(text);
            nametag.sendUpdatePackets(PacketConsumer.player(viewer));
            return false;
        });
    }

    void showNametag(@NotNull Player player, @NotNull PacketConsumer packetConsumer, int targetEntityId, @NotNull Vector3d position) {
        final ArmorStandNametag nametag = getOrCreateNametag(player.getWorld(), targetEntityId);
        this.viewers.computeIfAbsent(nametag, k -> new ArrayList<>()).add(player.getUniqueId());
        this.viewed.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(nametag);
        nametag.sendSpawnPackets(packetConsumer, position);
    }

    void hideNametag(@NotNull Player player, @NotNull PacketConsumer packetConsumer, int targetEntityId) {
        final ArmorStandNametag nametag = getNametag(targetEntityId);
        if (nametag == null) return;
        this.viewers.getOrDefault(nametag, new ArrayList<>()).remove(player.getUniqueId());
        this.viewed.getOrDefault(player.getUniqueId(), new ArrayList<>()).remove(nametag);
        nametag.sendRemovePackets(packetConsumer);
    }

    private void dispose(@NotNull Player player) {
        final List<ArmorStandNametag> viewedNametags = this.viewed.remove(player.getUniqueId());
        if (viewedNametags != null) {
            for (ArmorStandNametag nametag : viewedNametags) {
                this.viewers.getOrDefault(nametag, new ArrayList<>()).remove(player.getUniqueId());
            }
        }
        removeNametag(player.getEntityId());
    }

    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        dispose(event.getPlayer());
    }
}
