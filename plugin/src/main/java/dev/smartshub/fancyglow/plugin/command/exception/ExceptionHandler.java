package dev.smartshub.fancyglow.plugin.command.exception;

import dev.smartshub.fancyglow.plugin.service.notify.NotifyService;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.exception.NoPermissionException;

public class ExceptionHandler extends BukkitExceptionHandler {

    private final NotifyService notifyService;

    public ExceptionHandler(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    @HandleException
    public void onNoPermission(final @NotNull NoPermissionException e, final @NotNull BukkitCommandActor actor) {
        notifyService.sendChat(actor.sender(), "no-permission");
    }

}
