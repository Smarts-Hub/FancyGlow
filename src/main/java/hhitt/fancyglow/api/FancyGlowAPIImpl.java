package hhitt.fancyglow.api;

import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.GlowManager;
import hhitt.fancyglow.managers.PlayerGlowManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FancyGlowAPIImpl implements FancyGlowAPI {

    private final GlowManager glowManager;
    private final PlayerGlowManager playerGlowManager;

    public FancyGlowAPIImpl(FancyGlow plugin) {
        this.glowManager = plugin.getGlowManager();
        this.playerGlowManager = plugin.getPlayerGlowManager();
    }

    @Override
    public String getPlayerGlowColor(Player player) {
        return playerGlowManager.getPlayerGlowColor(player);
    }

    @Override
    public String getPlayerGlowColorName(Player player) {
        return playerGlowManager.getPlayerGlowColorName(player);
    }

    @Override
    public void setPlayerGlowColor(Player player, ChatColor color) {
        glowManager.setGlow(player, color);
    }

    @Override
    public void removePlayerGlow(Player player) {
        glowManager.removeGlow(player);
    }

    @Override
    public boolean hasGlow(Player player) {
        return playerGlowManager.findPlayerTeam(player) != null;
    }

    @Override
    public boolean hasFlashingMode(Player player) {
        return glowManager.isFlashingTaskActive(player);
    }

    @Override
    public void setFlashingMode(Player player, boolean status) {
        if (status) {
            // FIX: Was calling enableRainbow before
            glowManager.enableFlashing(player);
            return;
        }
        glowManager.disableFlashing(player);
    }

    @Override
    public boolean hasRainbowMode(Player player) {
        return glowManager.isMulticolorTaskActive(player);
    }

    @Override
    public void setRainbowMode(Player player, boolean status) {
        if (status) {
            glowManager.enableRainbow(player);
            return;
        }
        glowManager.disableRainbow(player);
    }
}