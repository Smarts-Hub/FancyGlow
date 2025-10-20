package dev.smartshub.fancyglow.plugin.command.parameter;

import dev.smartshub.fancyglow.api.glow.GlowMode;
import dev.smartshub.fancyglow.plugin.registry.GlowModeRegistry;
import dev.smartshub.fancyglow.plugin.service.notify.NotifyService;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class GlowModeParameter implements ParameterType<BukkitCommandActor, GlowMode> {

    private final GlowModeRegistry glowModeRegistry;
    private final NotifyService notifyService;

    public GlowModeParameter(GlowModeRegistry glowModeRegistry, NotifyService notifyService) {
        this.glowModeRegistry = glowModeRegistry;
        this.notifyService = notifyService;
    }

    @Override
    public GlowMode parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
        String name = input.readString();
        GlowMode glow = glowModeRegistry.getGlowModeById(name);
        if (glow == null) {
            notifyService.sendChat(context.actor().sender(), "color-not-valid");
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
