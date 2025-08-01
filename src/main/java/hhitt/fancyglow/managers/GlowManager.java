package hhitt.fancyglow.managers;

import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.tasks.FlashingTask;
import hhitt.fancyglow.tasks.MulticolorTask;
import hhitt.fancyglow.utils.ColorUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GlowManager {

    public static final ChatColor[] COLORS_ARRAY = new ChatColor[]{
            ChatColor.BLACK,
            ChatColor.DARK_BLUE,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_AQUA,
            ChatColor.DARK_RED,
            ChatColor.DARK_PURPLE,
            ChatColor.GOLD,
            ChatColor.GRAY,
            ChatColor.DARK_GRAY,
            ChatColor.BLUE,
            ChatColor.GREEN,
            ChatColor.AQUA,
            ChatColor.RED,
            ChatColor.LIGHT_PURPLE,
            ChatColor.YELLOW,
            ChatColor.WHITE
    };

    private final FancyGlow plugin;

    private final Set<UUID> flashingPlayerSet;
    private final Set<UUID> multicolorPlayerSet;
    private final ScoreboardManager scoreboardManager;

    private BukkitTask flashingTask;
    private BukkitTask multicolorTask;

    public GlowManager(FancyGlow plugin) {
        this.plugin = plugin;

        this.flashingPlayerSet = new HashSet<>();
        this.multicolorPlayerSet = new HashSet<>();

        // It could be null at this point?
        this.scoreboardManager = plugin.getServer().getScoreboardManager();
    }

    public boolean toggleMulticolorGlow(Player player) {
        if (isMulticolorTaskActive(player)) {
            disableRainbow(player);
            return false;
        }

        enableRainbow(player);
        return true;
    }

    public void enableRainbow(Player player) {
        player.setGlowing(true);
        multicolorPlayerSet.add(player.getUniqueId());
    }

    public void disableRainbow(Player player) {
        removeGlow(player);
        multicolorPlayerSet.remove(player.getUniqueId());
    }

    /**
     * Toggles player flashing mode status.
     *
     * @param player Player to change status.
     * @return Returns true is mode has been enabled, false if disabled.
     */
    public boolean toggleFlashingGlow(Player player) {
        if (isFlashingTaskActive(player)) {
            disableFlashing(player);
            return false;
        }

        enableFlashing(player);
        return true;
    }

    public void enableFlashing(Player player) {
        flashingPlayerSet.add(player.getUniqueId());
    }

    public void disableFlashing(Player player) {
        flashingPlayerSet.remove(player.getUniqueId());
        player.setGlowing(true);
    }

    public void setGlow(Player player, ChatColor color) {
        // Remove any existing glow
        removeGlow(player);
        // Add the player to the team and enable glowing
        getOrCreateTeam(color).addEntry(ChatColor.stripColor(player.getName()));
        player.setGlowing(true);
    }

    public void removeGlow(Player player) {
        // Remove glow from player
        player.setGlowing(false);
        // Remove from any existing color team
        removePlayerFromAllTeams(player);
    }

    public void removePlayerFromAllTeams(Player player) {
        Scoreboard board = scoreboardManager.getMainScoreboard();
        String cleanName = ChatColor.stripColor(player.getName());

        // Handle remove if rainbow selected
        if (isMulticolorTaskActive(player)) {
            // Remove from multicolor set.
            multicolorPlayerSet.remove(player.getUniqueId());
        }

        // Same as above but for flashing task 😎
        if (isFlashingTaskActive(player)) {
            // Remove from flashing set.
            flashingPlayerSet.remove(player.getUniqueId());
        }

        // Attempt to remove player from any color team
        Team team;
        for (final ChatColor color : COLORS_ARRAY) {
            team = board.getTeam(color.name());
            // fix #31: ISE (IllegalStateException) due to entry removal when team does not have it.
            if ((team != null) && team.hasEntry(cleanName)) {
                team.removeEntry(cleanName);
            }
        }
    }

    public Team getOrCreateTeam(ChatColor color) {
        Scoreboard board = scoreboardManager.getMainScoreboard();
        String colorName = color.name();
        Team team = board.getTeam(colorName);
        if (team == null) {
            team = board.registerNewTeam(colorName);
            team.setColor(color);
        }
        return team;
    }

    public boolean hasGlowPermission(Player player, ChatColor color) {
        return hasGlowPermission(player, color.name());
    }

    public boolean hasGlowPermission(Player player, String colorName) {
        return player.hasPermission("fancyglow." + colorName.toLowerCase());
    }

    public void scheduleMulticolorTask() {
        if (multicolorTask == null || multicolorTask.isCancelled()) {
            int ticks = plugin.getConfiguration().getInt("Rainbow_Update_Interval");
            this.multicolorTask = new MulticolorTask(plugin)
                    .runTaskTimerAsynchronously(plugin, 5L, ticks);
        }
    }

    public void stopMulticolorTask() {
        if (multicolorTask != null && !multicolorTask.isCancelled()) {
            multicolorTask.cancel();
        }
    }

    public void scheduleFlashingTask() {
        if (flashingTask == null || flashingTask.isCancelled()) {
            int ticks = plugin.getConfiguration().getInt("Flashing_Update_Interval");
            this.flashingTask = new FlashingTask(plugin)
                    .runTaskTimerAsynchronously(plugin, 5L, ticks);
        }
    }

    public void stopFlashingTask() {
        if (flashingTask != null && !flashingTask.isCancelled()) {
            flashingTask.cancel();
        }
    }

    public boolean isFlashingTaskActive(Player player) {
        return flashingPlayerSet.contains(player.getUniqueId());
    }

    public boolean isMulticolorTaskActive(Player player) {
        return multicolorPlayerSet.contains(player.getUniqueId());
    }

    public Set<UUID> getFlashingPlayerSet() {
        return flashingPlayerSet;
    }

    public Set<UUID> getMulticolorPlayerSet() {
        return multicolorPlayerSet;
    }

    public List<Team> getGlowTeams() {
        Set<Team> scoreboardTeams = scoreboardManager.getMainScoreboard().getTeams();
        List<Team> teamsList = new ArrayList<>(scoreboardTeams.size());
        for (Team team : scoreboardTeams) {
            if (ColorUtils.isAvailableColor(team.getName())) {
                teamsList.add(team);
            }
        }
        return teamsList;
    }

    public boolean isDeniedWorld(String worldName) {
        List<String> noAllowedWorlds = plugin.getConfiguration().getStringList("Disabled_Worlds");
        return noAllowedWorlds.contains(worldName);
    }
}
