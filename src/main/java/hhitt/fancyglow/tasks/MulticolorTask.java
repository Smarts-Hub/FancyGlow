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

        // Cycle through colors
        ChatColor currentColor = GlowManager.COLORS_ARRAY[currentIndex];
        Team currentTeam = glowManager.getOrCreateTeam(currentColor);

        for (UUID uuid : glowManager.getMulticolorPlayerSet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                continue;
            }

            String cleanName = player.getName();
            Team lastTeam = playerGlowManager.findPlayerTeam(player);

            // Update the Minecraft Team (This handles the actual glowing outline)
            if (lastTeam != null && !lastTeam.equals(currentTeam)) {
                lastTeam.removeEntry(cleanName);
            }

            if (!currentTeam.hasEntry(cleanName)) {
                currentTeam.addEntry(cleanName);
            }

            if (!player.isGlowing()) {
                player.setGlowing(true);
            }
            
            // NOTE: We do NOT need to update TAB here. 
            // The placeholder %fancyglow_tab_color% handles the nametag color automatically.
        }

        // Move to next color index
        currentIndex = (currentIndex + 1) % GlowManager.COLORS_ARRAY.length;
    }
}