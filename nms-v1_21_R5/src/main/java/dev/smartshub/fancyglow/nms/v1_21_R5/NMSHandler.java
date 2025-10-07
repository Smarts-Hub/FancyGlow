package dev.smartshub.fancyglow.nms.v1_21_R5;

import dev.smartshub.fancyglow.api.glow.GlowState;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class NMSHandler implements dev.smartshub.fancyglow.api.nms.NMSHandler {

    private static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID =
            new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
    private static final byte GLOWING_FLAG = 0x40;

    private static final Map<NamedTextColor, ChatFormatting> COLOR_MAP = Map.ofEntries(
            Map.entry(NamedTextColor.BLACK, ChatFormatting.BLACK),
            Map.entry(NamedTextColor.DARK_BLUE, ChatFormatting.DARK_BLUE),
            Map.entry(NamedTextColor.DARK_GREEN, ChatFormatting.DARK_GREEN),
            Map.entry(NamedTextColor.DARK_AQUA, ChatFormatting.DARK_AQUA),
            Map.entry(NamedTextColor.DARK_RED, ChatFormatting.DARK_RED),
            Map.entry(NamedTextColor.DARK_PURPLE, ChatFormatting.DARK_PURPLE),
            Map.entry(NamedTextColor.GOLD, ChatFormatting.GOLD),
            Map.entry(NamedTextColor.GRAY, ChatFormatting.GRAY),
            Map.entry(NamedTextColor.DARK_GRAY, ChatFormatting.DARK_GRAY),
            Map.entry(NamedTextColor.BLUE, ChatFormatting.BLUE),
            Map.entry(NamedTextColor.GREEN, ChatFormatting.GREEN),
            Map.entry(NamedTextColor.AQUA, ChatFormatting.AQUA),
            Map.entry(NamedTextColor.RED, ChatFormatting.RED),
            Map.entry(NamedTextColor.LIGHT_PURPLE, ChatFormatting.LIGHT_PURPLE),
            Map.entry(NamedTextColor.YELLOW, ChatFormatting.YELLOW),
            Map.entry(NamedTextColor.WHITE, ChatFormatting.WHITE)
    );

    private final Scoreboard scoreboard;

    public NMSHandler() {
        this.scoreboard = ((CraftServer) Bukkit.getServer()).getServer().getScoreboard();
    }

    @Override
    public void setGlowing(Player target, GlowState state) {
        NamedTextColor color = state.getCurrentColor();

        if (color == null) {
            removeGlowing(target);
            return;
        }

        ServerPlayer nmsTarget = toNMS(target);
        String teamName = getTeamName(target);

        updateTeam(teamName, target.getName(), convertColor(color));
        setGlowingFlag(nmsTarget, true);
        broadcastMetadata(nmsTarget);
    }

    @Override
    public void removeGlowing(Player target) {
        ServerPlayer nmsTarget = toNMS(target);
        String teamName = getTeamName(target);

        removeTeam(teamName, target.getName());
        setGlowingFlag(nmsTarget, false);
        broadcastMetadata(nmsTarget);
    }

    private void updateTeam(String teamName, String playerName, ChatFormatting color) {
        PlayerTeam oldTeam = scoreboard.getPlayerTeam(teamName);
        if (oldTeam != null) {
            scoreboard.removePlayerTeam(oldTeam);
        }

        PlayerTeam team = scoreboard.addPlayerTeam(teamName);
        team.setColor(color);
        team.setCollisionRule(Team.CollisionRule.NEVER);
        team.setNameTagVisibility(Team.Visibility.ALWAYS);
        scoreboard.addPlayerToTeam(playerName, team);
    }

    private void removeTeam(String teamName, String playerName) {
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team != null) {
            scoreboard.removePlayerFromTeam(playerName, team);
            scoreboard.removePlayerTeam(team);
        }
    }

    private void setGlowingFlag(ServerPlayer nmsPlayer, boolean glowing) {
        byte flags = nmsPlayer.getEntityData().get(DATA_SHARED_FLAGS_ID);
        flags = glowing ? (byte) (flags | GLOWING_FLAG) : (byte) (flags & ~GLOWING_FLAG);
        nmsPlayer.getEntityData().set(DATA_SHARED_FLAGS_ID, flags);
    }

    private void broadcastMetadata(ServerPlayer nmsPlayer) {
        byte flags = nmsPlayer.getEntityData().get(DATA_SHARED_FLAGS_ID);
        List<SynchedEntityData.DataValue<?>> dataValues = List.of(
                SynchedEntityData.DataValue.create(DATA_SHARED_FLAGS_ID, flags)
        );

        ClientboundSetEntityDataPacket packet =
                new ClientboundSetEntityDataPacket(nmsPlayer.getId(), dataValues);

        Bukkit.getOnlinePlayers().stream()
                .map(this::toNMS)
                .forEach(viewer -> viewer.connection.send(packet));
    }

    private String getTeamName(Player player) {
        return "glow_" + player.getUniqueId();
    }

    private ServerPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private ChatFormatting convertColor(NamedTextColor color) {
        return COLOR_MAP.getOrDefault(color, ChatFormatting.WHITE);
    }
}