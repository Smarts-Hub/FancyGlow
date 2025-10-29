package dev.smartshub.fancyglow.plugin.service.glow;

import dev.smartshub.fancyglow.plugin.storage.database.dao.PlayerGlowDAO;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class GlowPersistenceService {

    private final PlayerGlowDAO playerGlowDAO;
    private final ConcurrentHashMap<UUID, String> lastModeCache = new ConcurrentHashMap<>();

    public GlowPersistenceService(PlayerGlowDAO playerGlowDAO) {
        this.playerGlowDAO = playerGlowDAO;
    }

    public CompletableFuture<Optional<String>> getStoredMode(UUID playerId) {
        String cached = lastModeCache.get(playerId);
        if (cached != null) {
            return CompletableFuture.completedFuture(Optional.of(cached));
        }
        return playerGlowDAO.getPlayerGlowMode(playerId);
    }

    public CompletableFuture<Void> saveMode(UUID playerId, String modeId) {
        lastModeCache.put(playerId, modeId);
        return playerGlowDAO.savePlayerGlowMode(playerId, modeId)
                .exceptionally(ex -> {
                    throw new RuntimeException("Failed to save glow mode to DB", ex);
                });
    }

    public CompletableFuture<Void> removeMode(UUID playerId) {
        lastModeCache.remove(playerId);
        return playerGlowDAO.removePlayerGlowMode(playerId)
                .exceptionally(ex -> {
                    throw new RuntimeException("Failed to remove glow mode from DB", ex);
                });
    }

}
