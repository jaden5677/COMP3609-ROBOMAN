package Entities;

import java.awt.Color;
import java.awt.image.BufferedImage;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

/**
 * Thundorb - Floating orb that shoots up to 4 electric orbs, then retreats
 * to reload. Electric orbs stun the player for 2 seconds. If the player gets
 * too close, the player is stunned and Thundorb loses a charge. Worth 350 pts.
 */
public class Thundorb extends Enemy {

    private static final int POINTS = 350;
    private static final int HEALTH = 40;
    private static final int MAX_CHARGES = 4;
    private static final int RELOAD_TICKS = 60;          // 3 sec
    private static final int SHOOT_INTERVAL_TICKS = 15;  // 0.75 sec
    private static final int STUN_RANGE = 40;
    private static final int STUN_DURATION_MS = 2000;
    private static final int SHOOT_RANGE = 200;
    private static final int RETREAT_SPEED = 3;
    private static final int ORB_DAMAGE = 5;

    private int charges;
    private int reloadTimer;
    private int shootTimer;
    private boolean retreating;
    private int floatTick;
    private int baseY;

    public Thundorb(int x, int y, Player player, Level level) {
        super(x, y, 72, 72, HEALTH, POINTS, player, level);
        this.charges = MAX_CHARGES;
        this.reloadTimer = 0;
        this.shootTimer = 0;
        this.retreating = false;
        this.floatTick = 0;
        this.baseY = y;
        this.fallbackColor = Color.CYAN;
        this.animSpeed = 8;
        this.affectedByGravity = false; // floats
        loadSprites();
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("SpriteSheets/Thundorb.png");
            if (sheet != null) {
                // Thundorb.png is 512x512: 4 cols x 4 rows (128x128)
                int fw = 128;
                int fh = 128;
                animFrames = ext.extractRow(sheet, 0, 4, fw, fh);
            }
        } catch (Exception e) {
            System.out.println("Could not load Thundorb sprites: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (!alive) return;

        // Float up and down
        floatTick++;
        y = baseY + (int)(Math.sin(floatTick * 0.1) * 4);

        // Proximity stun - costs a charge
        if (distanceToPlayer() <= STUN_RANGE && !player.isStunned()) {
            player.stun(STUN_DURATION_MS);
            charges--;
            if (charges <= 0) {
                retreating = true;
                reloadTimer = RELOAD_TICKS;
            }
        }

        if (retreating) {
            // Move away from player
            int dir = player.getX() > x ? -1 : 1;
            x += dir * RETREAT_SPEED;
            reloadTimer--;
            if (reloadTimer <= 0) {
                charges = MAX_CHARGES;
                retreating = false;
            }
        } else if (charges > 0 && playerInRange(SHOOT_RANGE)) {
            if (shootTimer <= 0) {
                shootAtPlayer();
                charges--;
                shootTimer = SHOOT_INTERVAL_TICKS;
                if (charges <= 0) {
                    retreating = true;
                    reloadTimer = RELOAD_TICKS;
                }
            }
        }

        if (shootTimer > 0) shootTimer--;

        facePlayer();
        animate();
    }

    private void shootAtPlayer() {
        int px = player.getX() + player.getWidth() / 2;
        int py = player.getY() + player.getHeight() / 2;
        int ox = x + width / 2;
        int oy = y + height / 2;
        double angle = Math.atan2(py - oy, px - ox);
        int speed = 5;
        int pdx = (int)(Math.cos(angle) * speed);
        int pdy = (int)(Math.sin(angle) * speed);
        projectiles.add(new Projectile(ox - 12, oy - 12, pdx, pdy,
            Projectile.Type.ENEMY_ELECTRIC, ORB_DAMAGE));
    }
}
