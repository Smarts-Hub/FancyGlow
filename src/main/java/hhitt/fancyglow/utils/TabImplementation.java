package hhitt.fancyglow.utils;

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
    private final PlayerGlowManager playerGlowManager;
    private boolean initialized = false;

    public TabImplementation(FancyGlow plugin) {
        this.plugin = plugin;
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

            // Always register the placeholder so %fancyglow_tab_color% is available
            // regardless of whether Auto_Tag is enabled. Users who put the placeholder
            // directly in TAB's groups.yml need it to resolve even with Auto_Tag: false.
            instance.getPlaceholderManager().registerPlayerPlaceholder(
                    "%fancyglow_tab_color%",
                    100,
                    tabPlayer -> {
                        Player bukkitPlayer = (Player) tabPlayer.getPlayer();
                        return bukkitPlayer != null ? playerGlowManager.getPlayerGlowColor(bukkitPlayer) : "";
                    });

            boolean autoTag = plugin.getConfiguration().getBoolean("Auto_Tag", false);

            if (!autoTag) {
                // Auto_Tag is disabled: the placeholder (%fancyglow_tab_color% or the
                // PlaceholderAPI %fancyglow_color%) should be placed directly in TAB's
                // groups.yml tabprefix/tagprefix. TAB resolves it dynamically, so group
                // changes via LuckPerms apply instantly without /tab reload.
                plugin.getLogger().info("Successfully hooked into TAB. Auto_Tag is disabled — put %fancyglow_tab_color% in your TAB groups.yml prefixes.");
                initialized = true;
                return;
            }

            // Auto_Tag: true — inject the placeholder into the player's TAB prefix
            // automatically on every join/reload.
            // NOTE: This sets a TAB API prefix override which freezes the displayed
            // prefix. If a player's LuckPerms group changes while they are online, the
            // new group prefix will not appear until the next /tab reload or re-login.
            // If you use TAB groups.yml with per-group prefixes containing
            // %fancyglow_tab_color%, set Auto_Tag to false to avoid this.
            EventBus eventBus = Objects.requireNonNull(instance.getEventBus(), "TAB EventBus is not available.");

            plugin.getLogger().info("Successfully hooked into TAB. Auto_Tag is enabled — glow color will be injected into TAB prefixes automatically.");

            eventBus.register(PlayerLoadEvent.class, event -> {
                Player player = (Player) event.getPlayer().getPlayer();
                if (player == null) return;

                // 20 tick delay ensures TAB has loaded the player's group/prefix first
                Bukkit.getScheduler().runTaskLater(plugin, () -> applyTagPrefix(player), 20L);
            });

            initialized = true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into TAB: " + e.getMessage());
        }
    }

    /**
     * Appends the glow color placeholder to the player's TAB nametag/tablist prefix.
     * Only called when Auto_Tag is enabled.
     */
    private void applyTagPrefix(Player player) {
        try {
            plugin.getGlowManager().getTabIntegration().setPlayerTeamColor(player, "%fancyglow_tab_color%");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply automatic TAB prefix: " + e.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}