package dev.smartshub.fancyglow.plugin.service.glow;

import dev.smartshub.fancyglow.api.config.ConfigType;
import dev.smartshub.fancyglow.plugin.service.config.ConfigService;
import org.bukkit.entity.Player;

import java.util.List;

public class GlowPolicyService {

    private final ConfigService configService;

    public GlowPolicyService(ConfigService configService) {
        this.configService = configService;
    }

    public boolean isWorldDisabled(Player player) {
        final var config = configService.provide(ConfigType.SETTINGS);
        final var section = config.getConfigurationSection("glow-worlds");

        if (section == null) return false;

        final String mode = section.getString("mode", "blacklist").toLowerCase();
        final List<String> worlds = section.getStringList("list");
        final String currentWorld = player.getWorld().getName();

        final boolean isInList = worlds.contains(currentWorld);

        return switch (mode) {
            case "whitelist" -> !isInList;
            case "blacklist" -> isInList;
            default -> false;
        };
    }

    public boolean isPersistent() {
        return configService.provide(ConfigType.SETTINGS).getBoolean("persistent-glow", true);
    }
}
