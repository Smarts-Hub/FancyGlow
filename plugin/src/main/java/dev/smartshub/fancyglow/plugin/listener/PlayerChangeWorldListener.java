package dev.smartshub.fancyglow.plugin.listener;

import dev.smartshub.fancyglow.plugin.service.glow.GlowHandlingService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangeWorldListener implements Listener {

    private final GlowHandlingService glowHandlingService;

    public PlayerChangeWorldListener(GlowHandlingService glowHandlingService) {
        this.glowHandlingService = glowHandlingService;
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        glowHandlingService.handleWorldChange(event.getPlayer());
    }

}
