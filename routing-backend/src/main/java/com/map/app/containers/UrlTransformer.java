package com.map.app.containers;

import com.map.app.model.UrlContainer;
import com.graphhopper.util.shapes.GHPoint;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Siftee
 */
public class UrlTransformer {
	private ArrayList<GHPoint> waypoints;  // List of GHPoint (for waypoints)
	private String routeType;
	private String vehicle;

	// Constructor
	public UrlTransformer() {
		this.waypoints = new ArrayList<>();
	}

	// Getters and Setters
	public List<GHPoint> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(ArrayList<GHPoint> waypoints) {
		this.waypoints = waypoints;
	}

	public String getRouteType() {
		return routeType;
	}

	public void setRouteType(String routeType) {
		this.routeType = routeType;
	}

	public String getVehicle() {
		return vehicle;
	}

	public void setVehicle(String vehicle) {
		this.vehicle = vehicle;
	}

	/**
	 * Convert method to transform the DTO into the model (UrlContainer).
	 * This method now handles multiple waypoints (each represented as a GHPoint).
	 */
	public UrlContainer convert() {
		// Converting the list of GHPoint waypoints into a UrlContainer model
		UrlContainer rp = new UrlContainer();

		// Set waypoints in the UrlContainer
		rp.setWaypoints(this.waypoints);  // directly setting the list of GHPoints
		rp.setRouteType(this.getRouteType());
		rp.setVehicle(this.getVehicle());

		return rp;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (GHPoint waypoint : waypoints) {
			sb.append("point=" + waypoint.getLat() + "," + waypoint.getLon() + "&");
		}
		sb.append("Vehicle=" + vehicle).append("&").append("RouteType=" + routeType);
		return sb.toString();
	}
}