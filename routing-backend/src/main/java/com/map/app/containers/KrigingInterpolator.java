package com.map.app.containers;

import com.map.app.model.AirQuality;
import smile.interpolation.variogram.SphericalVariogram;
import smile.interpolation.KrigingInterpolation;

import java.util.Arrays;
import java.util.List;

public class KrigingInterpolator {
    private KrigingInterpolation kriging = null;
    private SphericalVariogram spherical = null;

    public KrigingInterpolator(String csvFilePath) {
        List<AirQuality> points = AirQualityLoader.loadFromCSV(csvFilePath);

        double[][] coords = new double[points.size()][2];
        double[] values = new double[points.size()];

        for (int i = 0; i < points.size(); i++) {
            coords[i][0] = points.get(i).getLat();
            coords[i][1] = points.get(i).getLon();
            values[i] = points.get(i).getAqi(); // AQI value
        }

        // Use Exponential Variogram for Kriging interpolation
        try{
            this.spherical = new SphericalVariogram(0.1,100,0.1);
            this.kriging = new KrigingInterpolation(coords, values, this.spherical, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double interpolate(double lat, double lon) {
        return kriging.interpolate(new double[]{lat, lon});
    }
}
