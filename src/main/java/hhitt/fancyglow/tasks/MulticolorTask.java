package hhitt.fancyglow.tasks;

import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.GlowManager;
import hhitt.fancyglow.managers.PlayerGlowManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public class MulticolorTask extends BukkitRunnable {

    private int currentIndex = 0;
    private final GlowManager glowManager;
    private final PlayerGlowManager playerGlowManager;

    public MulticolorTask(FancyGlow plugin) {
        this.glowManager = plugin.getGlowManager();
        this.playerGlowManager = plugin.getPlayerGlowManager();
    }

    @Override
    public void run() {
        if (glowManager.getMulticolorPlayerSet().isEmpty()) {
            return;
        }

        ChatColor currentColor = GlowManager.COLORS_ARRAY[currentIndex];
        Team currentTeam = glowManager.getOrCreateTeam(currentColor);

        for (UUID uuid : glowManager.getMulticolorPlayerSet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || player.isDead()) {
                continue;
            }

            String cleanName = ChatColor.stripColor(player.getName());
            Team lastTeam = playerGlowManager.findPlayerTeam(player);

            // Remove from old team first
            if (lastTeam != null && !lastTeam.equals(currentTeam)) {
                lastTeam.removeEntry(cleanName);
            }

            // Add to new team
            if (!currentTeam.hasEntry(cleanName)) {
                currentTeam.addEntry(cleanName);
            }

            // Ensure player is glowing
            if (!player.isGlowing()) {
                player.setGlowing(true);
            }

            // Update scoreboard if necessary
            if (currentTeam.getScoreboard() != null && 
                player.getScoreboard() != currentTeam.getScoreboard()) {
                player.setScoreboard(currentTeam.getScoreboard());
            }
            
            // Update TAB team color
            glowManager.getTabIntegration().setPlayerTeamColor(player, currentColor);
        }

        // Increment for next color
        currentIndex = (currentIndex + 1) % GlowManager.COLORS_ARRAY.length;
    }
}