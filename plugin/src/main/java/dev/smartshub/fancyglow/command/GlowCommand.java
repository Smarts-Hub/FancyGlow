package dev.smartshub.fancyglow.command;

import dev.smartshub.fancyglow.glow.GlowMode;
import dev.smartshub.fancyglow.service.glow.GlowHandlingService;
import dev.smartshub.fancyglow.service.notify.NotifyService;
import org.bukkit.Bukkit;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("glow")
public class GlowCommand {

    private final GlowHandlingService glowHandlingService;
    private final NotifyService notifyService;

    public GlowCommand(GlowHandlingService glowHandlingService, NotifyService notifyService) {
        this.glowHandlingService = glowHandlingService;
        this.notifyService = notifyService;
    }

    @Subcommand("")
    public void color(BukkitCommandActor actor, GlowMode glowMode) {
        if(!actor.isPlayer()) return;
        glowHandlingService.toggleGlow(actor.asPlayer(), glowMode);
    }

    @Subcommand("off")
    public void off(BukkitCommandActor actor) {
        if(!actor.isPlayer()) return;
        glowHandlingService.disableGlow(actor.asPlayer());
    }

    @Subcommand("set <player>")
    @CommandPermission("fancyglow.admin")
    public void set(BukkitCommandActor actor, String playerName, GlowMode glowMode) {
        var player = Bukkit.getPlayer(playerName);
        if(player == null) {
            notifyService.sendChat(actor.asPlayer(), "player-not-found");
            return;
        }

        glowHandlingService.applyGlowMode(player, glowMode.getId());
        notifyService.sendChat(actor.asPlayer(), "set-glow-others");
    }

    @Subcommand("off <player>")
    @CommandPermission("fancyglow.admin")
    public void off(BukkitCommandActor actor, String playerName) {
        var player = Bukkit.getPlayer(playerName);
        if(player == null) {
            notifyService.sendChat(actor.asPlayer(), "player-not-found");
            return;
        }

        glowHandlingService.disableGlow(player);
        notifyService.sendChat(actor.asPlayer(), "off-glow-others");
    }

    @Subcommand("off all")
    @CommandPermission("fancyglow.admin")
    public void offAll(BukkitCommandActor actor) {
        for(var player : Bukkit.getOnlinePlayers()) {
            glowHandlingService.disableGlow(player);
        }
        notifyService.sendChat(actor.asPlayer(), "off-glow-all");
    }

    @Subcommand("reload")
    @CommandPermission("fancyglow.admin")
    public void reload(BukkitCommandActor actor) {

    }

}
