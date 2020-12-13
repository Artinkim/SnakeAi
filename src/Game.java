import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game {
	public static void main(String[] args) throws IOException {
		new Game().init();
	}

	// Width and height of JFrame, GENERATION_SIZE number of snakes per generation
	// X and Y is the size of the grid
	final int width = 500;
	final int height = 500;
	final int GENERATION_SIZE = 500;
	final int X = 20;
	final int Y = 20;
	final int ratioX = width / X;
	final int ratioY = height / Y;
	int[] dx = { 0, -1, 0, 1 };
	int[] dy = { -1, 0, 1, 0 };
	double[][] nodes = new double[4][];
	double[][][][] weights = new double[GENERATION_SIZE][3][][];
	double[][][][] weights2 = new double[GENERATION_SIZE][3][][];
	boolean[][] taken = new boolean[X + 1][Y + 1];
	double[] fitnessSums = new double[GENERATION_SIZE + 1];
	double[] fitnesses = new double[GENERATION_SIZE];
	Random rand = new Random();
	Snake snake = new Snake();
	Food food = new Food();
	int scoreFood = 0;
	int scoreMove = 0;
	int snakeCount = 0;
	int generationCount = 0;
	int lastFoodStep = 0;
	int topIndex = -1;
	int topFood = 0;
	GamePanel gamePanel;
	Graph graphAvg;
	Graph graphTop;
	final int layer1 = 12; // Layer sizes
	final int layer2 = 10;
	final int layer3 = 8;
	final int layer4 = 4;
	final boolean TRAIN = false, Display = false;
	void initView() { // Initializes JFrames
		graphAvg = new Graph(width, height, "graphAvg");
		graphTop = new Graph(width, height, "graphTop");
		JFrame frame = new JFrame("Game Display");
		gamePanel = new GamePanel();
		frame.add(gamePanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(width, height);
		frame.setVisible(true);
	}

	void init() throws IOException {
		for (int i = 0; i < GENERATION_SIZE; i++) {
			weights[i][0] = new double[layer2][layer1];
			weights[i][1] = new double[layer3][layer2];
			weights[i][2] = new double[layer4][layer3];
			weights2[i][0] = new double[layer2][layer1];
			weights2[i][1] = new double[layer3][layer2];
			weights2[i][2] = new double[layer4][layer3];
		}
		nodes[0] = new double[layer1];
		nodes[1] = new double[layer2];
		nodes[2] = new double[layer3];
		nodes[3] = new double[layer4];
		initView(); // Initializes JFrames
		setWeights(); // Reads file and sets weights
//		initRandomWieghts(); // Sets weights randomly
		while (true) {
			while (snakeCount < GENERATION_SIZE) {
				// Run all snakes
				if (scoreFood > (X * Y) - 40) {
					
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						Thread.sleep(0);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				update();
				gamePanel.repaint();
			}
			System.out.println(generationCount);
			calculateFitnessSums(); // Calculate Fitnesses Sums array and find the snake who got the highest score
			System.out.println(
					"Best Snake Ate: " + ((double) topFood / ((double) X * (double) Y)) * 100+0.5 + "% of the food \n");
			if (topFood > (X * Y) - 3) { //If snake beat game show snake play (snakeCount is already set to best snake in calculateFitnessSums
				while (topIndex == snakeCount) {
					update();
					if (scoreFood > (X * Y) - 400) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					gamePanel.repaint();
				}
			}
//			if(topFood >= (X*Y)-2) {
//				System.out.println(topIndex);
//				break;
//			}
			nextGeneration(); // Calculate weights for the next generation and store them into weights2
			// System.out.println(weights + " " + weights2);
			weights = weights2.clone();
			if (generationCount % 10 == 0) { // Every 50 generations write weights to file
				writeWeights();
				System.out.println("Weights Written to file");
			}
			snakeCount = 0;
			topFood = 0;
			generationCount++;
			if (generationCount > 3000) { // Stops after certain number of generations and writes to file
				writeWeights();
				System.out.println("Done");
				break;
			}
		}
	}

	void initRandomWieghts() { // Sets weights randomly
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				for (int k = 0; k < weights[i][j].length; k++) {
					for (int l = 0; l < weights[i][j][k].length; l++) {
						weights[i][j][k][l] = (rand.nextDouble() * 2.0) - 1.0;
					}
				}
			}
		}
	}

	void setWeights() throws IOException { // Reads file and sets weights
		BufferedReader br = new BufferedReader(new FileReader("evolutions.txt"));
		for (int i = 0; i < weights.length; i++) {
			StringTokenizer st = new StringTokenizer(br.readLine());
			for (int j = 0; j < weights[i].length; j++) {
				for (int k = 0; k < weights[i][j].length; k++) {
					for (int l = 0; l < weights[i][j][k].length; l++) {
						weights[i][j][k][l] = Double.parseDouble(st.nextToken());
					}
				}
			}
		}
		br.close();
	}

	void calculateFitnessSums() { // Calculate Fitnesses Sums array and find the snake who got the highest score
		double topFitness = 0;
		for (int i = 0; i < GENERATION_SIZE; i++) {
			fitnessSums[i + 1] = fitnessSums[i] + fitnesses[i];
			if (fitnesses[i] > topFitness) {
				topFitness = fitnesses[i];
				topIndex = i;
			}
		}
		snakeCount = topIndex;
		System.out.println("Highest Fitness: " + topFitness + "\n");
		System.out.println("Average Fitness: " + fitnessSums[GENERATION_SIZE] / GENERATION_SIZE + "\n");
//		graphTop.addValue(topFitness); // Adds values of top and average fitness to graphs
		graphAvg.addValue(fitnessSums[GENERATION_SIZE] / GENERATION_SIZE);
		graphAvg.panel.repaint();
//		graphTop.panel.repaint();
	}

	void writeWeights() throws IOException { // Writes weights2 to file
		BufferedWriter bw = new BufferedWriter(new FileWriter("evolutions.txt"));
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				for (int k = 0; k < weights[i][j].length; k++) {
					for (int l = 0; l < weights[i][j][k].length; l++) {
						bw.write(weights2[i][j][k][l] + " ");
					}
				}
			}
			bw.write("\n");
		}
		bw.close();
	}

	void nextGeneration() { // Calculate weights for the next generation and store them into weights2
		for (int i = 0; i < weights.length; i++) {
			int temp1 = Arrays.binarySearch(fitnessSums, rand.nextDouble() * fitnessSums[GENERATION_SIZE]);
			int temp2 = Arrays.binarySearch(fitnessSums, rand.nextDouble() * fitnessSums[GENERATION_SIZE]);
			int index1 = Math.max(temp1, -(temp1 + 2)); // Find the two parents of the new snake(based on fitness)
			int index2 = Math.max(temp2, -(temp2 + 2));
			for (int j = 0; j < weights[i].length; j++) {
				int temp = (rand.nextBoolean()) ? index2 : index1; // Randomly choose one of the parents weights to use
				for (int k = 0; k < weights[i][j].length; k++) {// Set new weights with a mutation(plus or minus 5%)
					for (int l = 0; l < weights[i][j][k].length; l++) {
						weights2[i][j][k][l] = (1 + (rand.nextDouble() * 0.1) - 0.05) * weights[temp][j][k][l];
					}
				}
			}
		}
	}

	double inverse(int d) { // Inverse function that returns 0 for 0
		if (d == 0)
			return 0;
		return 1.0 / d;
	}

	void update() {
		Pair current = snake.snakeParts.get(snake.headIndex);
		nodes[0][0] = inverse(current.x + 1);
		nodes[0][1] = inverse(current.y + 1);
		nodes[0][2] = inverse(Y - current.y);
		nodes[0][3] = inverse(X - current.x);
//		nodes[0][12] = inverse(Math.sqrt(current.x * current.y)); // Euclidian distances from walls
//		nodes[0][13] = inverse(Math.sqrt((X - current.x) * (Y - current.y)));
//		nodes[0][14] = inverse(Math.sqrt((current.x) * (Y - current.y)));
//		nodes[0][15] = inverse(Math.sqrt((X - current.x) * (current.y)));
		nodes[0][4] = 0;
		nodes[0][5] = 0;
		nodes[0][6] = 0;
		nodes[0][7] = 0;
		for (int i = current.x + 1; i < X; i++) {
			if (taken[i][current.y]) {
				nodes[0][4] = inverse(i - current.x);
				break;
			}
		}
		for (int i = current.x - 1; i > -1; i--) {
			if (taken[i][current.y]) {
				nodes[0][5] = inverse(current.x - i);
				break;
			}
		}
		for (int i = current.y + 1; i < Y; i++) {
			if (taken[current.x][i]) {
				nodes[0][6] = inverse(i - current.y);
				break;
			}
		}
		for (int i = current.y - 1; i > -1; i--) {
			if (taken[current.x][i]) {
				nodes[0][7] = inverse(current.y - i);
				break;
			}
		}
		nodes[0][8] = inverse(Math.max(0, current.x - food.x));
		nodes[0][9] = inverse(Math.max(0, current.y - food.y));
		nodes[0][10] = inverse(Math.max(0, food.x - current.x));
		nodes[0][11] = inverse(Math.max(0, food.y - current.y));
//		nodes[0][snake.direction + 12] = 1; // Direction input
		updateOutput();
		double value = Math.max(nodes[nodes.length - 1][0],
				Math.max(nodes[nodes.length - 1][1], Math.max(nodes[nodes.length - 1][2], nodes[nodes.length - 1][3])));
		int index = rand.nextInt(4);
		while (nodes[nodes.length - 1][index] != value) {
			index = rand.nextInt(4);
		}
		snake.update(index);
		if (scoreMove - lastFoodStep > X * Y || scoreFood > (X * Y) - 3) {//If going in loop or completed game
			dead();
		}
	}

	void updateOutput() { // updates outputs of nodes based on weights of snakeCount
		for (int k = 0; k < weights[snakeCount].length; k++) {
			for (int i = 0; i < weights[snakeCount][k].length; i++) {
				double temp = 0;
				for (int j = 0; j < weights[snakeCount][k][i].length; j++) {
					temp += nodes[k][j] * weights[snakeCount][k][i][j];
				}
				nodes[k + 1][i] = Math.max(temp, 0);
			}
		}
//		for (int i = 0; i < weights[snakeCount][weights[snakeCount].length - 1].length; i++) { // Can use for sigmoid output layer
//			double temp = 0;
//			for (int j = 0; j < weights[snakeCount][weights[snakeCount].length - 1][i].length; j++) {
//				temp += nodes[weights[snakeCount].length - 1][j]
//						* weights[snakeCount][weights[snakeCount].length - 1][i][j];
//			}
//			nodes[nodes.length - 1][i] = (1 / (1 + Math.pow(Math.E, (-1 * temp))));
////			System.out.println(nodes[nodes.length - 1][i]);
//		}
	}

	void dead() { // Called when snake dies, calculates fitness score, resets variables
		fitnesses[snakeCount] = (scoreMove + (Math.pow(2, scoreFood) + Math.pow(scoreFood, 2.1) * 500.0)
				- (Math.pow(scoreFood, 1.2) * Math.pow(0.25 * scoreMove, 1.3)));
		topFood = Math.max(topFood, scoreFood);
		for (boolean[] b : taken)
			Arrays.fill(b, false);
		snake = new Snake();
		food = new Food();
		scoreMove = 0;
		scoreFood = 0;
		snakeCount++;
	}

	class GamePanel extends JPanel {
		@Override
		public void paintComponent(Graphics g) { // Calls draw of food and snake
			super.paintComponent(g);
			g.setColor(new Color(255, 0, 0));
			snake.draw(g);
			g.setColor(new Color(0, 255, 0));
			food.draw(g);

		}
	}

	public class Snake {
		ArrayList<Pair> snakeParts;
		int direction = 1;
		int headIndex = 1;
		int tailIndex = 0;

		Snake() { // Creates the snake with a head and tail at the center of the screen
			snakeParts = new ArrayList<Pair>();
			snakeParts.add(new Pair(X / 2, Y / 2));
			snakeParts.add(new Pair((X / 2) - 1, Y / 2));
			taken[X / 2][Y / 2] = true;
			taken[(X / 2) - 1][(Y / 2)] = true;
		}

		void update(int d) {
			scoreMove++;
			if ((d + 2) % 4 != direction) // Set direction of snake(Does not allow opposite direction)
				direction = d;
			Pair head = snakeParts.get(headIndex);
			if ((head.x + dx[direction] < 0 || head.x + dx[direction] > X - 1) // Checks if hits wall
					|| (head.y + dy[direction] < 0 || head.y + dy[direction] > Y - 1)) {
				dead();
				return;
			} 
			if (taken[head.x + dx[direction]][head.y + dy[direction]]) { // Checks if ran into self
				dead();
				return;
			}
			taken[head.x][head.y] = true;
			if (head.x == food.x && head.y == food.y) { // Snake eats food
				scoreFood++;
				food.eaten();
				snakeParts.add(tailIndex, new Pair(head.x + dx[direction], head.y + dy[direction]));
			} else { // Snake moves in direction
				taken[snakeParts.get(tailIndex).x][snakeParts.get(tailIndex).y] = false;
				snakeParts.get(tailIndex).x = head.x + dx[direction];
				snakeParts.get(tailIndex).y = head.y + dy[direction];
			}
			headIndex = tailIndex;
			if (tailIndex == snakeParts.size() - 1) {
				tailIndex = 0;
			} else {
				tailIndex++;
			}

		}

		void draw(Graphics g) { // Draw snake
			for (int i = 0; i < snakeParts.size(); i++) {
				g.fillRect(snakeParts.get(i).x * ratioX, snakeParts.get(i).y * ratioY, ratioX, ratioY);
			}
			g.setColor(new Color(128, 0, 128));
			g.fillRect(snakeParts.get(headIndex).x * ratioX, snakeParts.get(headIndex).y * ratioY, ratioX, ratioY);
		}
	}

	class Food {
		int x;
		int y;

		Food() {
			this.x = rand.nextInt(X);
			this.y = rand.nextInt(Y);
		}

		void draw(Graphics g) { // Draw Food
			g.setColor(new Color(0, 255, 0));
			g.fillRect(this.x * ratioX, this.y * ratioY, ratioX, ratioY);
		}

		void eaten() { // Set food position to where the snake is not
			lastFoodStep = scoreMove;
			boolean loop = true;
			taken[snake.snakeParts.get(snake.tailIndex).x][snake.snakeParts.get(snake.tailIndex).y] = true;
			while (loop) {
				this.x = rand.nextInt(X);
				this.y = rand.nextInt(Y);
				if (!taken[this.x][this.y])
					loop = false;
			}
			taken[snake.snakeParts.get(snake.tailIndex).x][snake.snakeParts.get(snake.tailIndex).y] = false;
		}

	}

	class Pair implements Comparable<Pair> {
		int x;
		int y;

		Pair(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo(Pair o) {
			// TODO Auto-generated method stub
			return this.x - o.x;
		}
	}
}