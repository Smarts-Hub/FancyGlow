package dev.smartshub.fancyglow.builder.mapper;

import dev.smartshub.fancyglow.builder.serializer.GlowModeSerializer;
import dev.smartshub.fancyglow.config.ConfigContainer;
import dev.smartshub.fancyglow.glow.GlowMode;

import java.util.Set;
import java.util.stream.Collectors;

public class GlowModeMapper implements Mapper<Set<GlowMode>, Set<ConfigContainer>> {

    private final GlowModeSerializer serializer = new GlowModeSerializer();

    @Override
    public Set<GlowMode> map(Set<ConfigContainer> configs) {
        return configs.stream()
                .map(serializer::serialize)
                .collect(Collectors.toSet());
    }

}
