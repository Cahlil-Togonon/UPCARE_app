package com.map.app.controller;
import java.util.ArrayList;
import java.util.List;

import com.graphhopper.util.shapes.GHPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.map.app.containers.UrlTransformer;
import com.map.app.model.UrlContainer;
import com.map.app.model.RoutePath;
import com.map.app.model.TrafficData;
import com.map.app.service.TrafficAndRoutingService;

/**
 * @author Siftee
 */
@Controller
public class MapControl {
	@Autowired
	TrafficAndRoutingService trs;
	
	
	
	@GetMapping(value="/")
	public String read(Model model)
	{
		model.addAttribute("pt",new UrlTransformer());
		model.addAttribute("bbox",trs.getBoundingBox());
		return "index";
	}
    
	@RequestMapping(value="/routing",method=RequestMethod.GET)
	public String load(@ModelAttribute("pt") UrlTransformer pt, BindingResult errors, Model model)
    {
		
    UrlContainer rp=pt.convert();
//    System.out.println(rp.toString());
	ArrayList<RoutePath> res=trs.getPath(rp);
	model.addAttribute("route",res);
	model.addAttribute("bbox",trs.getBoundingBox());
	model.addAttribute("rbbox",res.get(0).getBounds());
   	return "index";
	}
	
	@ResponseBody 
	@RequestMapping(value = "/routing",method=RequestMethod.GET,produces="application/json")
	public ArrayList<RoutePath> fetchJSONResponse(
			@RequestParam("point") List<String> points, // List of points as strings
			@RequestParam("Vehicle") String vehicle,
			@RequestParam("RouteType") String routeType,
			@RequestParam(value = "mediaType", defaultValue = "json") String mediaType) {

		// Convert the point strings (latitude, longitude) into a list of coordinates
		ArrayList<GHPoint> waypoints = new ArrayList<>();
		for (String point : points) {
			String[] latLon = point.split(",");
			if (latLon.length == 2) {
				double latitude = Double.parseDouble(latLon[1]);
				double longitude = Double.parseDouble(latLon[0]);
				waypoints.add(new GHPoint(latitude, longitude)); // Coordinate class must be defined
			}
		}

		// Create or modify the UrlTransformer object (assuming it needs the coordinates, vehicle, routeType, etc.)
		UrlTransformer pt = new UrlTransformer();
		pt.setWaypoints(waypoints); // Assuming the setter exists
		pt.setVehicle(vehicle);
		pt.setRouteType(routeType);

		// Convert the UrlTransformer object to UrlContainer
		UrlContainer rp = pt.convert();

		// Return the path as a response
		return trs.getPath(rp);
	}
	
    @RequestMapping(value = "/traffic", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public TrafficData show()
    {
    return trs.getAll();	
    }
    
   
    
 
}
