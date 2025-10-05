package dev.smartshub.fancyglow.plugin.storage.database.table;

import dev.smartshub.fancyglow.plugin.storage.database.connection.DatabaseConnection;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaCreator {

    public static void createSchema() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_glow (
                    player_id VARCHAR(36) PRIMARY KEY,
                    glow_mode_id VARCHAR(64) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """);

            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_glow_mode ON player_glow(glow_mode_id)");

            Bukkit.getLogger().info("[FancyGlow] Database schema created successfully!");

        } catch (SQLException e) {
            Bukkit.getLogger().severe("[FancyGlow] Error creating database schema:");
            e.printStackTrace();
        }
    }
}