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
            if (player == null || !player.isOnline()) {
                continue;
            }

            String cleanName = player.getName();
            Team lastTeam = playerGlowManager.findPlayerTeam(player);

            // If the player is in an old color team, remove them first
            if (lastTeam != null && !lastTeam.equals(currentTeam)) {
                lastTeam.removeEntry(cleanName);
            }

            // Join the new color team
            if (!currentTeam.hasEntry(cleanName)) {
                currentTeam.addEntry(cleanName);
            }

            // Ensure glowing is actually on
            if (!player.isGlowing()) {
                player.setGlowing(true);
            }
        }

        // Cycle index
        currentIndex = (currentIndex + 1) % GlowManager.COLORS_ARRAY.length;
    }
}