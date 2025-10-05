package dev.smartshub.fancyglow.plugin.command;

import dev.smartshub.fancyglow.api.glow.GlowMode;
import dev.smartshub.fancyglow.plugin.FancyGlow;
import dev.smartshub.fancyglow.plugin.service.config.ConfigService;
import dev.smartshub.fancyglow.plugin.service.glow.GlowHandlingService;
import dev.smartshub.fancyglow.plugin.service.notify.NotifyService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("glow")
public class GlowCommand {

    private final GlowHandlingService glowHandlingService;
    private final NotifyService notifyService;
    private final ConfigService configService;
    private final FancyGlow plugin;

    public GlowCommand(GlowHandlingService glowHandlingService, NotifyService notifyService,
                       ConfigService configService, FancyGlow plugin) {
        this.glowHandlingService = glowHandlingService;
        this.notifyService = notifyService;
        this.configService = configService;
        this.plugin = plugin;
    }

    @Subcommand("color")
    public void color(BukkitCommandActor actor, GlowMode glowMode) {
        if(!actor.isPlayer()) return;
        glowHandlingService.toggleGlow(actor.asPlayer(), glowMode);
    }

    @Subcommand("off")
    public void off(BukkitCommandActor actor) {
        if(!actor.isPlayer()) return;
        glowHandlingService.disableGlow(actor.asPlayer());
    }

    @Subcommand("set")
    @CommandPermission("fancyglow.admin")
    public void set(BukkitCommandActor actor, Player player, GlowMode glowMode) {
        if(player == null) {
            notifyService.sendChat(actor.asPlayer(), "player-not-found");
            return;
        }

        glowHandlingService.applyGlowMode(player, glowMode.getId());
        notifyService.sendChat(actor.asPlayer(), "set-glow-others");
    }

    @Subcommand("off-to")
    @CommandPermission("fancyglow.admin")
    public void offOthers(BukkitCommandActor actor, Player player) {
        if(player == null) {
            notifyService.sendChat(actor.asPlayer(), "player-not-found");
            return;
        }

        glowHandlingService.disableGlow(player);
        notifyService.sendChat(actor.asPlayer(), "off-glow-others");
    }

    @Subcommand("off-all")
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
        configService.reloadAll();
        plugin.restartAsyncTask();
        notifyService.sendChat(actor.asPlayer(), "config-reloaded");
    }

}
