/* NAME: Ziyu Song
 * NET ID: zsong10
 * ASSIGNMENT: Project #4
 * LAB SESSION: TR 615-730pm
 * Collaborator: Sifan Ye (sye8), Qiuyue Sun (qsun15)
 * COURSE: CSC 172
 */

public class Edge implements Comparable<Edge> {

	public final String id;
	public double weight;
	public Vertex v, w;

	public Edge(String id, Vertex v, Vertex w) {
		this.id = id;
		this.v = v;
		this.w = w;
		this.weight = Math.sqrt(Math.pow((v.latitude - w.latitude), 2) + Math.pow((v.longitude - w.longitude), 2));
	}

	@Override
	public int compareTo(Edge e) {
		if ((this.weight - e.weight) < 0) {
			return -1;
		} else if ((this.weight - e.weight) > 0) {
			return 1;
		} else {
			return 0;
		}
	}

	public String toString() {
		return "ID: " + id + "; Start Point: " + v + "; End Point: " + w + ".";
	}
	
}