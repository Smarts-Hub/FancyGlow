package dev.smartshub.fancyglow.plugin.listener;

import dev.smartshub.fancyglow.plugin.service.glow.GlowHandlingService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final GlowHandlingService glowHandlingService;

    public PlayerJoinListener(GlowHandlingService glowHandlingService) {
        this.glowHandlingService = glowHandlingService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        glowHandlingService.handleJoin(event.getPlayer());
    }

}
