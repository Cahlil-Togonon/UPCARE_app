package com.map.app.containers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.map.app.model.AirQuality;

public class AirQualityLoader {
    public static List<AirQuality> loadFromCSV(String filePath) {
        List<AirQuality> airQualityList = new ArrayList<>();
        Set<String> seen = new HashSet<>();  // To track unique sensor-location pairs

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                String name = parts[0];
                double lon = Double.parseDouble(parts[1]); // X (Longitude)
                double lat = Double.parseDouble(parts[2]); // Y (Latitude)
                double aqi = Double.parseDouble(parts[3]); // US AQI

                String key = name; // Unique key based on sensor name and location
                if (!seen.contains(key)) {
                    seen.add(key);
                    airQualityList.add(new AirQuality(lat, lon, aqi, name));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println(airQualityList);
        return airQualityList;
    }
}
