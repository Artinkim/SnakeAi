import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Graph {
	ArrayList<Double> arr = new ArrayList<Double>();
	private int height;
	private int width;
	private double max = 0;
	Panel panel;

	Graph(int height, int width) {
		this.height = height;
		this.width = width;
		JFrame frame = new JFrame();
		frame.setSize(width, height);
		frame.setVisible(true);
		panel = new Panel();
		frame.add(panel);
	}
	Graph(int height, int width, String name) {
		this.height = height;
		this.width = width;
		JFrame frame = new JFrame(name);
		frame.setSize(width, height);
		frame.setVisible(true);
		panel = new Panel();
		frame.add(panel);
	}

	void addValue(double d) {
		arr.add(d);
		max = Math.max(max, d);
	}

	class Panel extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (arr.size() > 1) {
				g.setColor(new Color(255, 0, 0));
				for (int i = 0; i < arr.size() - 1; i++) {
					g.drawLine((width * i / arr.size()), (int) (height - ((height / max) * arr.get(i))),
							(width * (i + 1) / arr.size()), (int) (height - (height / max) * arr.get(i + 1)));
				}
			}
		}
	}
}
