package com.map.app.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/manila_osm?user=postgres&password=admin";
    private static final String DB_URL = getDatabaseUrl();

    private static String getDatabaseUrl() {
        String envUrl = System.getenv("DATABASE_URL");
        if (envUrl == null || envUrl.trim().isEmpty()) {
            System.out.println("DATABASE_URL not set. Using default.");
            return DEFAULT_DB_URL;
        }
        return envUrl;
    }
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
