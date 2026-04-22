package Entities.Enemies;

import java.awt.Color;
import java.awt.image.BufferedImage;

import Entities.Player.Player;
import Entities.Projectiles.Projectile;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

/**
 * Compters - Hovering patrol enemy. Flies above the player and shoots
 * projectiles downward. Comes in two visual variants (STANDARD / ALT)
 * which use different colour palettes and matching projectile sprites.
 *
 * Sprite sheet layout (32x32 cells):
 *   Row 0: STANDARD compter, 2 flight frames (cols 0-1)
 *   Row 1: ALT compter,      2 flight frames (cols 0-1)
 *   Row 2: projectiles - col 0 -> ALT, col 1 -> STANDARD
 *
 * Worth 200 points.
 */
public class Compters extends Enemy {

    public enum Variant { STANDARD, ALT }

    private static final int POINTS = 200;
    private static final int HEALTH = 30;
    private static final int PATROL_SPEED = 2;
    private static final int SHOOT_RANGE = 250;
    private static final int SHOOT_COOLDOWN_TICKS = 40; // 2 sec at 20fps
    private static final int SRC_FRAME = 32;
    private static final int PROJECTILE_SIZE = 32;
    private static final int PROJECTILE_DAMAGE = 10;
    private static final int PROJECTILE_SPEED = 6;

    private final Variant variant;
    private int patrolLeftBound;
    private int patrolRightBound;
    private int shootCooldown;
    private boolean movingRight;

    private BufferedImage projectileSprite;

    public Compters(int x, int y, Player player, Level level) {
        this(x, y, player, level, Variant.STANDARD);
    }

    public Compters(int x, int y, Player player, Level level, Variant variant) {
        super(x, y, 84, 84, HEALTH, POINTS, player, level);
        this.variant = variant;
        this.patrolLeftBound = x - 192;
        this.patrolRightBound = x + 192;
        this.shootCooldown = 0;
        this.movingRight = true;
        this.fallbackColor = (variant == Variant.ALT) ? Color.MAGENTA : Color.ORANGE;
        this.animSpeed = 6;
        this.affectedByGravity = false; // hovers
        loadSprites();
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("Spritesheets/Compters.png");
            if (sheet == null) return;

            int row = (variant == Variant.ALT) ? 1 : 0;
            BufferedImage[] flight = new BufferedImage[2];
            flight[0] = ext.extractSprite(sheet, 0 * SRC_FRAME, row * SRC_FRAME, SRC_FRAME, SRC_FRAME);
            flight[1] = ext.extractSprite(sheet, 1 * SRC_FRAME, row * SRC_FRAME, SRC_FRAME, SRC_FRAME);
            animFrames = flight;

            // Row 2: col 0 = ALT projectile, col 1 = STANDARD projectile
            int projCol = (variant == Variant.ALT) ? 0 : 1;
            projectileSprite = ext.extractSprite(sheet,
                projCol * SRC_FRAME, 2 * SRC_FRAME, SRC_FRAME, SRC_FRAME);
        } catch (Exception e) {
            System.out.println("Could not load Compters sprites: " + e.getMessage());
        }
    }

    public Variant getVariant() { return variant; }

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
            int projX = x + width / 2 - PROJECTILE_SIZE / 2;
            int projY = y + height;
            Projectile p = new Projectile(
                projX, projY,
                0, PROJECTILE_SPEED,
                Projectile.Type.ENEMY_NORMAL,
                PROJECTILE_DAMAGE,
                projectileSprite);
            p.width  = PROJECTILE_SIZE;
            p.height = PROJECTILE_SIZE;
            projectiles.add(p);
            shootCooldown = SHOOT_COOLDOWN_TICKS;
        }

        animate();
    }
}
