package dev.smartshub.fancyglow.service.glow;

import dev.smartshub.fancyglow.config.ConfigType;
import dev.smartshub.fancyglow.glow.GlowMode;
import dev.smartshub.fancyglow.glow.GlowState;
import dev.smartshub.fancyglow.nms.NMSHandler;
import dev.smartshub.fancyglow.registry.GlowModeRegistry;
import dev.smartshub.fancyglow.registry.GlowStateRegistry;
import dev.smartshub.fancyglow.service.config.ConfigService;
import dev.smartshub.fancyglow.service.notify.NotifyService;
import dev.smartshub.fancyglow.storage.database.dao.PlayerGlowDAO;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class GlowHandlingService {

    private final NMSHandler nmsHandler;
    private final GlowStateRegistry glowStateRegistry;
    private final GlowModeRegistry glowModeRegistry;
    private final PlayerGlowDAO playerGlowDAO;
    private final ConfigService configService;
    private final NotifyService notifyService;

    public GlowHandlingService(NMSHandler nmsHandler, GlowStateRegistry glowStateRegistry,
                               GlowModeRegistry glowModeRegistry, PlayerGlowDAO playerGlowDAO,
                               ConfigService configService, NotifyService notifyService) {
        this.nmsHandler = nmsHandler;
        this.glowStateRegistry = glowStateRegistry;
        this.glowModeRegistry = glowModeRegistry;
        this.playerGlowDAO = playerGlowDAO;
        this.configService = configService;
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
            // Keep state but do not render in disabled worlds
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
            // Stored mode no longer exists; cleanup stale DB entry
            playerGlowDAO.removePlayerGlowMode(player.getUniqueId());
            return;
        }

        // Replace any existing state and apply (or keep hidden if world disabled)
        unregisterExisting(player);
        GlowState state = new GlowState(player.getUniqueId(), mode, currentTick());
        glowStateRegistry.register(state);
        applyGlowEffect(player);
    }

    public void handleLeave(Player player) {
        // Persist or cleanup according to settings
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
            // Cancel the state and remove persistence when entering a disabled world
            disableGlow(player);
            notifyService.sendChat(player, "glow-disabled-world");
            return;
        }
        applyGlowEffect(player);
    }

    public boolean applyGlowMode(Player player, String glowModeId) {
        if (glowModeId == null || glowModeId.isBlank()) {
            // Disable and remove from persistence
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

        // Persist selection asynchronously if persistence is enabled
        if (isPersistent()) {
            playerGlowDAO.savePlayerGlowMode(player.getUniqueId(), mode.getId());
        }
        // Notify player
        notifyService.sendChat(player, "glow-enabled");
        return true;
    }

    public void disableGlow(Player player) {
        removeGlowEffect(player);
        unregisterExisting(player);
        // Remove from persistence explicitly
        playerGlowDAO.removePlayerGlowMode(player.getUniqueId());
        // Notify player
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
