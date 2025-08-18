package hhitt.fancyglow.listeners;

import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.GlowManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final GlowManager glowManager;

    public PlayerJoinListener(FancyGlow plugin) {
        this.glowManager = plugin.getGlowManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("fancyglow.all_colors")) {
            glowManager.removeGlow(player);
        }
    }
}
