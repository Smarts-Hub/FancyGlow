package dev.smartshub.fancyglow.plugin.command.parameter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class PlayerParameter implements ParameterType<BukkitCommandActor, Player> {


    @Override
    public Player parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
        String name = input.readString();
        var player = Bukkit.getPlayer(name);
        if (player == null) {
            throw new CommandErrorException("Player " + name + " not found!");
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
