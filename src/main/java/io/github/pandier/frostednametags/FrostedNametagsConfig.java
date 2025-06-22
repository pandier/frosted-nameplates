package io.github.pandier.frostednametags;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class FrostedNametagsConfig {
    private String nametag;

    public void load(@NotNull FileConfiguration config) {
        nametag = ChatColor.translateAlternateColorCodes('&', config.getString("nametag", "%player_name%"));
    }

    @NotNull
    public String getNametag() {
        return nametag;
    }
}
