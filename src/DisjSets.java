/* NAME: Ziyu Song
 * NET ID: zsong10
 * ASSIGNMENT: Project #4
 * LAB SESSION: TR 615-730pm
 * Collaborator: Sifan Ye (sye8), Qiuyue Sun (qsun15)
 * COURSE: CSC 172
 */

import java.util.HashMap;

/**
 * For Kruskal's Algorithm
 */
public class DisjSets {

	private HashMap<String, String> set;

	public DisjSets(HashMap<String, String> set) {
		this.set = set;
	}

	public String find(String x) {
		if (set.get(x).equals(x)) {
			return x;
		} else {
			return find(set.get(x));
		}
	}

	public void union(String root1, String root2) {
		String set1 = find(root1);
		String set2 = find(root2);
		set.replace(set2, set1);
	}

}
