package io.github.pandier.frostednameplates.internal;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks what nameplates each player is viewing.
 */
public class NameplateViewershipTracker {
    private final Map<UUID, Set<Integer>> viewed = new ConcurrentHashMap<>();

    public void track(@NotNull UUID player) {
        this.viewed.put(player, ConcurrentHashMap.newKeySet());
    }

    public void show(@NotNull UUID player, int target) {
        Set<Integer> ids = this.viewed.get(player);
        if (ids == null) return;
        // TODO: The set might be removed from a different thread removed by this time.
        ids.add(target);
    }

    public void hide(@NotNull UUID player, int target) {
        Set<Integer> ids = this.viewed.get(player);
        if (ids == null) return;
        // TODO: The set might've been from a different thread removed by this time
        ids.remove(target);
    }

    public @NotNull Set<Integer> untrack(@NotNull UUID player) {
        Set<Integer> ids = this.viewed.remove(player);
        return ids != null ? ids : Set.of();
    }
}
