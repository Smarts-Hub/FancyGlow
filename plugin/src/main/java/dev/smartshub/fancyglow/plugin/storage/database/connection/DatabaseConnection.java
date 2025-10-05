package dev.smartshub.fancyglow.plugin.storage.database.connection;

import dev.smartshub.fancyglow.api.config.ConfigContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static String DRIVER;
    private static boolean initialized = false;

     static  {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't load a valid JDBC driver!", e);
        }
    }

    public static void init(ConfigContainer config) {
        if (initialized) return;

        DRIVER = config.getString("driver", "h2").toLowerCase();
        String dbName = config.getString("db-name", "fancyglow");

        if (dbName == null) {
            throw new IllegalArgumentException("No 'db-name' at database.yml");
        }

        if (DRIVER.equals("mysql")) {
            String host = config.getString("host" , "localhost");
            String port = config.getString("port", "3306");
            String username = config.getString("username", "root");
            String password = config.getString("password", "");

            if (host == null || port == null || username == null || password == null) {
                throw new IllegalArgumentException("MySQL configuration is incomplete!");
            }

            URL = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?useSSL=false&autoReconnect=true&characterEncoding=utf8";
            USER = username;
            PASSWORD = password;

        } else if (DRIVER.equals("h2")) {
            URL = "jdbc:h2:file:./plugins/FancyGlow/data/" + dbName + ";AUTO_SERVER=TRUE";
            USER = "fancyglow";
            PASSWORD = "";
        } else {
            throw new IllegalArgumentException("Unsupported driver: " + DRIVER);
        }

        initialized = true;
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new IllegalStateException("DatabaseConnection not initialized!");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String getDriver() {
        return DRIVER;
    }

}


