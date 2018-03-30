/* NAME: Ziyu Song
 * NET ID: zsong10
 * ASSIGNMENT: Project #4
 * LAB SESSION: TR 615-730pm
 * Collaborator: Sifan Ye (sye8), Qiuyue Sun (qsun15)
 * COURSE: CSC 172
 */

public class Vertex implements Comparable<Vertex> {

	public final String id;
	public double dist, distMile, longitude, latitude;
	public boolean known;
	public Vertex path;

	public Vertex(String id, double lat, double longi) {
		this.id = id;
		this.latitude = lat;
		this.longitude = longi;
		this.dist = Double.MAX_VALUE;
		this.known = false;
	}

	public void reset() {
		this.dist = Double.MAX_VALUE;
		this.known = false;
		this.path = null;
	}

	@Override
	public int compareTo(Vertex v) {
		if ((this.dist - v.dist) < 0) {
			return -1;
		} else if ((this.dist - v.dist) > 0) {
			return 1;
		} else {
			return 0;
		}
	}

	public String toString() {
		return "ID: " + id + "; Latitude: " + latitude + "; Longitude: " + longitude;
	}

}
