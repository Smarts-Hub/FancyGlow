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

    public TabIntegration(FancyGlow plugin) {
        this.plugin = plugin;
        this.isTabAvailable = plugin.getServer().getPluginManager().getPlugin("TAB") != null;

        if (isTabAvailable && getNameTagManager() == null) {
            plugin.getLogger().warning("TAB NameTagManager is disabled. FancyGlow glow colors may be overridden by TAB.");
        }
    }

    /**
     * Returns the current TAB API instance, or {@code null} if TAB is not available.
     *
     * <p>Managers (NameTagManager, TabListFormatManager) are intentionally resolved fresh
     * on every call rather than cached at construction time. When TAB is reloaded via
     * {@code /tab reload}, it discards all feature instances (marking them inactive) and
     * creates new ones. Any reference cached before the reload becomes stale and throws
     * {@code IllegalStateException: This instance got discarded because plugin was reloaded.
     * Obtain a new instance.} on the next use. Fetching a fresh reference on every call
     * avoids this without any performance concern, since the lookup is a simple map get.
     */
    private TabAPI getTabApiInstance() {
        if (!isTabAvailable) return null;
        try {
            return TabAPI.getInstance();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets the player's nametag/tablist prefix in TAB to include the glow color.
     * Overloaded to accept a String for placeholder injection.
     */
    public void setPlayerTeamColor(Player player, String colorCode) {
        TabAPI tabAPI = getTabApiInstance();
        if (tabAPI == null) return;

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) return;

            // 1. Handle nametag (above head)
            NameTagManager nameTagManager = tabAPI.getNameTagManager();
            if (nameTagManager != null) {
                String originalPrefix = nameTagManager.getOriginalPrefix(tabPlayer);
                if (originalPrefix == null) originalPrefix = "";
                nameTagManager.setPrefix(tabPlayer, originalPrefix + colorCode);
            }

            // 2. Handle tablist (player list)
            TabListFormatManager tabListFormatManager = tabAPI.getTabListFormatManager();
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
     * Resets the player's nametag/tablist prefix in TAB to the original group value.
     * This removes the custom glow color applied by FancyGlow.
     */
    public void resetPlayerTeamColor(Player player) {
        TabAPI tabAPI = getTabApiInstance();
        if (tabAPI == null) return;

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) return;

            NameTagManager nameTagManager = tabAPI.getNameTagManager();
            TabListFormatManager tabListFormatManager = tabAPI.getTabListFormatManager();
            if (nameTagManager != null) nameTagManager.setPrefix(tabPlayer, null);
            if (tabListFormatManager != null) tabListFormatManager.setPrefix(tabPlayer, null);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to reset TAB team color for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void pauseTeamHandling(Player player) {
        TabAPI tabAPI = getTabApiInstance();
        if (tabAPI == null) return;
        try {
            NameTagManager nameTagManager = tabAPI.getNameTagManager();
            if (nameTagManager == null) return;
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer != null) nameTagManager.pauseTeamHandling(tabPlayer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to pause TAB team handling: " + e.getMessage());
        }
    }

    public void resumeTeamHandling(Player player) {
        TabAPI tabAPI = getTabApiInstance();
        if (tabAPI == null) return;
        try {
            NameTagManager nameTagManager = tabAPI.getNameTagManager();
            if (nameTagManager == null) return;
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer != null) nameTagManager.resumeTeamHandling(tabPlayer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to resume TAB team handling: " + e.getMessage());
        }
    }

    public boolean isAvailable() {
        return getTabApiInstance() != null;
    }

    public TabAPI getTabAPI() {
        return getTabApiInstance();
    }

    public NameTagManager getNameTagManager() {
        TabAPI tabAPI = getTabApiInstance();
        return tabAPI == null ? null : tabAPI.getNameTagManager();
    }
}
