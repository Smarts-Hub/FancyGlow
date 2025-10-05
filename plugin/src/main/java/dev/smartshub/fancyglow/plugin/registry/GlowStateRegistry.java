package dev.smartshub.fancyglow.plugin.registry;

import dev.smartshub.fancyglow.api.glow.GlowState;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GlowStateRegistry {

    private final Set<GlowState> glows = ConcurrentHashMap.newKeySet();

    public void register(GlowState glowState) {
        glows.add(glowState);
    }

    public void unregister(GlowState glowState) {
        glows.remove(glowState);
    }

    public GlowState getGlowStateByPlayer(UUID uuid) {
        return glows.stream()
                .filter(glowState -> glowState.getPlayerId().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public Set<GlowState> getAllGlows() {
        return glows;
    }

}
