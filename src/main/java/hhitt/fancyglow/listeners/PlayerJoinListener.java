package hhitt.fancyglow.listeners;

import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.GlowManager;
import hhitt.fancyglow.managers.PlayerGlowManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Team;

public class PlayerJoinListener implements Listener {

    private final GlowManager glowManager;
    private final PlayerGlowManager playerGlowManager;

    public PlayerJoinListener(FancyGlow plugin) {
        this.glowManager = plugin.getGlowManager();
        this.playerGlowManager = plugin.getPlayerGlowManager();
    }

    /*
     * This used to listen to PlayerQuitEvent (despite its name) and wiped the glow of every player without
     * 'fancyglow.all_colors' when they left, which broke Persistent_Mode for normal players.
     * It now runs on join and only removes a persisted glow the player is no longer allowed to have.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Team team = playerGlowManager.findPlayerTeam(player);
        if (team != null && !player.hasPermission("fancyglow.all_colors")
                && !glowManager.hasGlowPermission(player, team.getName())) {
            glowManager.removeGlow(player);
            return;
        }

        if (player.isGlowing() && glowManager.isDeniedWorld(player.getWorld().getName())) {
            player.setGlowing(false);
        }
    }
}
