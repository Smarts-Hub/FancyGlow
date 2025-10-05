package dev.smartshub.fancyglow.plugin.builder;

import dev.smartshub.fancyglow.api.builder.Builder;
import dev.smartshub.fancyglow.plugin.builder.mapper.GlowModeMapper;
import dev.smartshub.fancyglow.api.glow.GlowMode;
import dev.smartshub.fancyglow.plugin.service.config.ConfigService;

import java.util.Set;

public class GlowModeBuilder implements Builder<Set<GlowMode>, ConfigService> {

    private final GlowModeMapper mapper = new GlowModeMapper();

    @Override
    public Set<GlowMode> build(ConfigService configService) {
        return mapper.map(configService.provideAllGlows());
    }

}
