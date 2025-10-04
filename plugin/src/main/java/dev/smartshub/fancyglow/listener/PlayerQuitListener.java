package dev.smartshub.fancyglow.listener;

import dev.smartshub.fancyglow.service.glow.GlowHandlingService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final GlowHandlingService glowHandlingService;

    public PlayerQuitListener(GlowHandlingService glowHandlingService) {
        this.glowHandlingService = glowHandlingService;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        glowHandlingService.handleLeave(event.getPlayer());
    }

}
