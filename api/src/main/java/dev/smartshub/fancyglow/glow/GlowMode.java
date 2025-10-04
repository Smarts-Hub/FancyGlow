package dev.smartshub.fancyglow.glow;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Objects;

public class GlowMode {
    private final String id;
    private final String displayName;
    private final String permission;
    private final List<NamedTextColor> colors;
    private final boolean animated;
    private final long ticksPerColor;

    public GlowMode(String id, String displayName, String permission, long ticksPerColor, List<NamedTextColor> colors) {
        if (colors == null || colors.isEmpty()) {
            throw new IllegalArgumentException("Color list must contain at least one element!");
        }

        boolean hasValidColor = colors.stream().anyMatch(Objects::nonNull);
        if (!hasValidColor) {
            throw new IllegalArgumentException("Color list must contain at least one non-null color!");
        }

        if (ticksPerColor <= 0) {
            throw new IllegalArgumentException(id + ": tick-update-interval must be greater than 0!");
        }

        this.id = id;
        this.displayName = displayName;
        this.permission = permission;
        this.colors = List.copyOf(colors);
        this.animated = colors.size() > 1;
        this.ticksPerColor = ticksPerColor;
    }

    public GlowMode(String id, String displayName, String permission, NamedTextColor color) {
        this(id, displayName, permission, 20, List.of(color));
    }

    public boolean isAnimated() {
        return animated;
    }

    public NamedTextColor getNextColor(long tick) {
        if (colors.isEmpty()) {
            return null;
        }

        long frame = tick / ticksPerColor;
        int index = (int) (frame % colors.size());
        return colors.get(index);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPermission() {
        return permission;
    }

    public List<NamedTextColor> getColors() {
        return colors;
    }

    public long getTicksPerColor() {
        return ticksPerColor;
    }
}