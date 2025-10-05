package dev.smartshub.fancyglow.plugin.command.parameter;

import dev.smartshub.fancyglow.api.glow.GlowMode;
import dev.smartshub.fancyglow.plugin.registry.GlowModeRegistry;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class GlowModeParameter implements ParameterType<BukkitCommandActor, GlowMode> {

    private final GlowModeRegistry glowModeRegistry;

    public GlowModeParameter(GlowModeRegistry glowModeRegistry) {
        this.glowModeRegistry = glowModeRegistry;
    }

    @Override
    public GlowMode parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
        String name = input.readString();
        GlowMode glow = glowModeRegistry.getGlowModeById(name);
        if (glow == null) {
            throw new CommandErrorException("No such glow: " + name);
        }
        return glow;
    }

    @Override
    public @NotNull SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        return context -> glowModeRegistry.getAllGlowModes().stream()
                .map(GlowMode::getId)
                .toList();
    }

}
