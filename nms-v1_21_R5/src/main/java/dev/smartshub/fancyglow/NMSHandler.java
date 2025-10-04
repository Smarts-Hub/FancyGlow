package dev.smartshub.fancyglow;

import dev.smartshub.fancyglow.glow.GlowState;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class NMSHandler implements dev.smartshub.fancyglow.nms.NMSHandler {

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

    public void setGlowing(Player target, GlowState state) {
        NamedTextColor color = state.getCurrentColor();

        if (color == null) {
            removeGlowing(target);
            return;
        }

        CraftPlayer craftTarget = (CraftPlayer) target;
        ServerPlayer nmsTarget = craftTarget.getHandle();

        byte flags = nmsTarget.getEntityData().get(DATA_SHARED_FLAGS_ID);
        flags |= GLOWING_FLAG; // Set bit

        List<SynchedEntityData.DataValue<?>> dataValues = List.of(
                new SynchedEntityData.DataValue<>(0, EntityDataSerializers.BYTE, flags)
        );
        ClientboundSetEntityDataPacket metadataPacket =
                new ClientboundSetEntityDataPacket(nmsTarget.getId(), dataValues);

        String teamName = "glow_" + target.getUniqueId();
        ChatFormatting chatFormatting = convertColor(color);

        ClientboundSetPlayerTeamPacket teamPacket =
                ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(
                        new PlayerTeam(null, teamName) {{
                            setColor(chatFormatting);
                            setCollisionRule(Team.CollisionRule.NEVER);
                            setNameTagVisibility(Team.Visibility.ALWAYS);
                        }},
                        true
                );

        ClientboundSetPlayerTeamPacket addPlayerPacket =
                ClientboundSetPlayerTeamPacket.createPlayerPacket(
                        new PlayerTeam(null, teamName),
                        target.getName(),
                        ClientboundSetPlayerTeamPacket.Action.ADD
                );

        Bukkit.getOnlinePlayers().forEach(viewer -> {
            sendPacketsToViewer(viewer, teamPacket, addPlayerPacket, metadataPacket);
        });
    }

    public void removeGlowing(Player target) {
        CraftPlayer craftTarget = (CraftPlayer) target;
        ServerPlayer nmsTarget = craftTarget.getHandle();

        byte flags = nmsTarget.getEntityData().get(DATA_SHARED_FLAGS_ID);
        flags &= ~GLOWING_FLAG; // Remove bit

        List<SynchedEntityData.DataValue<?>> dataValues = List.of(
                new SynchedEntityData.DataValue<>(0, EntityDataSerializers.BYTE, flags)
        );
        ClientboundSetEntityDataPacket metadataPacket =
                new ClientboundSetEntityDataPacket(nmsTarget.getId(), dataValues);

        String teamName = "glow_" + target.getUniqueId();
        ClientboundSetPlayerTeamPacket removePacket =
                ClientboundSetPlayerTeamPacket.createPlayerPacket(
                        new PlayerTeam(null, teamName),
                        target.getName(),
                        ClientboundSetPlayerTeamPacket.Action.REMOVE
                );

        ClientboundSetPlayerTeamPacket deleteTeamPacket =
                ClientboundSetPlayerTeamPacket.createRemovePacket(
                        new PlayerTeam(null, teamName)
                );

        Bukkit.getOnlinePlayers().forEach(viewer -> {
            CraftPlayer craftViewer = (CraftPlayer) viewer;
            ServerPlayer nmsViewer = craftViewer.getHandle();

            nmsViewer.connection.send(metadataPacket);
            nmsViewer.connection.send(removePacket);
            nmsViewer.connection.send(deleteTeamPacket);
        });
    }

    private void sendPacketsToViewer(Player viewer,
                                     ClientboundSetPlayerTeamPacket teamPacket,
                                     ClientboundSetPlayerTeamPacket addPlayerPacket,
                                     ClientboundSetEntityDataPacket metadataPacket) {
        CraftPlayer craftViewer = (CraftPlayer) viewer;
        ServerPlayer nmsViewer = craftViewer.getHandle();

        nmsViewer.connection.send(teamPacket);
        nmsViewer.connection.send(addPlayerPacket);
        nmsViewer.connection.send(metadataPacket);
    }

    private ChatFormatting convertColor(NamedTextColor color) {
        return COLOR_MAP.getOrDefault(color, ChatFormatting.WHITE);
    }
}