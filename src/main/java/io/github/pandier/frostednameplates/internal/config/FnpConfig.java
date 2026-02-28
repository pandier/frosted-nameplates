package io.github.pandier.frostednameplates.internal.config;

import io.github.pandier.frostednameplates.internal.formatter.Formatter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class FnpConfig {
    private String nameplate;
    private Formatter formatter;
    private int updateInterval;

    public void load(@NotNull FileConfiguration config) {
        nameplate = config.getString("nameplate", "%player_name%");
        formatter = Formatter.BY_NAME.getOrDefault(config.getString("formatter", "minimessage").toLowerCase(Locale.ROOT), Formatter.MINIMESSAGE);
        updateInterval = config.getInt("update-interval", 20);
    }

    @NotNull
    public String getNameplate() {
        return nameplate;
    }

    @NotNull
    public Formatter getFormatter() {
        return formatter;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }
}
