package dev.smartshub.fancyglow.plugin.service.glow;

import dev.smartshub.fancyglow.api.config.ConfigType;
import dev.smartshub.fancyglow.api.glow.GlowMode;
import dev.smartshub.fancyglow.api.glow.GlowState;
import dev.smartshub.fancyglow.api.nms.NMSHandler;
import dev.smartshub.fancyglow.plugin.registry.GlowModeRegistry;
import dev.smartshub.fancyglow.plugin.registry.GlowStateRegistry;
import dev.smartshub.fancyglow.plugin.service.config.ConfigService;
import dev.smartshub.fancyglow.plugin.service.notify.NotifyService;
import dev.smartshub.fancyglow.plugin.storage.database.dao.PlayerGlowDAO;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class GlowHandlingService {

    private final NMSHandler nmsHandler;
    private final GlowStateRegistry glowStateRegistry;
    private final GlowModeRegistry glowModeRegistry;
    private final PlayerGlowDAO playerGlowDAO = new PlayerGlowDAO();
    private final ConfigService configService;
    private final NotifyService notifyService;

    public GlowHandlingService(NMSHandler nmsHandler, GlowStateRegistry glowStateRegistry,
                               GlowModeRegistry glowModeRegistry,
                               ConfigService configService, NotifyService notifyService) {
        this.nmsHandler = nmsHandler;
        this.glowStateRegistry = glowStateRegistry;
        this.glowModeRegistry = glowModeRegistry;this.configService = configService;
        this.notifyService = notifyService;
    }

    public void toggleGlow(Player player, GlowMode glowMode) {
        final var existing = glowStateRegistry.getGlowStateByPlayer(player.getUniqueId());
        if (existing != null && existing.getMode().equals(glowMode)) {
            disableGlow(player);
            return;
        }
        applyGlowMode(player, glowMode.getId());
    }

    public void applyGlowEffect(Player player) {
        final var glowState = glowStateRegistry.getGlowStateByPlayer(player.getUniqueId());
        if (glowState == null) {
            removeGlowEffect(player);
            return;
        }
        if (isWorldDisabled(player)) {
            removeGlowEffect(player);
            return;
        }
        nmsHandler.setGlowing(player, glowState);
    }

    public void removeGlowEffect(Player player) {
        nmsHandler.removeGlowing(player);
    }

    public void handleJoin(Player player) {
        final boolean isPersistent = isPersistent();
        if (!isPersistent) return;

        Optional<String> stored = playerGlowDAO.getPlayerGlowMode(player.getUniqueId()).join();
        if (stored.isEmpty()) {
            return;
        }

        GlowMode mode = glowModeRegistry.getGlowModeById(stored.get());
        if (mode == null) {
            playerGlowDAO.removePlayerGlowMode(player.getUniqueId());
            return;
        }

        unregisterExisting(player);
        GlowState state = new GlowState(player.getUniqueId(), mode, currentTick());
        glowStateRegistry.register(state);
        applyGlowEffect(player);
    }

    public void handleLeave(Player player) {
        final boolean isPersistent = isPersistent();
        final GlowState existing = glowStateRegistry.getGlowStateByPlayer(player.getUniqueId());
        if (existing != null) {
            if (isPersistent) {
                playerGlowDAO.savePlayerGlowMode(player.getUniqueId(), existing.getMode().getId());
            } else {
                playerGlowDAO.removePlayerGlowMode(player.getUniqueId());
            }
        }
        removeGlowEffect(player);
        unregisterExisting(player);
    }

    public void handleWorldChange(Player player) {
        if (isWorldDisabled(player)) {
            disableGlow(player);
            notifyService.sendChat(player, "glow-disabled-world");
            return;
        }
        applyGlowEffect(player);
    }

    public boolean applyGlowMode(Player player, String glowModeId) {
        if (glowModeId == null || glowModeId.isBlank()) {
            disableGlow(player);
            return true;
        }

        GlowMode mode = glowModeRegistry.getGlowModeById(glowModeId);
        if (mode == null) {
            return false;
        }

        unregisterExisting(player);
        GlowState state = new GlowState(player.getUniqueId(), mode, currentTick());
        glowStateRegistry.register(state);
        applyGlowEffect(player);

        if (isPersistent()) {
            playerGlowDAO.savePlayerGlowMode(player.getUniqueId(), mode.getId());
        }
        notifyService.sendChat(player, "glow-enabled");
        return true;
    }

    public void disableGlow(Player player) {
        removeGlowEffect(player);
        unregisterExisting(player);
        playerGlowDAO.removePlayerGlowMode(player.getUniqueId());
        notifyService.sendChat(player, "glow-disabled");
    }

    private boolean isWorldDisabled(Player player) {
        final var disabledWorlds = configService.provide(ConfigType.SETTINGS)
                .getStringList("disabled-worlds", List.of());
        return disabledWorlds.contains(player.getWorld().getName());
    }

    private boolean isPersistent() {
        return configService.provide(ConfigType.SETTINGS).getBoolean("persistent-glow", true);
    }

    private void unregisterExisting(Player player) {
        GlowState existing = glowStateRegistry.getGlowStateByPlayer(player.getUniqueId());
        if (existing != null) {
            glowStateRegistry.unregister(existing);
        }
    }

    private long currentTick() {
        return System.currentTimeMillis() / 50L;
    }
}
