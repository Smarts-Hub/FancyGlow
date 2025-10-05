package dev.smartshub.fancyglow.plugin.builder.serializer;

import dev.smartshub.fancyglow.api.builder.serializer.Serializer;
import dev.smartshub.fancyglow.api.config.ConfigContainer;
import dev.smartshub.fancyglow.api.glow.GlowMode;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GlowModeSerializer implements Serializer<GlowMode, ConfigContainer> {

    private static final Map<String, NamedTextColor> COLOR_MAP = Map.ofEntries(
            Map.entry("BLACK", NamedTextColor.BLACK),
            Map.entry("DARK_BLUE", NamedTextColor.DARK_BLUE),
            Map.entry("DARK_GREEN", NamedTextColor.DARK_GREEN),
            Map.entry("DARK_AQUA", NamedTextColor.DARK_AQUA),
            Map.entry("DARK_RED", NamedTextColor.DARK_RED),
            Map.entry("DARK_PURPLE", NamedTextColor.DARK_PURPLE),
            Map.entry("GOLD", NamedTextColor.GOLD),
            Map.entry("GRAY", NamedTextColor.GRAY),
            Map.entry("DARK_GRAY", NamedTextColor.DARK_GRAY),
            Map.entry("BLUE", NamedTextColor.BLUE),
            Map.entry("GREEN", NamedTextColor.GREEN),
            Map.entry("AQUA", NamedTextColor.AQUA),
            Map.entry("RED", NamedTextColor.RED),
            Map.entry("LIGHT_PURPLE", NamedTextColor.LIGHT_PURPLE),
            Map.entry("YELLOW", NamedTextColor.YELLOW),
            Map.entry("WHITE", NamedTextColor.WHITE)
    );

    @Override
    public GlowMode serialize(ConfigContainer config) {
        final var id = config.getNameWithoutExtension();
        final var displayName = config.getString("display-name", "Unnamed Glow");
        final var permission = config.getString("permission", "fancyglow." + id);
        final var ticksPerColor = config.getInt("tick-update-interval", 20) % 2 == 0
                ? config.getInt("tick-update-interval", 20)
                : config.getInt("tick-update-interval", 20) + 1;
        final var colorStrings = config.getStringList("color", List.of());

        final List<NamedTextColor> colors = new ArrayList<>();
        for (String colorString : colorStrings) {
            colors.add(parseColor(colorString));
        }

        boolean hasValidColor = colors.stream().anyMatch(Objects::nonNull);
        if (!hasValidColor) {
            throw new IllegalArgumentException(
                    "GlowMode '" + id + "' must contain at least one valid color!"
            );
        }

        return new GlowMode(id, displayName, permission, ticksPerColor, colors);
    }

    private NamedTextColor parseColor(String colorName) {
        if (colorName == null || colorName.isEmpty()) {
            return null;
        }

        String upperName = colorName.toUpperCase().trim();

        if (upperName.equals("OFF") || upperName.equals("NULL") || upperName.equals("NONE")) {
            return null;
        }

        return COLOR_MAP.get(upperName);
    }
}