package dev.smartshub.fancyglow.builder;

import dev.smartshub.fancyglow.builder.mapper.GlowModeMapper;
import dev.smartshub.fancyglow.glow.GlowMode;
import dev.smartshub.fancyglow.service.config.ConfigService;

import java.util.Set;

public class GlowModeBuilder implements Builder<Set<GlowMode>, ConfigService> {

    private final GlowModeMapper mapper = new GlowModeMapper();

    @Override
    public Set<GlowMode> build(ConfigService configService) {
        return mapper.map(configService.provideAllGlows());
    }

}
