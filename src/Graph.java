/* NAME: Ziyu Song
 * NET ID: zsong10
 * ASSIGNMENT: Project #4
 * LAB SESSION: TR 615-730pm
 * Collaborator: Sifan Ye (sye8), Qiuyue Sun (qsun15)
 * COURSE: CSC 172
 */

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Scanner;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;

import java.io.File;
import java.io.FileNotFoundException;

public class Graph extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String filename;
	public int width, height; // window width and height
	private Scanner scan;
	
	private Map<String, Vertex> vertices = new HashMap<String, Vertex>();
	private Map<String, Edge> roads = new HashMap<String, Edge>();
	private Map<String, List<String>> adjList = new HashMap<String, List<String>>();
	private Set<String> intersectID;
	
	// Dijkstra's Algorithm
	private PriorityQueue<Vertex> unknownVertex = new PriorityQueue<Vertex>();
	
	// Kruskal's Algorithm
	public HashMap<String, String> vertexSet = new HashMap<>();
	public ArrayList<Edge> mst;
	
	// for scaling the map
	public double xScale, yScale, maxLat, maxLong, minLat, minLong, ratio;
	
	// for painting the shortest path
	public boolean paintPath = false;
	public double totalDis = 0;
	public String start, end;
	
	// for painting the meridian map
	public boolean paintMeridian = false;
	
	public boolean containsIntersect(String s) {
		return intersectID.contains(s);
	}

	public Graph(String filename, String[] args) {
		// read file
		this.filename = filename;
		try {
			scan = new Scanner(new File(filename));
			while (scan.hasNextLine()) {
				if (scan.next().equals("i")) {
					String id = scan.next();
					Vertex tempVertex = new Vertex(id, scan.nextDouble(), scan.nextDouble());
					vertices.put(id, tempVertex);
					adjList.put(id, new ArrayList<String>());
					vertexSet.put(id, id);
				} else {
					String id = scan.next();
					String intersect1 = scan.next();
					String intersect2 = scan.next();
					adjList.get(intersect1).add(intersect2);
					adjList.get(intersect2).add(intersect1);
					roads.put(id, new Edge(id, vertices.get(intersect1), vertices.get(intersect2)));
				}
			}
			intersectID = vertices.keySet();

			// minimum and maximum values for longitude and latitude from previous runs, to reduce run time
			switch (filename) {
			case "ur.txt":
				maxLat = 43.131704;
				minLat = 43.125214;
				maxLong = 77.632098;
				minLong = 77.625301;
				break;
			case "monroe.txt":
				maxLat = 43.365846;
				minLat = 42.940061;
				maxLong = 77.99713699999998;
				minLong = 77.37142;
				break;
			case "nys.txt":
				maxLat = 45.010457999999986;
				minLat = 40.518202999999986;
				maxLong = 79.76214499999999;
				minLong = 71.85861899999999;
				break;
			default:
				maxLat = maxLat();
				maxLong = maxLong();
				minLat = minLat();
				minLong = minLong();
				break;
			}
			ratio = ratio();
			width = width();
			height = (int) (width * ratio);
			xScale = xScale();
			yScale = yScale();
		} catch (FileNotFoundException e) {
			System.out.println("Error! File Not Found!");
		}

		// -directions argument
		int pathIndex = argsLookup(args, "-directions");
		if (pathIndex != -1) {
			String startID = args[pathIndex + 1];
			if (filename.equals("nys.txt")) {
				switch (startID) {
				case "NYC":
					// Brooklyn Bridge
					startID = "i81216";
					break;
				case "Buffalo":
					// University of Buffalo
					startID = "i353325";
					break;
				case "Rochester":
					// college town
					startID = "i198667";
					break;
				case "Syracuse":
					startID = "i358397";
					break;
				case "Ithaca":
					startID = "i1593";
					break;
				case "Troy":
					startID = "i211427";
					break;
				case "Albany":
					startID = "i228820";
					break;
				}
			}
			String endID = args[pathIndex + 2];
			if (filename.equals("nys.txt")) {
				switch (endID) {
				case "NYC":
					// Brooklyn Bridge
					endID = "i81216";
					break;
				case "Buffalo":
					// University of Buffalo
					endID = "i353325";
					break;
				case "Rochester":
					// college town
					endID = "i198667";
					break;
				case "Syracuse":
					endID = "i358397";
					break;
				case "Ithaca":
					endID = "i1593";
					break;
				case "Troy":
					endID = "i211427";
					break;
				case "Albany":
					endID = "i228820";
					break;
				}
			}
			boolean start = containsIntersect(startID);
			boolean end = containsIntersect(endID);
			while (!start || !end) {
				if (!start) {
					System.out.println("Start Point Not Found!");
				}
				if (!end) {
					System.out.println("End Point Not Found!");
				}
				System.out.println("Please try again: ");
				startID = scan.next();
				endID = scan.next();
				start = containsIntersect(startID);
				end = containsIntersect(endID);
			}
			printPath(startID, endID);
			System.out.println();
		}
		
		// -meridianmap argument
		if (argsLookup(args, "-meridianmap") != -1) {
			kruskal();
		}
	}
	
	// shortest path
	private void dijkstra(Vertex s) {
		vertices.forEach((k, v) -> v.reset());
		s.dist = 0;
		vertices.replace(s.id, s);
		unknownVertex.add(vertices.get(s.id));
		
		while (!unknownVertex.isEmpty()) {
			Vertex v = unknownVertex.poll();
			v.known = true;
			vertices.replace(v.id, v);
			List<String> tempAdjList = adjList.get(v.id);
			
			for (String temp : tempAdjList) {
				Vertex w = vertices.get(temp);
				
				if (!w.known) {
					double cvw = Math.sqrt(Math.pow((v.latitude - w.latitude), 2) + Math.pow((v.longitude - w.longitude), 2));
					double dMile = distanceMiles(v.longitude, w.longitude, v.latitude, w.latitude);
					if (v.dist + cvw < w.dist) {
						w.dist = v.dist + cvw;
						w.distMile = dMile;
						w.path = v;
						vertices.replace(w.id, w);
						unknownVertex.add(vertices.get(w.id));
					}
				}
			}
		}
	}
	
	public void printPath(String s, String e) {
		start = s;
		end = e;
		paintPath = true;
		totalDis = 0;
		System.out.println();
		System.out.println("Shortest Path from " + s + " to " + e + ": ");
		dijkstra(vertices.get(s));
		
		if (vertices.get(e).path == null) {
			System.out.println("Sorry, Path Not Found :/");
		} else {
			printPathV(vertices.get(e));
			System.out.println();
			System.out.println("-----------------------*****-------------------------");
			System.out.printf("Total Distance Travelled: %.6f miles. \n", totalDis);
		}
		repaint();
	}
	
	private void printPathV(Vertex v) {
		if (v.path != null) {
			printPathV(v.path);
			totalDis += v.distMile;
			System.out.printf("Travel %.6f miles to: \n", v.distMile);
		}
		System.out.println(v);
	}

	// meridian map
	public void kruskal() {
		DisjSets verticesSet = new DisjSets(vertexSet);
		PriorityQueue<Edge> edgeQ = new PriorityQueue<Edge>(roads.values());
		mst = new ArrayList<Edge>();
		
		while (mst.size() != vertices.size() - 1) {
			Edge e = edgeQ.poll();
			if (e == null) {
				break;
			}
			
			String startSet = verticesSet.find(e.v.id);
			String endSet = verticesSet.find(e.w.id);
			if (!startSet.equals(endSet)) {
				mst.add(e);
				verticesSet.union(startSet, endSet);
			}
		}
		
		// print meridian map
		System.out.println("Meridian Map: ");
		for (Edge e : mst) {
			System.out.println(e);
			System.out.printf("Road length %.6f miles. \n", distanceMiles(e.v.longitude, e.w.longitude, e.v.latitude, e.w.latitude));
			System.out.println();
		}
		paintMeridian = true;
		repaint();
	}

	// haversine formula to calculate distance
	public double distanceMiles(double long1, double long2, double lat1, double lat2) {
		double dLong = Math.toRadians(long2 - long1);
		double dLat = Math.toRadians(lat2 - lat1);
		long1 = Math.toRadians(long1);
		long2 = Math.toRadians(long2);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		double R = 3959; // earth radius
		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLong / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c * 0.621371; // convert it to miles
	}

	// scale the graph
	public double maxLong() {
		double val = Double.MIN_VALUE;
		for (String s : intersectID) {
			if (Math.abs(vertices.get(s).longitude) > val) {
				val = Math.abs(vertices.get(s).longitude);
			}
		}
		return val;
	}

	public double maxLat() {
		double val = Double.MIN_VALUE;
		for (String s : intersectID) {
			if (Math.abs(vertices.get(s).latitude) > val) {
				val = Math.abs(vertices.get(s).latitude);
			}
		}
		return val;
	}

	public double minLong() {
		double val = Double.MAX_VALUE;
		for (String s : intersectID) {
			if (Math.abs(vertices.get(s).longitude) < val) {
				val = Math.abs(vertices.get(s).longitude);
			}
		}
		return val;
	}

	public double minLat() {
		double val = Double.MAX_VALUE;
		for (String s : intersectID) {
			if (Math.abs(vertices.get(s).latitude) < val) {
				val = Math.abs(vertices.get(s).latitude);
			}
		}
		return val;
	}

	// degree of longitude/latitude per pixel
	public double xScale() {
		return (height - 200) / (maxLat - minLat);
	}

	public double yScale() {
		return (width - 100) / (maxLong - minLong);
	}

	// ratio of the graph (y/x)
	public double ratio() {
		return (maxLat - minLat) / (maxLong - minLong);
	}

	// get window width or height
	public int width() {
		if (ratio < 0.8) {
			return (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.95);
		} else {
			return (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		// draw map
		Iterator<Entry<String, Edge>> it = roads.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Edge> pair = it.next();
			Edge e = ((Edge) pair.getValue());
			int y1 = (int) Math.abs((e.v.latitude - maxLat) * yScale);
			int x1 = (int) Math.abs((e.v.longitude + maxLong) * xScale);
			int y2 = (int) Math.abs((e.w.latitude - maxLat) * yScale);
			int x2 = (int) Math.abs((e.w.longitude + maxLong) * xScale);
			g.drawLine(x1, y1, x2, y2);
		}
		
		// draw meridian map
		if (paintMeridian) {
			for (Edge e : mst) {
				int x1 = (int) Math.abs((e.v.longitude + maxLong) * xScale);
				int y1 = (int) Math.abs((e.v.latitude - maxLat) * yScale);
				int x2 = (int) Math.abs((e.w.longitude + maxLong) * xScale);
				int y2 = (int) Math.abs((e.w.latitude - maxLat) * yScale);
				g.setColor(new Color(153, 51, 255));
				g.drawLine(x1, y1, x2, y2);
			}
		}
		
		// draw path
		if (paintPath) {
			Vertex st = vertices.get(start);
			Vertex ed = vertices.get(end);
			Vertex path = ed;
			int sx = (int) Math.abs((st.longitude + maxLong) * xScale);
			int sy = (int) Math.abs((st.latitude - maxLat) * yScale);
			int ex = (int) Math.abs((ed.longitude + maxLong) * xScale);
			int ey = (int) Math.abs((ed.latitude - maxLat) * yScale);
			g.setColor(Color.BLUE);
			while (path.path != null) {
				int pstx = (int) Math.abs((path.longitude + maxLong) * xScale);
				int psty = (int) Math.abs((path.latitude - maxLat) * yScale);
				int pedx = (int) Math.abs((path.path.longitude + maxLong) * xScale);
				int pedy = (int) Math.abs((path.path.latitude - maxLat) * yScale);
				path = path.path;
				g.drawLine(pstx, psty, pedx, pedy);
			}
			g.setColor(Color.GREEN);
			g.fillOval(sx - 3, sy - 3, 6, 6);
			g.setColor(Color.RED);
			g.fillOval(ex - 3, ey - 3, 6, 6);
		}
		
		// mark cities on the nys map
		if (filename.equals("nys.txt")) {
			g.setColor(new Color(85, 200, 49));
			
			g.fillRect((int) Math.abs((-74.005900 + maxLong) * xScale) - 2, (int) Math.abs((40.712700 - maxLat) * yScale) - 2, 4, 4);
			g.fillRect((int) Math.abs((-78.849444 + maxLong) * xScale) - 2, (int) Math.abs((42.904722 - maxLat) * yScale) - 2, 4, 4);
			g.fillRect((int) Math.abs((-77.611389 + maxLong) * xScale) - 2, (int) Math.abs((43.165556 - maxLat) * yScale) - 2, 4, 4);
			g.fillRect((int) Math.abs((-76.144444 + maxLong) * xScale) - 2, (int) Math.abs((43.046944 - maxLat) * yScale) - 2, 4, 4);
			g.fillRect((int) Math.abs((-76.50 + maxLong) * xScale) - 2, (int) Math.abs((42.443333 - maxLat) * yScale) - 2, 4, 4);
			g.fillRect((int) Math.abs((-73.6925 + maxLong) * xScale) - 2, (int) Math.abs((42.731660 - maxLat) * yScale) - 2, 4, 4);
			g.fillRect((int) Math.abs((-73.757222 + maxLong) * xScale) - 2, (int) Math.abs((42.6525 - maxLat) * yScale) - 2, 4, 4);
			
			g.setFont(new Font("Arial", Font.BOLD, 16));
			
			g.drawString("New York City", (int) Math.abs((-74.005900 + maxLong) * xScale) + 2, (int) Math.abs((40.712700 - maxLat) * yScale) - 2);
			g.drawString("Buffalo", (int) Math.abs((-78.849444 + maxLong) * xScale) + 2, (int) Math.abs((42.904722 - maxLat) * yScale) - 2);
			g.drawString("Rochester", (int) Math.abs((-77.611389 + maxLong) * xScale) + 2, (int) Math.abs((43.165556 - maxLat) * yScale) - 2);
			g.drawString("Syracuse", (int) Math.abs((-76.144444 + maxLong) * xScale) + 2, (int) Math.abs((43.046944 - maxLat) * yScale) - 2);
			g.drawString("Ithaca", (int) Math.abs((-76.50 + maxLong) * xScale) + 2, (int) Math.abs((42.443333 - maxLat) * yScale) - 2);
			g.drawString("Troy", (int) Math.abs((-73.6925 + maxLong) * xScale) + 2, (int) Math.abs((42.731660 - maxLat) * yScale) - 2);
			g.drawString("Albany", (int) Math.abs((-73.757222 + maxLong) * xScale) + 5, (int) Math.abs((42.6525 - maxLat) * yScale) + 5);
		}
	}

	// argument
	public int argsLookup(String[] arr, String s) {
		for (int i = 1; i < arr.length; i++) {
			if (arr[i].equals(s)) {
				return i;
			}
		}
		return -1;
	}
	
}