package com.map.app.model;

import com.graphhopper.util.shapes.GHPoint;
import java.util.ArrayList;

/**
 * @author Siftee
 */
public class UrlContainer {
	private ArrayList<GHPoint> waypoints;
//	private double Startlat;
//	private double Startlon;
//	private double Endlat;
//	private double Endlon;
	private String Vehicle;
	private String RouteType;
	
	
	
//	public double getStartlat() {
//		return Startlat;
//	}
//	public void setStartlat(float startlat) {
//		Startlat = startlat;
//	}
//	public double getStartlon() {
//		return Startlon;
//	}
//	public void setStartlon(float startlon) {
//		Startlon = startlon;
//	}
	public String getVehicle() {
		return Vehicle;
	}
	public void setVehicle(String vehicle) { Vehicle = vehicle;}
//	public double getEndlat() {
//		return Endlat;
//	}
//	public void setEndlat(float endlat) {
//		Endlat = endlat;
//	}
//	public double getEndlon() {
//		return Endlon;
//	}
//	public void setEndlon(float endlon) {
//		Endlon = endlon;
//	}
	public ArrayList<GHPoint> getWaypoints() {return waypoints;}
	public void setWaypoints(ArrayList<GHPoint> InputWaypoints) {waypoints = InputWaypoints;}

	public String getRouteType() {
		return RouteType;
	}
	public void setRouteType(String routeType) {
		RouteType = routeType;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (GHPoint waypoint : waypoints) {
			sb.append("point=" + waypoint.getLat() + "," + waypoint.getLon() + "&");
		}
		sb.append("Vehicle=" + Vehicle).append("&").append("RouteType=" + RouteType);
		return sb.toString();
	}

}