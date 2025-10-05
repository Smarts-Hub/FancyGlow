package dev.smartshub.fancyglow.plugin.registry;

import dev.smartshub.fancyglow.plugin.builder.GlowModeBuilder;
import dev.smartshub.fancyglow.api.glow.GlowMode;
import dev.smartshub.fancyglow.plugin.service.config.ConfigService;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GlowModeRegistry {

    private final ConfigService configService;
    private final GlowModeBuilder builder = new GlowModeBuilder();
    private final Set<GlowMode> glows = ConcurrentHashMap.newKeySet();

    public GlowModeRegistry(ConfigService configService) {
        this.configService = configService;
        glows.addAll(builder.build(configService));
    }

    public void register(GlowMode glowMode) {
        glows.add(glowMode);
    }

    public void unregister(GlowMode glowMode) {
        glows.remove(glowMode);
    }

    public GlowMode getGlowModeById(String id) {
        return glows.stream()
                .filter(glowMode -> glowMode.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public Set<GlowMode> getAllGlowModes() {
        return glows;
    }

    public void reload() {
        glows.clear();
        glows.addAll(builder.build(configService));
    }

}