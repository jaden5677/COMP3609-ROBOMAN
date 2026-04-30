package Entities.Enemies;

import java.awt.Color;
import java.awt.image.BufferedImage;

import Entities.Player.Player;
import Entities.Projectiles.Projectile;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

public class Compters extends Enemy {

    public enum Variant { STANDARD, ALT }

    private static final int POINTS = 200;
    private static final int HEALTH = 30;
    private static final int PATROL_SPEED = 2;
    private static final int SHOOT_RANGE = 250;
    private static final int SHOOT_COOLDOWN_TICKS = 40;
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
        if (variant == Variant.ALT) {
            this.fallbackColor = Color.MAGENTA;
        } else {
            this.fallbackColor = Color.ORANGE;
        }
        this.animSpeed = 6;
        this.affectedByGravity = false;
        loadSprites();
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("Spritesheets/Compters.png");
            if (sheet == null) return;

            int row;
            if (variant == Variant.ALT) {
                row = 1;
            } else {
                row = 0;
            }
            BufferedImage[] flight = new BufferedImage[2];
            flight[0] = ext.extractSprite(sheet, 0 * SRC_FRAME, row * SRC_FRAME, SRC_FRAME, SRC_FRAME);
            flight[1] = ext.extractSprite(sheet, 1 * SRC_FRAME, row * SRC_FRAME, SRC_FRAME, SRC_FRAME);
            animFrames = flight;

            int projCol;
            if (variant == Variant.ALT) {
                projCol = 0;
            } else {
                projCol = 1;
            }
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

        if (movingRight) {
            int newX = x + PATROL_SPEED;
            if (newX >= patrolRightBound || hitsSolid(newX, true)) {
                movingRight = false;
            } else {
                x = newX;
            }
        } else {
            int newX = x - PATROL_SPEED;
            if (newX <= patrolLeftBound || hitsSolid(newX, false)) {
                movingRight = true;
            } else {
                x = newX;
            }
        }
        facingRight = movingRight;

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

    private boolean hitsSolid(int newX, boolean movingRight) {
        int probeX;
        if (movingRight) {
            probeX = newX + width - 1;
        } else {
            probeX = newX;
        }
        return level.isSolid(probeX, y + 4)
            || level.isSolid(probeX, y + height / 2)
            || level.isSolid(probeX, y + height - 4);
    }
}
