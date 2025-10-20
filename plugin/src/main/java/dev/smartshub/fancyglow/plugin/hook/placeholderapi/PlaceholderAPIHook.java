package dev.smartshub.fancyglow.plugin.hook.placeholderapi;

import dev.smartshub.fancyglow.api.glow.GlowState;
import dev.smartshub.fancyglow.plugin.registry.GlowStateRegistry;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final GlowStateRegistry glowStateRegistry;

    public PlaceholderAPIHook(GlowStateRegistry glowStateRegistry) {
        this.glowStateRegistry = glowStateRegistry;
    }


    @NotNull
    @Override
    public String getIdentifier() {
        return "fancyglow";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "SmartsHub";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        GlowState state = glowStateRegistry.getGlowStateByPlayer(player.getUniqueId());

        // Might a little forced
        return switch (identifier.toLowerCase()) {
            case "is_glowing" -> state != null ? "true" : "false";
            case "glow_status" -> state != null ? (state.getCurrentColor() != null ? "active" : "inactive") : "none";
            case "glow_color" -> state != null && state.getCurrentColor() != null ? state.getCurrentColor().toString().toUpperCase() : "";
            case "glow_mode" -> state != null && state.getMode() != null ? state.getMode().getId() : "";
            case "glow_display_name" -> state != null && state.getMode() != null && state.getMode().getDisplayName() != null ? state.getMode().getDisplayName() : "";
            case "glow_permission" -> state != null && state.getMode() != null && state.getMode().getPermission() != null ? state.getMode().getPermission() : "";
            case "glow_ticks_per_color" -> state != null && state.getMode() != null ? String.valueOf(state.getMode().getTicksPerColor()) : "";
            case "glow_is_animated" -> state != null && state.getMode() != null ? (state.getMode().isAnimated() ? "true" : "false") : "";
            case "glow_colors" -> {
                if (state == null || state.getMode() == null || state.getMode().getColors() == null) yield "";
                yield state.getMode().getColors().stream()
                        .filter(java.util.Objects::nonNull)
                        .map(color -> color.toString().toUpperCase())
                        .reduce((a, b) -> a + "," + b)
                        .orElse("");
            }
            default -> "";
        };
    }
}
