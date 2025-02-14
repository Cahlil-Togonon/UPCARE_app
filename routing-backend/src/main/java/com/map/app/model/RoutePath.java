package com.map.app.model;

import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.map.app.model.PolylineEncoder;

public class RoutePath {
	private float distance;    // Total distance of the route (in meters)
	private float weight;      // Concentration score or other weight metrics
	private float time;        // Total time of the route (in seconds)
	private String points;  // Polyline encoded points
	private ArrayList<Double> bbox;
	private List<Map<String, Object>> instructions; // Detailed instructions

	// Getters and Setters
	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		this.time = time;
	}

	public String getPoints() {
		return points;
	}

	public void setPoints(String points) {
		this.points = points;
	}

	public ArrayList<Double> getBounds() {return bbox;}
	public void setBounds(ArrayList<Double> bbox) {this.bbox = bbox;}

	public List<Map<String, Object>> getInstructions() {
		return instructions;
	}

	public void setInstructions(List<Map<String, Object>> instructions) {
		this.instructions = instructions;
	}

	public RoutePath() {
		points = "";
		bbox = new ArrayList<>();
		distance = 0;
		weight = 0;
		time = 0;
	}

	// Method to calculate the bounding box from the PointList
	public BBox calcBBox2D(PointList pointList) {
		BBox bounds = BBox.createInverse(false);
		for (int i = 0; i < pointList.size(); i++) {
			bounds.update(pointList.getLat(i), pointList.getLon(i));
		}
		return bounds;
	}

	public void fillPath(PointList rp) {
//		for (int i = 0; i < rp.size(); i++) {
//			points.add(new GHPoint(rp.getLat(i), rp.getLon(i)).toGeoJson());
//		}

		double[][] coordinates = new double[rp.size()][2];

		for (int i = 0; i < rp.size(); i++) {
			coordinates[i][0] = rp.getLat(i);
			coordinates[i][1] = rp.getLon(i);
		}

		points = PolylineEncoder.encode(coordinates, 5);

		BBox routeBB = calcBBox2D(rp);
		bbox.add(routeBB.minLat);
		bbox.add(routeBB.minLon);
		bbox.add(routeBB.maxLat);
		bbox.add(routeBB.maxLon);
	}

	// Helper method to convert this object to JSON format
	public Map<String, Map<String, Object>> toJson() {
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("distance", this.distance);
		jsonMap.put("weight", this.weight);
		jsonMap.put("time", this.time);

		jsonMap.put("points", this.points);

		jsonMap.put("bbox", this.bbox);

		jsonMap.put("instructions", this.instructions);

		Map<String, Map<String, Object>> result = new HashMap<>();
		result.put("paths", jsonMap);

		return result;
	}
}

