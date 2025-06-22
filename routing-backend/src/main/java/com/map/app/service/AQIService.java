package com.map.app.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AQIService {

    // Cache to hold all AQI values keyed by "lat,lon"
    private static final Map<String, Double> AQI_CACHE = new HashMap<>();

    // Load all AQI values from the database into the cache
    public static void loadAllAQIData() {
        String query = "SELECT lat, lon, predicted_aqi FROM nodes";

        try (Connection conn = DatabaseConnection.connect()) {

            //Test the connection explicitly
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connected to PostgreSQL from AQIService.");
            } else {
                System.err.println("Connection is null or closed.");
                return;
            }

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    double lat = rs.getDouble("lat");
                    double lon = rs.getDouble("lon");
                    double aqi = rs.getDouble("predicted_aqi");

                    String key = getKey(lat, lon);
                    AQI_CACHE.put(key, aqi);
                }

                System.out.println("Loaded " + AQI_CACHE.size() + " AQI entries into cache.");

            }

        } catch (SQLException e) {
            System.err.println("Failed to connect or query database:");
            e.printStackTrace();
        }
    }

    // Helper method to format the lat/lon as the key
    private static String getKey(double lat, double lon) {
        return String.format("%.7f,%.7f", lat, lon);
    }

    // Retrieve AQI from the cache
    public static Double getAQIFromCache(double lat, double lon) {
        return AQI_CACHE.get(getKey(lat, lon));
    }

    // Optional: Clear cache if needed
    public static void clearCache() {
        AQI_CACHE.clear();
    }
}




