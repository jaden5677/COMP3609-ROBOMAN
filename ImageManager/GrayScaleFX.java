package ImageManager;

import java.util.Random;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import MainGame.GamePanel;

public class GrayScaleFX implements ImageFX {

	private static final int WIDTH = 120;
	private static final int HEIGHT = 120;
	private static final int YPOS = 250;

	private GamePanel panel;

	private int x;
	private int y;

	private BufferedImage spriteImage;
	private BufferedImage copyImage;

	Graphics2D g2;

	int time, timeChange;
	boolean originalImage, grayImage;

	public GrayScaleFX (GamePanel p) {
		panel = p;

		Random random = new Random();
		x = random.nextInt (panel.getWidth() - WIDTH);
		y = YPOS;

		time = 0;
		timeChange = 1;
		originalImage = true;
		grayImage = false;

		spriteImage = ImageManager.loadBufferedImage("images/Butterfly.png");
		copyImage = ImageManager.copyImage(spriteImage);

		copyToGray();

	}

	private int toGray (int pixel) {

		int alpha, red, green, blue, gray;
		int newPixel;

		alpha = (pixel >> 24) & 255;
		red = (pixel >> 16) & 255;
		green = (pixel >> 8) & 255;
		blue = pixel & 255;

		gray = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);

		red = green = blue = gray;

		newPixel = blue | (green << 8) | (red << 16) | (alpha << 24);

		return newPixel;
	}

	private void copyToGray() {
		int imWidth = copyImage.getWidth();
		int imHeight = copyImage.getHeight();

		int [] pixels = new int[imWidth * imHeight];
		copyImage.getRGB(0, 0, imWidth, imHeight, pixels, 0, imWidth);

		for (int i=0; i<pixels.length; i++) {
			pixels[i] = toGray(pixels[i]);
		}

		copyImage.setRGB(0, 0, imWidth, imHeight, pixels, 0, imWidth);
	}

	public void draw (Graphics2D g2) {

		if (originalImage) {
			g2.drawImage(spriteImage, x, y, WIDTH, HEIGHT, null);
		}
		else
		if (grayImage) {
			g2.drawImage(copyImage, x, y, WIDTH, HEIGHT, null);
		}
	}

	public Rectangle2D.Double getBoundingRectangle() {
		return new Rectangle2D.Double (x, y, WIDTH, HEIGHT);
	}

	public void update() {

		time = time + timeChange;

		if (time < 20) {
			originalImage = true;
			grayImage = false;
		}
		else
		if (time < 40) {
			originalImage = false;
			grayImage = true;
		}
		else {
			time = 0;
		}
	}

}