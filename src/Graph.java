import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Graph {
	ArrayList<Double> arr = new ArrayList<Double>();
	public int height;
	public int width;
	public double max = 0;
	Panel panel;

	Graph(int height, int width) {
		this.height = height;
		this.width = width;
		Frame frame = new Frame();
		panel = new Panel();
		frame.add(panel);
	}

	class Frame extends JFrame {
		Frame() {
			this.setSize(width, height);
			this.setVisible(true);
		}
	}

	void addValue(double d) {
		arr.add(d);
		max = Math.max(max, d);
	}

	class Panel extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
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
