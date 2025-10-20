package dev.smartshub.fancyglow.plugin.command.parameter;

import dev.smartshub.fancyglow.plugin.service.notify.NotifyService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class PlayerParameter implements ParameterType<BukkitCommandActor, Player> {

    private final NotifyService notifyService;

    public PlayerParameter(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    @Override
    public Player parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
        String name = input.readString();
        var player = Bukkit.getPlayer(name);
        if (player == null) {
            notifyService.sendChat(context.actor().sender(), "target-not-valid");
        }
        return player;
    }

    @Override
    public @NotNull SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        return context -> Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();
    }

}
