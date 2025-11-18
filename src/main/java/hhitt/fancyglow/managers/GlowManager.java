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
    private final TabIntegration tabIntegration;

    private BukkitTask flashingTask;
    private BukkitTask multicolorTask;

    public GlowManager(FancyGlow plugin) {
        this.plugin = plugin;
        this.flashingPlayerSet = new HashSet<>();
        this.multicolorPlayerSet = new HashSet<>();
        this.scoreboardManager = plugin.getServer().getScoreboardManager();
        this.tabIntegration = new TabIntegration(plugin);
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
        
        // Get or create team
        Team team = getOrCreateTeam(color);
        
        // Add player to scoreboard team
        String cleanName = ChatColor.stripColor(player.getName());
        team.addEntry(cleanName);
        player.setGlowing(true);
        
        // Apply TAB team color if TAB is available
        tabIntegration.setPlayerTeamColor(player, color);
    }

    public void removeGlow(Player player) {
        player.setGlowing(false);
        removePlayerFromAllTeams(player);
        
        // Reset TAB team color if TAB is available
        tabIntegration.resetPlayerTeamColor(player);
    }

    public void removePlayerFromAllTeams(Player player) {
        Scoreboard board = scoreboardManager.getMainScoreboard();
        String cleanName = ChatColor.stripColor(player.getName());

        if (isMulticolorTaskActive(player)) {
            multicolorPlayerSet.remove(player.getUniqueId());
        }

        if (isFlashingTaskActive(player)) {
            flashingPlayerSet.remove(player.getUniqueId());
        }

        // Remove player from any color team
        for (final ChatColor color : COLORS_ARRAY) {
            Team team = board.getTeam(color.name());
            if (team != null && team.hasEntry(cleanName)) {
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
    
    public TabIntegration getTabIntegration() {
        return tabIntegration;
    }
}