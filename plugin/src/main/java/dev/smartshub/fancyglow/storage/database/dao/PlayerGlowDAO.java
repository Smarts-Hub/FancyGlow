package dev.smartshub.fancyglow.storage.database.dao;

import dev.smartshub.fancyglow.storage.database.connection.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerGlowDAO {

    private static final String SELECT_BY_ID =
            "SELECT player_id, glow_mode_id FROM player_glow WHERE player_id = ?";

    private static final String INSERT_OR_UPDATE =
            "INSERT INTO player_glow (player_id, glow_mode_id) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE glow_mode_id = VALUES(glow_mode_id)";

    private static final String DELETE_BY_ID =
            "DELETE FROM player_glow WHERE player_id = ?";


    public CompletableFuture<Optional<String>> getPlayerGlowMode(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

                stmt.setString(1, playerId.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String glowModeId = rs.getString("glow_mode_id");
                        return Optional.of(glowModeId);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error fetching player glow mode", e);
            }
            return Optional.empty();
        });
    }

    public CompletableFuture<Void> savePlayerGlowMode(UUID playerId, String glowModeId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(INSERT_OR_UPDATE)) {

                stmt.setString(1, playerId.toString());
                stmt.setString(2, glowModeId);

                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Error saving player glow mode", e);
            }
        });
    }

    public CompletableFuture<Void> removePlayerGlowMode(UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ID)) {

                stmt.setString(1, playerId.toString());
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Error removing player glow mode", e);
            }
        });
    }

    public CompletableFuture<Boolean> hasGlowMode(UUID playerId) {
        return getPlayerGlowMode(playerId)
                .thenApply(Optional::isPresent);
    }
}