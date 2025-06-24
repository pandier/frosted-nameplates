package io.github.pandier.frostednametags;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class FrostedNametagsConfig {
    private String nametag;
    private int updateInterval;

    public void load(@NotNull FileConfiguration config) {
        nametag = ChatColor.translateAlternateColorCodes('&', config.getString("nametag", "%player_name%"));
        updateInterval = config.getInt("update-interval", 20);
    }

    @NotNull
    public String getNametag() {
        return nametag;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }
}
