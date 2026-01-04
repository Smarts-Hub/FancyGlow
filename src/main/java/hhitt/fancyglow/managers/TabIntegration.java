package hhitt.fancyglow.managers;

import hhitt.fancyglow.FancyGlow;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Handles integration with the TAB plugin to synchronize glow colors with TAB nametags.
 * This prevents TAB from overriding glow colors set by FancyGlow by setting the TAB
 * prefix to include the glow color.
 */
public class TabIntegration {

    private final FancyGlow plugin;
    private final boolean isTabAvailable;
    private TabAPI tabAPI;
    private NameTagManager nameTagManager;
    private TabListFormatManager tabListFormatManager;

    public TabIntegration(FancyGlow plugin) {
        this.plugin = plugin;
        this.isTabAvailable = plugin.getServer().getPluginManager().getPlugin("TAB") != null;
        
        if (isTabAvailable) {
            try {
                this.tabAPI = TabAPI.getInstance();
                this.nameTagManager = tabAPI.getNameTagManager();
                this.tabListFormatManager = tabAPI.getTabListFormatManager();
                
                if (nameTagManager == null) {
                    plugin.getLogger().warning("TAB NameTagManager is disabled. FancyGlow glow colors may be overridden by TAB.");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("TAB is installed but API is not available: " + e.getMessage());
            }
        }
    }

    /**
     * Sets the player's nametag prefix in TAB to include the glow color.
     * Overloaded to accept String for placeholder injection.
     */
    public void setPlayerTeamColor(Player player, String colorCode) {
        if (!isTabAvailable || tabAPI == null) {
            return;
        }

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) return;

            // 1. Handle Nametags (Above Head)
            if (nameTagManager != null) {
                String originalPrefix = nameTagManager.getOriginalPrefix(tabPlayer);
                if (originalPrefix == null) originalPrefix = "";
                nameTagManager.setPrefix(tabPlayer, originalPrefix + colorCode);
            }

            // 2. Handle Tablist (The player list)
            if (tabListFormatManager != null) {
                String originalPrefix = tabListFormatManager.getOriginalPrefix(tabPlayer);
                if (originalPrefix == null) originalPrefix = "";
                tabListFormatManager.setPrefix(tabPlayer, originalPrefix + colorCode);
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set TAB team color for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Original method signature for compatibility with GlowManager.
     */
    public void setPlayerTeamColor(Player player, ChatColor color) {
        setPlayerTeamColor(player, color.toString());
    }

    /**
     * Resets the player's nametag prefix in TAB to the original value.
     * This removes the custom glow color applied by FancyGlow.
     */
    public void resetPlayerTeamColor(Player player) {
        if (!isTabAvailable || tabAPI == null) {
            return;
        }

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) return;

            // Reset both Managers by passing null
            if (nameTagManager != null) nameTagManager.setPrefix(tabPlayer, null);
            if (tabListFormatManager != null) tabListFormatManager.setPrefix(tabPlayer, null);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to reset TAB team color for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void pauseTeamHandling(Player player) {
        if (!isTabAvailable || tabAPI == null || nameTagManager == null) return;
        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer != null) nameTagManager.pauseTeamHandling(tabPlayer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to pause TAB team handling: " + e.getMessage());
        }
    }

    public void resumeTeamHandling(Player player) {
        if (!isTabAvailable || tabAPI == null || nameTagManager == null) return;
        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer != null) nameTagManager.resumeTeamHandling(tabPlayer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to resume TAB team handling: " + e.getMessage());
        }
    }

    public boolean isAvailable() {
        return isTabAvailable && tabAPI != null;
    }

    public TabAPI getTabAPI() {
        return tabAPI;
    }

    public NameTagManager getNameTagManager() {
        return nameTagManager;
    }
}