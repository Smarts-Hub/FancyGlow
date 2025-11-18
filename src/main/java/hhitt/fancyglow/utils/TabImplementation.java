package hhitt.fancyglow.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.PlayerGlowManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import me.neznamy.tab.api.nametag.NameTagManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class TabImplementation {

    private final FancyGlow plugin;
    private final YamlDocument configuration;
    private final PlayerGlowManager playerGlowManager;

    private String tabVersion;
    private boolean initialized = false;

    public TabImplementation(FancyGlow plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfiguration();
        this.playerGlowManager = plugin.getPlayerGlowManager();
    }

    public void initialize() {
        Plugin tabPlugin = plugin.getServer().getPluginManager().getPlugin("TAB");
        if (tabPlugin == null || !tabPlugin.isEnabled()) {
            return;
        }

        tabVersion = tabPlugin.getDescription().getVersion();
        if (!isCompatibleTAB(stripTags(tabVersion), "5.0.4")) {
            plugin.getLogger().warning("TAB implementation won't work due to version incompatibility.");
            plugin.getLogger().warning("You need at least version 5.0.4 or newer. Current version: " + tabVersion);
            return;
        }

        hook();
    }

    private void hook() {
        boolean autoTag = configuration.getBoolean("Auto_Tag", false);
        
        plugin.getLogger().info("Compatible version of TAB " + tabVersion + " has been found.");
        
        if (!autoTag) {
            plugin.getLogger().info("You can enable the Auto_Tag option in your config to automatically apply glow colors to TAB prefixes.");
            plugin.getLogger().info("When enabled, TAB prefixes will be modified to include the glow color.");
            return;
        }

        try {
            TabAPI instance = TabAPI.getInstance();
            EventBus eventBus = Objects.requireNonNull(instance.getEventBus(), "TAB EventBus is not available.");

            plugin.getLogger().info("Auto_Tag is enabled. TAB will be used to display glow colors.");
            
            // Register TAB listener for auto-tagging
            eventBus.register(PlayerLoadEvent.class, event -> {
                if (!event.isJoin()) {
                    applyTagPrefix(event, instance);
                    return;
                }

                // Delay for join events to ensure everything is loaded
                Bukkit.getScheduler().runTaskLater(plugin, () -> applyTagPrefix(event, instance), 15L);
            });

            initialized = true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into TAB: " + e.getMessage());
        }
    }

    private void applyTagPrefix(PlayerLoadEvent event, TabAPI instance) {
        try {
            int ticks = plugin.getConfiguration().getInt("Rainbow_Update_Interval", 10);
            if (ticks <= 0) {
                ticks = 1;
            }

            // Register placeholder for glow color
            instance.getPlaceholderManager().registerPlayerPlaceholder(
                    "%fancyglow_tab_color%",
                    50 * ticks,
                    player -> {
                        Player bukkitPlayer = (Player) player.getPlayer();
                        return bukkitPlayer != null ? playerGlowManager.getPlayerGlowColor(bukkitPlayer) : "";
                    });

            NameTagManager nameTagManager = Objects.requireNonNull(
                    instance.getNameTagManager(), 
                    "TAB NameTagManager is unavailable."
            );
            
            String originalPrefix = nameTagManager.getOriginalPrefix(event.getPlayer());
            if (originalPrefix == null) {
                originalPrefix = "";
            }

            // Append the glow color placeholder to the prefix
            String modifiedPrefix = originalPrefix + "%fancyglow_tab_color%";
            nameTagManager.setPrefix(event.getPlayer(), modifiedPrefix);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply TAB prefix for player: " + e.getMessage());
        }
    }

    private boolean isCompatibleTAB(String tabVersion, String desiredVersion) {
        if (tabVersion.equals(desiredVersion)) return true;

        try {
            String[] versionParts = tabVersion.split("\\.");
            String[] desiredVersionParts = desiredVersion.split("\\.");

            for (int i = 0; i < Math.min(versionParts.length, desiredVersionParts.length); i++) {
                int current = Integer.parseInt(versionParts[i]);
                int required = Integer.parseInt(desiredVersionParts[i]);

                if (current != required) {
                    return current > required;
                }
            }

            return true;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            plugin.getLogger().warning("Failed to parse TAB version numbers: " + tabVersion);
            return false;
        }
    }

    public static String stripTags(final String version) {
        return version.replaceAll("[-;+].+", "");
    }

    public boolean isInitialized() {
        return initialized;
    }
}