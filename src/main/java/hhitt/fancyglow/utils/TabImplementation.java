package hhitt.fancyglow.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.PlayerGlowManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class TabImplementation {

    private final FancyGlow plugin;
    private final YamlDocument configuration;
    private final PlayerGlowManager playerGlowManager;
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

        hook();
    }

    private void hook() {
        try {
            TabAPI instance = TabAPI.getInstance();
            EventBus eventBus = Objects.requireNonNull(instance.getEventBus(), "TAB EventBus is not available.");

            plugin.getLogger().info("Successfully hooked into TAB. Automatic nametag coloring enabled.");
            
            // Register the dynamic placeholder for real-time updates (Rainbow/Flashing)
            instance.getPlaceholderManager().registerPlayerPlaceholder(
                    "%fancyglow_tab_color%",
                    100,
                    tabPlayer -> {
                        Player bukkitPlayer = (Player) tabPlayer.getPlayer();
                        return bukkitPlayer != null ? playerGlowManager.getPlayerGlowColor(bukkitPlayer) : "";
                    });

            // Automatically inject the placeholder into the player's TAB prefix on join
            eventBus.register(PlayerLoadEvent.class, event -> {
                Player player = (Player) event.getPlayer().getPlayer();
                if (player == null) return;

                // 20 tick delay ensures TAB has loaded the player's group/prefix first
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    applyTagPrefix(player);
                }, 20L);
            });

            initialized = true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into TAB: " + e.getMessage());
        }
    }

    /**
     * Uses the TabIntegration manager to force the placeholder into the prefix.
     */
    private void applyTagPrefix(Player player) {
        try {
            // This calls the method in TabIntegration that you were worried about missing!
            // It appends the color placeholder to the end of the current rank prefix.
            plugin.getGlowManager().getTabIntegration().setPlayerTeamColor(player, "%fancyglow_tab_color%");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply automatic TAB prefix: " + e.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}