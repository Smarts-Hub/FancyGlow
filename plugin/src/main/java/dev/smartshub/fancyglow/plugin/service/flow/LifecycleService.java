package dev.smartshub.fancyglow.plugin.service.flow;

import dev.smartshub.fancyglow.api.flow.Reloadable;
import dev.smartshub.fancyglow.plugin.FancyGlow;
import dev.smartshub.fancyglow.plugin.service.config.ConfigService;
import dev.smartshub.fancyglow.plugin.registry.GlowModeRegistry;
import dev.smartshub.fancyglow.plugin.task.AsyncJobTask;

import java.util.ArrayList;
import java.util.List;

public class LifecycleService {

    private final ConfigService configService;
    private final GlowModeRegistry glowModeRegistry;
    private final List<Reloadable> reloadables = new ArrayList<>();
    private AsyncJobTask asyncJobTask;

    public LifecycleService(ConfigService configService, GlowModeRegistry glowModeRegistry) {
        this.configService = configService;
        this.glowModeRegistry = glowModeRegistry;
    }

    public void registerReloadable(Reloadable reloadable) {
        reloadables.add(reloadable);
    }

    public void setAsyncJobTask(AsyncJobTask asyncJobTask) {
        this.asyncJobTask = asyncJobTask;
    }

    public void reload() {
        if (asyncJobTask != null) {
            asyncJobTask.stop();
        }

        configService.reloadAll();
        glowModeRegistry.reload();
        reloadables.forEach(Reloadable::reload);

        if (asyncJobTask != null) {
            asyncJobTask.reload();
            asyncJobTask.start();
        }
    }

    public void shutdown() {
        if (asyncJobTask != null) {
            asyncJobTask.stop();
        }
        reloadables.forEach(Reloadable::shutdown);
    }
}