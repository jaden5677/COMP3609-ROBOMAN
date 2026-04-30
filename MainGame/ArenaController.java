package MainGame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;

import Entities.Enemies.Enemy;
import Entities.Items.ItemInterface;
import Entities.Player.Player;
import Factory.EnemyFactory;
import Factory.EnemyFactory.EnemyKind;
import Factory.ItemFactory;

public class ArenaController {

    public enum State { INACTIVE, DWELLING, ACTIVE, CLEARING, COMPLETE }

    private static final long DWELL_REQUIRED_MS = 2_000L;
    private static final long DURATION_MS       = 120_000L;
    private static final long SPAWN_INTERVAL_MS = 7_000L;

    private static final EnemyKind[] SPAWN_POOL = {
        EnemyKind.LANCE_GUARD,
        EnemyKind.METAL_CHOMP,
        EnemyKind.THUNDORB
    };

    private final Level level;
    private final Player player;
    private final EnemyFactory factory;
    private final List<Enemy> enemies;
    private final List<ItemInterface> items;
    private final ItemFactory itemFactory = new ItemFactory();
    private final Random rng = new Random();

    private State state = State.INACTIVE;
    private long dwellStartMs;
    private long activationMs;
    private long lastSpawnMs;

    public ArenaController(Level level, Player player,
                           EnemyFactory factory,
                           List<Enemy> enemies,
                           List<ItemInterface> items) {
        this.level = level;
        this.player = player;
        this.factory = factory;
        this.enemies = enemies;
        this.items = items;
    }

    public State getState() { return state; }

    public void update(long nowMs) {
        if (!level.hasArena()) return;

        boolean inArena = level.isPlayerInArena(player.getBoundingRectangle());

        switch (state) {
            case INACTIVE:
                if (inArena) {
                    dwellStartMs = nowMs;
                    state = State.DWELLING;
                }
                break;

            case DWELLING:
                if (!inArena) {
                    state = State.INACTIVE;
                } else if (nowMs - dwellStartMs >= DWELL_REQUIRED_MS) {
                    level.activateQBlocks();
                    activationMs = nowMs;
                    lastSpawnMs = nowMs;
                    state = State.ACTIVE;
                    spawnRandomEnemy();
                }
                break;

            case ACTIVE:
                if (nowMs - activationMs >= DURATION_MS) {
                    state = State.CLEARING;
                } else if (nowMs - lastSpawnMs >= SPAWN_INTERVAL_MS) {
                    spawnRandomEnemy();
                    lastSpawnMs = nowMs;
                }
                break;

            case CLEARING:
                if (!anyArenaEnemyAlive()) {
                    level.clearExitBlocks();
                    spawnRewardKey();
                    state = State.COMPLETE;
                }
                break;

            case COMPLETE:
                break;
        }
    }

    private boolean anyArenaEnemyAlive() {
        Rectangle bounds = level.getArenaBounds();
        if (bounds == null) return false;
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            int cx = e.getX() + e.getWidth() / 2;
            int cy = e.getY() + e.getHeight() / 2;
            if (bounds.contains(cx, cy)) return true;
        }
        return false;
    }

    private void spawnRandomEnemy() {
        List<int[]> points = level.getArenaSpawnPoints();
        if (points == null || points.isEmpty()) return;
        int[] p = points.get(rng.nextInt(points.size()));
        EnemyKind kind = SPAWN_POOL[rng.nextInt(SPAWN_POOL.length)];

        int spawnX = p[0];
        int spawnY = p[1] - Level.TILE_SIZE * 3;
        if (spawnY < 0) spawnY = 0;
        Enemy e = factory.create(kind, spawnX, spawnY);
        if (e != null) {
            enemies.add(e);
        }
    }

    private void spawnRewardKey() {
        if (items == null) return;
        Rectangle b = level.getArenaBounds();
        if (b == null) return;
        int kx = b.x + b.width / 2 - 16;
        int ky = b.y + b.height - Level.TILE_SIZE;
        items.add(itemFactory.createKey(kx, ky, level));
    }

    public void draw(Graphics2D g2) {
        if (state == State.INACTIVE) return;

        long now = System.currentTimeMillis();

        if (state == State.DWELLING) {
            drawCenteredBanner(g2, "ARENA ACTIVATING...", new Color(255, 220, 100));
            return;
        }

        if (state == State.ACTIVE) {
            long remaining = Math.max(0L, DURATION_MS - (now - activationMs));
            drawTimer(g2, remaining);
            return;
        }

        if (state == State.CLEARING) {
            drawTimer(g2, 0);
            drawCenteredBanner(g2, "CLEAR REMAINING ENEMIES", new Color(255, 120, 120));
            return;
        }

        if (state == State.COMPLETE) {
            drawCenteredBanner(g2, "LEVEL 1 COMPLETE", new Color(120, 255, 140));
        }
    }

    private void drawTimer(Graphics2D g2, long remainingMs) {
        long totalSec = (remainingMs + 999) / 1000;
        long mm = totalSec / 60;
        long ss = totalSec % 60;
        String text = String.format("%02d:%02d", mm, ss);

        g2.setFont(new Font("Arial", Font.BOLD, 56));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(text);
        int x = (GamePanel.SCREEN_WIDTH - w) / 2;
        int y = 80;

        g2.setColor(Color.BLACK);
        g2.drawString(text, x + 3, y + 3);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }

    private void drawCenteredBanner(Graphics2D g2, String text, Color color) {
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(text);
        int x = (GamePanel.SCREEN_WIDTH - w) / 2;
        int y = 150;
        g2.setColor(Color.BLACK);
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(color);
        g2.drawString(text, x, y);
    }
}
