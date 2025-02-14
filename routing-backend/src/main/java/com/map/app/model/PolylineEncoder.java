package com.map.app.model;

public class PolylineEncoder {

	// Helper function to encode a single coordinate
	private static String encodeCoordinate(double coordinates, int factor) {
		int coordinate = (int) Math.round(coordinates * factor);
		coordinate <<= 1;
		if (coordinate < 0) {
			coordinate = ~coordinate;
		}

		StringBuilder output = new StringBuilder();
		while (coordinate >= 0x20) {
			output.append((char) ((0x20 | (coordinate & 0x1f)) + 63));
			coordinate >>= 5;
		}
		output.append((char) (coordinate + 63));

		return output.toString();
	}

	// Main polyline encoding function
	public static String encode(double[][] coordinates, int precision) {
		if (coordinates.length == 0) return "";

		int factor = (int) Math.pow(10, precision);
		StringBuilder output = new StringBuilder();

		output.append(encodeCoordinate(coordinates[0][0], factor));
		output.append(encodeCoordinate(coordinates[0][1], factor));

		for (int i = 1; i < coordinates.length; i++) {
			double[] a = coordinates[i];
			double[] b = coordinates[i - 1];

			output.append(encodeCoordinate((a[0] - b[0]), factor));
			output.append(encodeCoordinate((a[1] - b[1]), factor));
		}

		return output.toString();
	}
}