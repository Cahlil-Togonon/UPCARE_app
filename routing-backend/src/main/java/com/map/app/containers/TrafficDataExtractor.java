package com.map.app.containers;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.graphhopper.util.shapes.BBox;
import com.map.app.service.TrafficAndRoutingService;
import com.map.app.service.TransportMode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import com.map.app.model.TrafficData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author  Siftee, Amit
 */
public class TrafficDataExtractor {
	private TrafficData dt = new TrafficData();

	private final Lock writeLock;
	private final GraphHopper hopper;
	public TrafficDataExtractor(GraphHopper hopper, Lock lock) {
		this.hopper = hopper;
		this.writeLock = lock;
	}

	public void fetchData(String apiKey, BBox boundingBox) {
//		final String URL = "https://traffic.ls.hereapi.com/traffic/6.2/flow.xml?apiKey="
//				+apiKey +"&bbox="
//				+boundingBox.minLat+","+boundingBox.minLon + ";"
//				+ boundingBox.maxLat + "," + boundingBox.maxLon +
//				"&responseattributes=sh,fc&units=metric";
//		parse_XML(URL);
		// https://www.here.com/docs/bundle/traffic-api-developer-guide-v7/page/topics/concepts/flow.html#flow
		final String URL = "https://data.traffic.hereapi.com/v7/flow?"
				+ "apiKey=" + apiKey
				+ "&in=bbox:" + boundingBox.minLon + "," + boundingBox.minLat + "," + boundingBox.maxLon + "," + boundingBox.maxLat
				+ "&locationReferencing=shape&minJamFactor=0&maxJamFactor=10&functionalClasses=1&functionalClasses=2&functionalClasses=3&functionalClasses=4&functionalClasses=5&advancedFeatures=deepCoverage";
		System.out.println(URL);
		parse_JSON(URL);
	}

	public void feed(TrafficData tempdt) {
		writeLock.lock();
		try {
			lockedFeed(tempdt);
		} finally {
			writeLock.unlock();
		}
	}

	private void lockedFeed(TrafficData tempdt) {
		this.dt = tempdt;
		Graph graph = hopper.getGraphHopperStorage().getBaseGraph();
		for (TransportMode mode: TransportMode.values()) {
			FlagEncoder encoder = hopper.getEncodingManager().getEncoder(mode.toString());
			LocationIndex locationIndex = hopper.getLocationIndex();
//			int errors = 0;
//			int updates = 0;
			Set<Integer> edgeIds = new HashSet<> ();
			for (int i = 0; i<dt.getLat().size(); i++) {
				List<Float> entryLats = dt.getLat().get(i);
				List<Float> entryLons = dt.getLons().get(i);
				List<Float> entrySpeed = dt.getSpeed().get(i);
				for(int j=0;j<entryLats.size();j++)
				{
				Float latitude = entryLats.get(j);
				Float longitude = entryLons.get(j);
				Snap qr = locationIndex.findClosest(latitude, longitude, EdgeFilter.ALL_EDGES);
				if (!qr.isValid()) {
					// logger.info("no matching road found for entry " + entry.getId() + " at " + point);
//					errors++;
					continue;
				}
				int edgeId = qr.getClosestEdge().getEdge();
				if (edgeIds.contains(edgeId)) {
					// TODO this wouldn't happen with our map matching component
//					errors++;
					continue;
				}
				edgeIds.add(edgeId);
				EdgeIteratorState edge = graph.getEdgeIteratorState(edgeId, Integer.MIN_VALUE);
				double value;
				switch(TrafficAndRoutingService.speedChoice){
					case avg_actual_from_hereMaps:
					default:
						value = entrySpeed.get(0);
						break;
					case free_flow_from_hereMaps:
						value = entrySpeed.get(1);
						break;
					case lower_of_two:
						value = Math.min(entrySpeed.get(0), entrySpeed.get(1));
						break;
				}
				DecimalEncodedValue avgSpeedEnc = encoder.getAverageSpeedEnc();
				double oldValue = edge.get(avgSpeedEnc);
				if (value != oldValue) {
//					updates++;
					//System.out.println(avgSpeedEnc.getMaxDecimal());
					edge.set(avgSpeedEnc, Math.min(value, avgSpeedEnc.getMaxDecimal()));
				}
			}
			}
		}
	}

	private void parse_XML(String Url) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new URL(Url).openStream());
			NodeList roads = doc.getElementsByTagName("FI");
			TrafficData tempdt = new TrafficData();
			tempdt.setSpeed(new ArrayList<>());
			tempdt.setLat(new ArrayList<>());
			tempdt.setLons(new ArrayList<>());

			for (int i = 0; i<roads.getLength(); i++) {
				Node road_1 = roads.item(i);
				if (road_1.getNodeType() == Node.ELEMENT_NODE) {
					Element road = (Element) road_1;
//					float le = 0;
					float fc = 5;
					float su = 0;
					float cn = 0;
					float ff = 0;
					NodeList myxml = road.getChildNodes();
					Element traf_info=(Element) road.getLastChild();
					//System.out.println(tata.getAttribute("FF"));
					if(traf_info!=null)
					{
					
						if (traf_info.hasAttribute("FF")) {
							ff = Float.parseFloat(traf_info.getAttribute("FF"));
						}
						if (traf_info.hasAttribute("CN")) {
							cn = Float.parseFloat(traf_info.getAttribute("CN"));
						}
						if (traf_info.hasAttribute("SU")) {
							su = Float.parseFloat(traf_info.getAttribute("SU"));
						}
					}
					/*
					for (int j = 0; j<myxml.getLength(); j++) {
						Node child_1 = myxml.item(j);
						if (child_1.getNodeType() == Node.ELEMENT_NODE) {
							Element child = (Element) child_1;
//							if (child.hasAttribute("LE")) {
//								le = Float.parseFloat(child.getAttribute("LE"));
//							}
							if (child.hasAttribute("FC")) {
								fc = Float.parseFloat(child.getAttribute("FC"));
							}
							if (child.hasAttribute("CN")) {
								cn = Float.parseFloat(child.getAttribute("CN"));
							}
							if (child.hasAttribute("SU")) {
								su = Float.parseFloat(child.getAttribute("SU"));
							}
						
						}
					}*/
					NodeList temp_shp = road.getElementsByTagName("SHP");
					if (temp_shp.getLength() > 0) {
						Node first_shp = temp_shp.item(0);
						if (first_shp.getNodeType() == Node.ELEMENT_NODE) {
							Element shp = (Element) first_shp;
							if (shp.hasAttribute("FC")) {
								fc = Float.parseFloat(shp.getAttribute("FC"));
								//System.out.println(fc);
							}
						}
					}
					//System.out.println(fc+" "+cn+" "+su);
					if (cn >= 0.7 && fc<= TrafficAndRoutingService.functional_road_class_here_maps) {

						NodeList shps = road.getElementsByTagName("SHP");
						if (shps.getLength() > 0) {
							ArrayList<Float> las = new ArrayList<>();
							ArrayList<Float> longs = new ArrayList<>();

							for (int k = 0; k<shps.getLength(); k++) {
								Node shp_1 = shps.item(k);
								if (shp_1.getNodeType() == Node.ELEMENT_NODE) {
									Element shp = (Element) shp_1;
									String[] ans = shp.getTextContent().replace(',', ' ').split(" ");

									if (k == 0) {
								
										las.add(Float.parseFloat(ans[0]));
										longs.add(Float.parseFloat(ans[1]));
										//System.out.println(las+" "+longs);
									}

									for (int l = 1; l< ans.length / 2; l++) {
										las.add(Float.parseFloat(ans[2 * l]));
										longs.add(Float.parseFloat(ans[2 * l + 1]));
									}
								}
							}
							tempdt.getLat().add(las);
							tempdt.getLons().add(longs);
							ArrayList<Float> combospeed = new ArrayList<>(2);
							combospeed.add(su);
							combospeed.add(ff);
							tempdt.getSpeed().add(combospeed);

						}
					}

				}

			}
			//System.out.println(tempdt.getLat());
			feed(tempdt);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println("Traffic parsing done...");
		}

	}

	public void parse_JSON(String urlString) {
		try {
			String jsonResponse = getJsonFromUrl(urlString);
			JSONObject jsonObject = new JSONObject(jsonResponse);
			JSONArray results = jsonObject.getJSONArray("results");

			TrafficData tempdt = new TrafficData();
			tempdt.setSpeed(new ArrayList<>());
			tempdt.setLat(new ArrayList<>());
			tempdt.setLons(new ArrayList<>());

			for (int i = 0; i < results.length(); i++) {
				JSONObject road = results.getJSONObject(i);
				JSONObject location = road.getJSONObject("location");
				JSONObject currentFlow = road.getJSONObject("currentFlow");

				JSONArray links = location.getJSONObject("shape").getJSONArray("links");
				ArrayList<Float> las = new ArrayList<>();
				ArrayList<Float> longs = new ArrayList<>();

				for (int j = 0; j < links.length(); j++) {
					JSONArray points = links.getJSONObject(j).getJSONArray("points");
					for (int k = 0; k < points.length(); k++) {
						JSONObject point = points.getJSONObject(k);
						las.add((float) point.getDouble("lat"));
						longs.add((float) point.getDouble("lng"));
					}
				}

				// Use subSegments if available
				if (currentFlow.has("subSegments")) {
					JSONArray subSegments = currentFlow.getJSONArray("subSegments");

					for (int s = 0; s < subSegments.length(); s++) {
						JSONObject sub = subSegments.getJSONObject(s);

						if (sub.has("speedUncapped") && sub.has("freeFlow")) {
							ArrayList<Float> speedInfo = new ArrayList<>();
							speedInfo.add((float) sub.getDouble("speedUncapped"));
							speedInfo.add((float) sub.getDouble("freeFlow"));

							tempdt.getLat().add(new ArrayList<>(las));
							tempdt.getLons().add(new ArrayList<>(longs));
							tempdt.getSpeed().add(speedInfo);
						}
					}
				} else {
					// Fallback to original logic
					if (currentFlow.has("confidence") && currentFlow.getDouble("confidence") >= 0.7
							&& currentFlow.has("speedUncapped") && currentFlow.has("freeFlow")) {
						ArrayList<Float> speedInfo = new ArrayList<>();
						speedInfo.add((float) currentFlow.getDouble("speedUncapped"));
						speedInfo.add((float) currentFlow.getDouble("freeFlow"));

						tempdt.getLat().add(las);
						tempdt.getLons().add(longs);
						tempdt.getSpeed().add(speedInfo);
					}
				}
			}

			feed(tempdt);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Traffic JSON parsing done...");
		}
	}


	// Helper function to fetch JSON from URL
	private String getJsonFromUrl(String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	public TrafficData getRoads() {
		return dt;
	}

}