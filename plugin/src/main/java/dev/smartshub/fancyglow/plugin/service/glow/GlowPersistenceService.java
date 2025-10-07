package dev.smartshub.fancyglow.plugin.service.glow;

import dev.smartshub.fancyglow.plugin.storage.database.dao.PlayerGlowDAO;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GlowPersistenceService {

    private final PlayerGlowDAO playerGlowDAO;

    public GlowPersistenceService(PlayerGlowDAO playerGlowDAO) {
        this.playerGlowDAO = playerGlowDAO;
    }

    public CompletableFuture<Optional<String>> getStoredMode(UUID playerId) {
        return playerGlowDAO.getPlayerGlowMode(playerId);
    }

    public void saveMode(UUID playerId, String modeId) {
        playerGlowDAO.savePlayerGlowMode(playerId, modeId);
    }

    public void removeMode(UUID playerId) {
        playerGlowDAO.removePlayerGlowMode(playerId);
    }
}
