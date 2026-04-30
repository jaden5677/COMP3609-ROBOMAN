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
import Entities.Enemies.Boss;
import Entities.Enemies.Enemy;
import Entities.Hazards.FallingSpike;
import Entities.Items.AbstractItem;
import Entities.Items.DamageUp;
import Entities.Items.GunType;
import Entities.Items.HealthPacks;
import Entities.Items.HealthPacks.HealthPackType;
import Entities.Items.ItemInterface;
import Entities.Items.MovementUp;
import Entities.Player.Player;
import Entities.Projectiles.Projectile;
import Factory.EnemyFactory;
import Factory.ItemFactory;

public class GamePanel extends JPanel
                       implements Runnable, KeyListener {

	public static final int SCREEN_WIDTH = 1280;
	public static final int SCREEN_HEIGHT = 720;

	public enum GameState { MENU, LEVEL_1, LEVEL_2, LEVEL_COMPLETE, GAME_COMPLETE }

	private SoundManager soundManager;

	private Player player;
	private Level level;
	private Camera camera;
	private HealthBar healthBar;
	private List<Enemy> enemies;
	private List<Projectile> projectiles;
	private List<ItemInterface> items;
	private List<FallingSpike> spikes;
	private ItemFactory itemFactory;
	private ArenaController arenaController;
	private Boss bossRef;
	private boolean bossDeathSeen;

	private boolean isRunning;
	private boolean isPaused;
	private Thread gameThread;
	private BufferedImage image;
	private ParallaxBackground parallax;

	private GameState state;
	private GameState completedLevel;
	private MenuScreen menuScreen;
	private LevelCompleteOverlay completeOverlay;

	private int score;

	public GamePanel () {
		isRunning = false;
		isPaused = false;
		soundManager = SoundManager.getInstance();

		parallax = new ParallaxBackground(SCREEN_WIDTH, SCREEN_HEIGHT);
		image = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);

		menuScreen      = new MenuScreen(parallax);
		completeOverlay = new LevelCompleteOverlay();
		state           = GameState.MENU;

		setFocusable(true);
		addKeyListener(this);

		score = 0;
	}

	public void createGameEntities() {
		loadLevel(GameState.LEVEL_1);
	}

	private void loadLevel(GameState which) {
		level = new Level();
		if (which == GameState.LEVEL_2) {
			level.loadLevel2();
		} else {
			level.loadPathOne();
		}

		camera = new Camera(SCREEN_WIDTH, SCREEN_HEIGHT);
		healthBar = new HealthBar();
		projectiles = new ArrayList<>();
		spikes      = new ArrayList<>();

		player = new Player(level.getPlayerSpawnX(), level.getPlayerSpawnY(), level);
		enemies = level.createEnemies(player);

		bossRef = null;
		bossDeathSeen = false;
		for (Enemy e : enemies) {
			if (e instanceof Boss) { bossRef = (Boss) e; break; }
		}

		itemFactory = new ItemFactory();
		items = new ArrayList<>();
		items.addAll(level.createItems());
		spawnStarterItems();

		arenaController = null;
		if (level.hasArena()) {
			arenaController = new ArenaController(
				level, player,
				new EnemyFactory(player, level),
				enemies,
				items);
		}

		score = 0;
		state = which;
	}

	private void spawnStarterItems() {
		int spawnX = level.getPlayerSpawnX();
		int spawnY = level.getPlayerSpawnY();

		int floorY = spawnY + Level.TILE_SIZE - 32;
		int stepX  = 56;
		int baseX  = spawnX + Level.TILE_SIZE;

		items.add(itemFactory.createHealthPack(baseX + 0 * stepX, floorY, HealthPackType.SMALL));
		items.add(itemFactory.createHealthPack(baseX + 1 * stepX, floorY, HealthPackType.MEDIUM));
		items.add(itemFactory.createHealthPack(baseX + 2 * stepX, floorY, HealthPackType.LARGE));

		items.add(itemFactory.createDamageUp(baseX + 3 * stepX, floorY, DamageUp.Tier.MEDIUM));

		items.add(itemFactory.createMovementUp(baseX + 4 * stepX, floorY, MovementUp.BoostType.JUMP,  4));
		items.add(itemFactory.createMovementUp(baseX + 5 * stepX, floorY, MovementUp.BoostType.SPEED, 2));

		items.add(itemFactory.createGunType(baseX + 6 * stepX, floorY, GunType.Variant.TRIPLE_SHOT));
		items.add(itemFactory.createGunType(baseX + 7 * stepX, floorY, GunType.Variant.CHARGE_SHOT));
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

		if (state == GameState.MENU) {
			menuScreen.update();
			return;
		}
		if (state == GameState.LEVEL_COMPLETE || state == GameState.GAME_COMPLETE) {
			return;
		}

		player.savePreviousPosition();
		for (Enemy enemy : enemies) {
			enemy.savePreviousPosition();
		}
		for (Projectile p : projectiles) {
			p.savePreviousPosition();
		}

		player.update();

		projectiles.addAll(player.getProjectiles());
		player.getProjectiles().clear();

		Iterator<ItemInterface> itemIt = items.iterator();
		while (itemIt.hasNext()) {
			ItemInterface item = itemIt.next();
			item.update();
			if (!item.isVisible()) {
				itemIt.remove();
				continue;
			}
			if (item.getBoundingRectangle().intersects(player.getBoundingRectangle())) {
				item.applyToPlayer(player);
				itemIt.remove();
			}
		}

		for (Enemy enemy : enemies) {
			enemy.update();
			projectiles.addAll(enemy.getProjectiles());
			enemy.getProjectiles().clear();
		}

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

				for (Enemy enemy : enemies) {
					if (enemy.isAlive() &&
					    p.getBoundingRectangle().intersects(enemy.getBoundingRectangle())) {

						if (enemy instanceof Boss && ((Boss) enemy).isAttacking()) {
							p.reflect();
							break;
						}
						enemy.takeDamage(p.getProjectileDamage(), p.getType());
						if (!enemy.isAlive()) {
							score += enemy.getPoints();
						}
						it.remove();
						break;
					}
				}
			} else {

				if (p.getBoundingRectangle().intersects(player.getBoundingRectangle())) {
					if (p.getType() == Projectile.Type.ENEMY_ELECTRIC) {
						player.stun(2000);
					}
					player.takeDamage(p.getProjectileDamage());
					it.remove();
				}
			}
		}

		if (arenaController != null) {
			arenaController.update(System.currentTimeMillis());
		}

		updateBossSpikeAttack();
		updateSpikes();

		camera.follow(player, level.getMapPixelWidth(), level.getMapPixelHeight());

		checkLevelCompletion();
	}

	private void updateBossSpikeAttack() {
		if (state != GameState.LEVEL_2) return;
		if (bossRef == null || !bossRef.isAlive()) return;
		List<int[]> emitters = level.getSpikeEmitters();
		if (emitters == null || emitters.isEmpty()) return;
		List<int[]> picks = bossRef.pollSpikeAttack(System.currentTimeMillis(), emitters);
		for (int[] pt : picks) {
			spikes.add(new FallingSpike(pt[0], pt[1], level.getSpikeSprite()));
		}
	}

	private void updateSpikes() {
		if (spikes == null || spikes.isEmpty()) return;
		Iterator<FallingSpike> sit = spikes.iterator();
		while (sit.hasNext()) {
			FallingSpike sp = sit.next();
			sp.update(level);
			if (!sp.isAlive()) { sit.remove(); continue; }

			if (sp.getBoundingRectangle().intersects(player.getBoundingRectangle())) {
				player.takeDamage(sp.getDamage());
				sp.kill();
				sit.remove();
				continue;
			}

			for (Enemy enemy : enemies) {
				if (!enemy.isAlive()) continue;
				if (sp.getBoundingRectangle().intersects(enemy.getBoundingRectangle())) {
					enemy.takeDamage(sp.getDamage(), Projectile.Type.ENEMY_NORMAL);
					if (!enemy.isAlive()) score += enemy.getPoints();
					sp.kill();
					sit.remove();
					break;
				}
			}
		}
	}

	private void checkLevelCompletion() {
		if (state == GameState.LEVEL_1) {
			int doorX = level.getDoorWorldX();
			if (doorX >= 0 && level.hasKey()
			    && player.x >= doorX + Level.TILE_SIZE / 2) {
				completedLevel = GameState.LEVEL_1;
				state = GameState.LEVEL_COMPLETE;
			}
		} else if (state == GameState.LEVEL_2) {

			if (bossRef != null
			    && !bossRef.isAlive()
			    && bossRef.isDeathSequenceComplete()
			    && !bossDeathSeen) {
				bossDeathSeen = true;
				completedLevel = GameState.LEVEL_2;
				state = GameState.GAME_COMPLETE;
			}
		}
	}

	public void gameRender() {
		Graphics2D imageContext = (Graphics2D) image.getGraphics();

		imageContext.setColor(new Color(30, 30, 50));
		imageContext.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

		if (state == GameState.MENU) {
			menuScreen.draw(imageContext, SCREEN_WIDTH, SCREEN_HEIGHT);
			blit(imageContext);
			return;
		}

		if (parallax != null && !level.isLevel2Mode()) {
			int cx;
			if (camera != null) {
				cx = camera.getX();
			} else {
				cx = 0;
			}
			parallax.draw(imageContext, cx);
		}

		imageContext.translate(-camera.getX(), -camera.getY());

		level.draw(imageContext);

		for (ItemInterface item : items) {
			item.draw(imageContext);
		}

		for (Enemy enemy : enemies) {
			enemy.draw(imageContext);
		}

		player.draw(imageContext);

		for (Projectile p : new ArrayList<>(projectiles)) {
			p.draw(imageContext);
		}

		if (spikes != null) {
			for (FallingSpike sp : spikes) sp.draw(imageContext);
		}

		imageContext.translate(camera.getX(), camera.getY());

		healthBar.draw(imageContext, player);

		imageContext.setColor(Color.WHITE);
		imageContext.setFont(new Font("Arial", Font.BOLD, 42));
		imageContext.drawString("Score: " + score, 30, 180);

		if (arenaController != null) {
			arenaController.draw(imageContext);
		}

		if (state == GameState.LEVEL_COMPLETE) {
			completeOverlay.draw(imageContext, SCREEN_WIDTH, SCREEN_HEIGHT,
				"LEVEL COMPLETE!", "Press ENTER to continue");
		} else if (state == GameState.GAME_COMPLETE) {
			completeOverlay.draw(imageContext, SCREEN_WIDTH, SCREEN_HEIGHT,
				"GAME COMPLETE!", "Press ENTER to return to the menu");
		}

		blit(imageContext);
	}

	private void blit(Graphics2D imageContext) {
		Graphics2D g2 = (Graphics2D) getGraphics();
		if (g2 != null) {
			g2.drawImage(image, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
			g2.dispose();
		}
		imageContext.dispose();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();

		if (state == GameState.MENU) {
			if (code == KeyEvent.VK_1) loadLevel(GameState.LEVEL_1);
			else if (code == KeyEvent.VK_2) loadLevel(GameState.LEVEL_2);
			return;
		}
		if (state == GameState.LEVEL_COMPLETE) {
			if (code == KeyEvent.VK_ENTER) {
				if (completedLevel == GameState.LEVEL_1) {
					loadLevel(GameState.LEVEL_2);
				} else {
					loadLevel(GameState.LEVEL_1);
				}
			}
			return;
		}
		if (state == GameState.GAME_COMPLETE) {
			if (code == KeyEvent.VK_ENTER) state = GameState.MENU;
			return;
		}
		if (code == KeyEvent.VK_ESCAPE) {
			state = GameState.MENU;
			return;
		}

		if (player == null) return;
		switch (code) {
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

	public void startGame() {
		if (gameThread == null) {

			state = GameState.MENU;
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