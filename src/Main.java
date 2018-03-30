/* NAME: Ziyu Song
 * NET ID: zsong10
 * ASSIGNMENT: Project #4
 * LAB SESSION: TR 615-730pm
 * Collaborator: Sifan Ye (sye8), Qiuyue Sun (qsun15)
 * COURSE: CSC 172
 */

import java.util.Scanner;

import javax.swing.JFrame;

public class Main {

	public Graph graph;

	public static Scanner scan = new Scanner(System.in);

	public Main(String filename, String[] args) {
		graph = new Graph(filename, args);
	}

	public int argsLookUp(String[] arr, String s) {
		for (int i = 1; i < arr.length; i++) {
			if (arr[i].equals(s)) {
				return i;
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		Main map = new Main(args[0], args);

		// -show argument
		if (map.argsLookUp(args, "-show") != -1) {
			JFrame frame = new JFrame("STREET MAPPING");
			frame.add(map.graph);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(map.graph.width - 100, map.graph.height);
			frame.setLocation(0, 0);
			map.graph.repaint();
			frame.setVisible(true);
		}
	}

}
