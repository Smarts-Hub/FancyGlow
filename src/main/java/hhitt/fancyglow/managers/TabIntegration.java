package hhitt.fancyglow.managers;

import hhitt.fancyglow.FancyGlow;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
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

    public TabIntegration(FancyGlow plugin) {
        this.plugin = plugin;
        this.isTabAvailable = plugin.getServer().getPluginManager().getPlugin("TAB") != null;
        
        if (isTabAvailable) {
            try {
                this.tabAPI = TabAPI.getInstance();
                this.nameTagManager = tabAPI.getNameTagManager();
                
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
     * This prevents TAB from overriding the glow color by ensuring the team
     * color matches the glow effect.
     */
    public void setPlayerTeamColor(Player player, ChatColor color) {
        if (!isTabAvailable || tabAPI == null || nameTagManager == null) {
            return;
        }

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) {
                return;
            }

            // Get the original prefix from TAB configuration
            String originalPrefix = nameTagManager.getOriginalPrefix(tabPlayer);
            if (originalPrefix == null) {
                originalPrefix = "";
            }

            // Append the color code to the end of the prefix to set the team color
            // The last color in the prefix determines the nametag/glow color
            String modifiedPrefix = originalPrefix + color.toString();
            
            // Set the custom prefix in TAB
            nameTagManager.setPrefix(tabPlayer, modifiedPrefix);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set TAB team color for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Resets the player's nametag prefix in TAB to the original value.
     * This removes the custom glow color applied by FancyGlow.
     */
    public void resetPlayerTeamColor(Player player) {
        if (!isTabAvailable || tabAPI == null || nameTagManager == null) {
            return;
        }

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) {
                return;
            }

            // Reset to original prefix by setting it to null
            nameTagManager.setPrefix(tabPlayer, null);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to reset TAB team color for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Pauses TAB's team handling for a player.
     * This is useful when you want complete control over the player's team
     * without TAB interfering.
     */
    public void pauseTeamHandling(Player player) {
        if (!isTabAvailable || tabAPI == null || nameTagManager == null) {
            return;
        }

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) {
                return;
            }

            nameTagManager.pauseTeamHandling(tabPlayer);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to pause TAB team handling for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Resumes TAB's team handling for a player.
     */
    public void resumeTeamHandling(Player player) {
        if (!isTabAvailable || tabAPI == null || nameTagManager == null) {
            return;
        }

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) {
                return;
            }

            nameTagManager.resumeTeamHandling(tabPlayer);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to resume TAB team handling for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Checks if TAB integration is available and working.
     */
    public boolean isAvailable() {
        return isTabAvailable && tabAPI != null && nameTagManager != null;
    }

    /**
     * Gets the TabAPI instance.
     */
    public TabAPI getTabAPI() {
        return tabAPI;
    }

    /**
     * Gets the NameTagManager instance.
     */
    public NameTagManager getNameTagManager() {
        return nameTagManager;
    }
}