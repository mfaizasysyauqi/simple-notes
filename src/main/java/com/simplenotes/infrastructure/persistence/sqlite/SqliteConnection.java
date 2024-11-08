// src/main/java/com/simplenotes/infrastructure/persistence/sqlite/SqliteConnection.java
package com.simplenotes.infrastructure.persistence.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteConnection {
    private static final String URL = "jdbc:sqlite:simplenotes.db";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
