package Entities;

import java.awt.Color;
import java.awt.image.BufferedImage;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

/**
 * Compters - Hovering patrol enemy. Flies above the player and shoots
 * projectiles downward. Worth 200 points.
 */
public class Compters extends Enemy {

    private static final int POINTS = 200;
    private static final int HEALTH = 30;
    private static final int PATROL_SPEED = 2;
    private static final int SHOOT_RANGE = 150;
    private static final int SHOOT_COOLDOWN_TICKS = 40; // 2 sec at 20fps

    private int patrolLeftBound;
    private int patrolRightBound;
    private int shootCooldown;
    private boolean movingRight;

    public Compters(int x, int y, Player player, Level level) {
        super(x, y, 84, 84, HEALTH, POINTS, player, level);
        this.patrolLeftBound = x - 192;
        this.patrolRightBound = x + 192;
        this.shootCooldown = 0;
        this.movingRight = true;
        this.fallbackColor = Color.ORANGE;
        this.animSpeed = 6;
        this.affectedByGravity = false; // hovers
        loadSprites();
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("SpriteSheets/Compters.png");
            if (sheet != null) {
                // Compters.png is 640x640: 4 cols x 4 rows of 160x160 cells
                int fw = 160;
                int fh = 160;
                BufferedImage[] row0 = ext.extractRow(sheet, 0, 4, fw, fh);
                BufferedImage[] row1 = ext.extractRow(sheet, 1, 4, fw, fh);
                animFrames = new BufferedImage[8];
                System.arraycopy(row0, 0, animFrames, 0, 4);
                System.arraycopy(row1, 0, animFrames, 4, 4);
            }
        } catch (Exception e) {
            System.out.println("Could not load Compters sprites: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (!alive) return;

        // Horizontal patrol
        if (movingRight) {
            x += PATROL_SPEED;
            if (x >= patrolRightBound) movingRight = false;
        } else {
            x -= PATROL_SPEED;
            if (x <= patrolLeftBound) movingRight = true;
        }
        facingRight = movingRight;

        // Shoot downward at player when in range
        if (shootCooldown > 0) {
            shootCooldown--;
        } else if (playerInRange(SHOOT_RANGE) && player.getY() > y) {
            projectiles.add(new Projectile(
                x + width / 2 - 12, y + height,
                0, 5, Projectile.Type.ENEMY_NORMAL, 10));
            shootCooldown = SHOOT_COOLDOWN_TICKS;
        }

        animate();
    }
}
