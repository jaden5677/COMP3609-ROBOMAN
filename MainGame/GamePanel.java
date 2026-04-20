package MainGame;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ImageManager.ImageManager;
import SoundManager.SoundManager;
import Entities.*;

/**
   A component that displays all the game entities.
*/

public class GamePanel extends JPanel
                       implements Runnable, KeyListener {

	private static final int SCREEN_WIDTH = 1500;
	private static final int SCREEN_HEIGHT = 1500;

	private SoundManager soundManager;

	private Player player;
	private Level level;
	private Camera camera;
	private HealthBar healthBar;
	private List<Enemy> enemies;
	private List<Projectile> projectiles;

	private boolean isRunning;
	private boolean isPaused;
	private Thread gameThread;
	private BufferedImage image;
	private Image backgroundImage;

	private int score;
	private boolean inArena;


	public GamePanel () {
		isRunning = false;
		isPaused = false;
		soundManager = SoundManager.getInstance();

		backgroundImage = ImageManager.loadImage("Background/Grassland/GrassLand_Background_1.png");
		image = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);

		setFocusable(true);
		addKeyListener(this);

		score = 0;
		inArena = false;
	}


	public void createGameEntities() {
		level = new Level();
		level.loadPathOne();

		camera = new Camera(SCREEN_WIDTH, SCREEN_HEIGHT);
		healthBar = new HealthBar();
		projectiles = new ArrayList<>();

		player = new Player(level.getPlayerSpawnX(), level.getPlayerSpawnY(), level);
		enemies = level.createEnemies(player);
	}


	public void run () {
		try {
			isRunning = true;
			while (isRunning) {
				if (!isPaused)
					gameUpdate();
				gameRender();
				Thread.sleep(50);
			}
		}
		catch(InterruptedException e) {}
	}


	public void gameUpdate() {
		// --- Save previous positions for erasing ---
		player.savePreviousPosition();
		for (Enemy enemy : enemies) {
			enemy.savePreviousPosition();
		}
		for (Projectile p : projectiles) {
			p.savePreviousPosition();
		}

		// --- Player ---
		player.update();

		// Collect player projectiles
		projectiles.addAll(player.getProjectiles());
		player.getProjectiles().clear();

		// --- Enemies ---
		for (Enemy enemy : enemies) {
			enemy.update();
			projectiles.addAll(enemy.getProjectiles());
			enemy.getProjectiles().clear();
		}

		// --- Projectiles & collisions ---
		Iterator<Projectile> it = projectiles.iterator();
		while (it.hasNext()) {
			Projectile p = it.next();
			p.update();

			if (!p.isActive() || p.collidesWithTile(level)) {
				it.remove();
				continue;
			}

			if (p.getType() == Projectile.Type.PLAYER_LIGHT ||
			    p.getType() == Projectile.Type.PLAYER_HEAVY) {
				// Player projectile vs enemies
				for (Enemy enemy : enemies) {
					if (enemy.isAlive() &&
					    p.getBoundingRectangle().intersects(enemy.getBoundingRectangle())) {
						enemy.takeDamage(p.getProjectileDamage(), p.getType());
						if (!enemy.isAlive()) {
							score += enemy.getPoints();
						}
						it.remove();
						break;
					}
				}
			} else {
				// Enemy projectile vs player
				if (p.getBoundingRectangle().intersects(player.getBoundingRectangle())) {
					if (p.getType() == Projectile.Type.ENEMY_ELECTRIC) {
						player.stun(2000);
					}
					player.takeDamage(p.getProjectileDamage());
					it.remove();
				}
			}
		}

		// --- Arena exit check ---
		if (inArena) {
			boolean allDead = true;
			for (Enemy e : enemies) {
				if (e.isAlive()) { allDead = false; break; }
			}
			if (allDead) {
				level.clearExitBlocks();
			}
		}

		// --- Camera ---
		camera.follow(player, level.getMapPixelWidth(), level.getMapPixelHeight());
	}


	public void gameRender() {
		Graphics2D imageContext = (Graphics2D) image.getGraphics();

		// Clear the entire buffer to prevent trails
		imageContext.setColor(new Color(30, 30, 50));
		imageContext.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

		// Background
		if (backgroundImage != null) {
			imageContext.drawImage(backgroundImage, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
		}

		// --- World space (translated by camera) ---
		imageContext.translate(-camera.getX(), -camera.getY());

		level.draw(imageContext);

		for (Enemy enemy : enemies) {
			enemy.draw(imageContext);
		}

		player.draw(imageContext);

		for (Projectile p : new ArrayList<>(projectiles)) {
			p.draw(imageContext);
		}

		imageContext.translate(camera.getX(), camera.getY());

		// --- HUD (screen space) ---
		healthBar.draw(imageContext, player);

		imageContext.setColor(Color.WHITE);
		imageContext.setFont(new Font("Arial", Font.BOLD, 42));
		imageContext.drawString("Score: " + score, 30, 180);

		// Blit to screen
		Graphics2D g2 = (Graphics2D) getGraphics();
		if (g2 != null) {
			g2.drawImage(image, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
			g2.dispose();
		}
		imageContext.dispose();
	}


	// -----------------------------------------------------------------------
	//  Input
	// -----------------------------------------------------------------------

	@Override
	public void keyPressed(KeyEvent e) {
		if (player == null) return;
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:  case KeyEvent.VK_A: player.setMoveLeft(true);    break;
			case KeyEvent.VK_RIGHT: case KeyEvent.VK_D: player.setMoveRight(true);   break;
			case KeyEvent.VK_UP:    case KeyEvent.VK_W:
			case KeyEvent.VK_SPACE:                     player.setJumpPressed(true);  break;
			case KeyEvent.VK_F:                         player.setShootPressed(true); break;
			case KeyEvent.VK_P:                         pauseGame();                  break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (player == null) return;
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:  case KeyEvent.VK_A: player.setMoveLeft(false);    break;
			case KeyEvent.VK_RIGHT: case KeyEvent.VK_D: player.setMoveRight(false);   break;
			case KeyEvent.VK_UP:    case KeyEvent.VK_W:
			case KeyEvent.VK_SPACE:                     player.setJumpPressed(false);  break;
			case KeyEvent.VK_F:                         player.setShootPressed(false); break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}


	// -----------------------------------------------------------------------
	//  Game management
	// -----------------------------------------------------------------------

	public void startGame() {
		if (gameThread == null) {
			createGameEntities();
			gameThread = new Thread(this);
			gameThread.start();
		}
	}

	public void startNewGame() {
		isPaused = false;
		if (gameThread == null || !isRunning) {
			createGameEntities();
			gameThread = new Thread(this);
			gameThread.start();
		}
	}

	public void pauseGame() {
		if (isRunning) {
			isPaused = !isPaused;
		}
	}

	public void endGame() {
		isRunning = false;
	}
}