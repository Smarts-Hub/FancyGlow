package dev.smartshub.fancyglow.plugin.listener;

import dev.smartshub.fancyglow.plugin.service.glow.GlowHandlingService;
import dev.smartshub.fancyglow.plugin.service.glow.GlowPolicyService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangeWorldListener implements Listener {

    private final GlowHandlingService glowHandlingService;
    private final GlowPolicyService glowPolicyService;

    public PlayerChangeWorldListener(GlowHandlingService glowHandlingService, GlowPolicyService glowPolicyService) {
        this.glowHandlingService = glowHandlingService;
        this.glowPolicyService = glowPolicyService;
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        glowPolicyService.checkAndNotifyWorldStatus(player);

        if (glowPolicyService.isWorldDisabled(player)) {
            glowHandlingService.off(player);
        }
    }

}
