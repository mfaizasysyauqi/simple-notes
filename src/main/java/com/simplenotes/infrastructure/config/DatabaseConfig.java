// DatabaseConfig.java
package com.simplenotes.infrastructure.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:sqlite:simplenotes.db";

    public static void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                executeSchema(conn);
                System.out.println("Database initialized successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Schema file error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executeSchema(Connection conn) throws SQLException, IOException {
        // Load schema.sql from resources
        try (InputStream inputStream = DatabaseConfig.class.getClassLoader().getResourceAsStream("database/schema.sql")) {
            if (inputStream == null) {
                throw new IOException("Schema file not found.");
            }
            String schema = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(schema);
            }
        }
    }
}
