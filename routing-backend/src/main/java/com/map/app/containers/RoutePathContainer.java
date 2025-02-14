package com.map.app.containers;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.map.app.graphhopperfuncs.concentrationCalc;
import com.map.app.model.RoutePath;
import com.map.app.model.UrlContainer;
import com.map.app.service.PathChoice;
import com.map.app.service.TrafficAndRoutingService;
import com.map.app.service.TransportMode;

import java.util.*;
import java.util.concurrent.locks.Lock;

public class RoutePathContainer {
	private final GraphHopper gh;
	private final Lock readLock;

	public RoutePathContainer(GraphHopper hopper, Lock readLock) {
		this.gh = hopper;
		this.readLock = readLock;
	}

	public RoutePath finalPath(UrlContainer p, String profile, TransportMode mode) {
		RoutePath indiv = new RoutePath();
		GHRequest request = new GHRequest(p.getWaypoints()).setProfile(profile).putHint(Parameters.CH.DISABLE, true);
		request.setPathDetails(Arrays.asList(Parameters.Details.EDGE_ID));
		PointList pl = new PointList();
		HashMap<String, Float> map = new HashMap<>();
		List<Map<String, Object>> instructionsList = new ArrayList<>();

		try {
			// Getting result
			GHResponse fullRes = gh.route(request);
			if (fullRes.hasErrors()) {
				throw new RuntimeException(fullRes.getErrors().toString());
			}

			ResponsePath res = fullRes.getBest();
			double concScore = concentrationCalc.calcConcentrationScore(gh, res.getPathDetails().get(Parameters.Details.EDGE_ID), mode);
			map.put("distance", (float) res.getDistance()); // meters
			map.put("time", (float) (res.getTime() / 1000.0)); // seconds
			map.put("weight", (float) concScore); // concentration score

			InstructionList instructions = res.getInstructions();
			Integer idx = 0;
			for (Instruction ele : instructions) {
				// Create the instruction map in the desired structure
				Map<String, Object> instruction = new HashMap<>();
				instruction.put("distance", ele.getDistance()); // distance in meters
//				instruction.put("heading", ele.getHeading());   // heading in degrees
				instruction.put("sign", ele.getSign());         // sign (0, 1, etc.)
				Integer idx1 = idx;
				Integer idx2 = idx;
				while (idx < instructions.size()) {
					if (ele != instructions.get(idx)){
						idx2 = idx;
						break;
					}
					idx += 1;
				}
				instruction.put("interval", Arrays.asList(idx1,idx2)); // interval [start, end]
				instruction.put("text", ele.getTurnDescription(instructions.getTr())); // text description
				instruction.put("time", ele.getTime());        // time in milliseconds
				instruction.put("street_name", ele.getName()); // street name

				instructionsList.add(instruction);
			}

			// Add final details to instructions
			pl = res.getPoints();
			indiv.fillPath(pl);
			indiv.setInstructions(instructionsList);

			indiv.setDistance(map.get("distance"));
			indiv.setTime(map.get("time"));
			indiv.setWeight(map.get("weight"));

		} finally {

		}
		return indiv;
	}

	public ArrayList<RoutePath> find(UrlContainer p) {
		//routing result for given route information
		this.readLock.lock();
		ArrayList<RoutePath> result = new ArrayList<>();

		try
		{
			//fetching the profile to do routing with
			String profile="";
			TransportMode mode=TransportMode.valueOf("car");
			PathChoice pathChoice;
			switch (p.getVehicle()) {
				case "bus":
					profile = "bus";
					break;
				case "ipt":
					profile = "ipt";
					break;
				case "metro":
					profile = "metro";
					break;
				default:
					mode= TransportMode.valueOf(p.getVehicle());
					pathChoice= PathChoice.valueOf(p.getRouteType());
					if(pathChoice.toString().equals("all")==false)
						profile = TrafficAndRoutingService.getModeBasedPathChoice(pathChoice, mode);
					break;
			}

			if(profile.length()!=0)
			{
				result.add(finalPath(p,profile,mode));
			}
			else
			{
				for(PathChoice pc:PathChoice.values())
				{
					if(!pc.toString().equals("all"))
					{

						profile = TrafficAndRoutingService.getModeBasedPathChoice(pc, mode);
						result.add(finalPath(p,profile,mode));
					}
				}
			}
		}
		finally
		{
			readLock.unlock();
		}
		return result; //result contains latitudes and longitudes of route and instructions for navigation
	}
}
