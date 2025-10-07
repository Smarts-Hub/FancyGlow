package dev.smartshub.fancyglow.plugin.command;

import dev.smartshub.fancyglow.api.glow.GlowMode;
import dev.smartshub.fancyglow.plugin.service.flow.LifecycleService;
import dev.smartshub.fancyglow.plugin.service.glow.GlowHandlingService;
import dev.smartshub.fancyglow.plugin.service.notify.NotifyService;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("glow")
public class GlowCommand {

    private final GlowHandlingService glowHandlingService;
    private final LifecycleService lifecycleService;
    private final NotifyService notifyService;

    public GlowCommand(GlowHandlingService glowHandlingService, LifecycleService lifecycleService,
                       NotifyService notifyService) {
        this.glowHandlingService = glowHandlingService;
        this.lifecycleService = lifecycleService;
        this.notifyService = notifyService;
    }

    @Subcommand("color")
    public void color(BukkitCommandActor actor, GlowMode glowMode) {
        glowHandlingService.color(actor, glowMode);
    }

    @Subcommand("toggle")
    public void toggle(BukkitCommandActor actor) {
        glowHandlingService.toggle(actor);
    }

    @Subcommand("off")
    public void off(BukkitCommandActor actor) {
        glowHandlingService.off(actor);
    }

    @Subcommand("set")
    @CommandPermission("fancyglow.admin")
    public void set(BukkitCommandActor actor, Player player, GlowMode glowMode) {
        glowHandlingService.set(actor, player, glowMode);
    }

    @Subcommand("off-to")
    @CommandPermission("fancyglow.admin")
    public void offOthers(BukkitCommandActor actor, Player player) {
        glowHandlingService.offOthers(player);
        notifyService.sendChat(actor.sender(), "glow-disabled-others");
    }

    @Subcommand("off-all")
    @CommandPermission("fancyglow.admin")
    public void offAll(BukkitCommandActor actor) {
        glowHandlingService.offAll();
        notifyService.sendChat(actor.sender(), "glow-disabled-all");
    }

    @Subcommand("reload")
    @CommandPermission("fancyglow.admin")
    public void reload(BukkitCommandActor actor) {
        lifecycleService.reload();
    }

}
