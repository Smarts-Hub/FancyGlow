package dev.smartshub.fancyglow.glow;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

public class GlowState {
    private final UUID playerId;
    private final GlowMode mode;
    private final long startTick;
    private NamedTextColor currentColor;

    public GlowState(UUID playerId, GlowMode mode, long startTick) {
        this.playerId = playerId;
        this.mode = mode;
        this.startTick = startTick;
        this.currentColor = mode.getNextColor(0);
    }

    public void updateColor(long currentTick) {
        if (mode.isAnimated()) {
            long relativeTick = currentTick - startTick;
            this.currentColor = mode.getNextColor(relativeTick);
        }
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public GlowMode getMode() {
        return mode;
    }

    public NamedTextColor getCurrentColor() {
        return currentColor;
    }
}