package io.github.pandier.frostednameplates;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class FnpConfig {
    private String nameplate;
    private int updateInterval;

    public void load(@NotNull FileConfiguration config) {
        nameplate = ChatColor.translateAlternateColorCodes('&', config.getString("nameplate", "%player_name%"));
        updateInterval = config.getInt("update-interval", 20);
    }

    @NotNull
    public String getNameplate() {
        return nameplate;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }
}
