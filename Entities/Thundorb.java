package Entities;

import java.awt.Color;
import java.awt.image.BufferedImage;
import ImageManager.SpriteSheetExtractor;
import ImageManager.ImageManager;
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
    private static final int LOSE_CHARGE_TICKS = 18;     // how long the lose-charge anim plays

    private int charges;
    private int reloadTimer;
    private int shootTimer;
    private boolean retreating;
    private int floatTick;
    private int baseY;

    // Animation states
    private enum AnimState { POWERED, LOSING_CHARGE, RETREATING }
    private AnimState animState;
    private int loseChargeTimer;

    // Sprite sets (56x56 cells)
    private BufferedImage[] poweredRightFrames;     // row 0, cols 0-1
    private BufferedImage[] poweredLeftFrames;       // row 0, cols 2-3
    private BufferedImage[] loseChargeRightFrames;   // row 1, 3 frames
    private BufferedImage[] loseChargeLeftFrames;    // row 2, 3 frames
    private BufferedImage[] retreatFrames;           // row 3, cols 0-1
    private BufferedImage projectileSprite;          // row 3, col 3

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
        this.animState = AnimState.POWERED;
        this.loseChargeTimer = 0;
        loadSprites();
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("Spritesheets/Thundorb.png");
            if (sheet != null) {
                int fw = 56;
                int fh = 56;
                // Row 0: powered up — cols 0-1 right, cols 2-3 left
                poweredRightFrames = new BufferedImage[] {
                    ext.extractSprite(sheet, 0, 0, fw, fh),
                    ext.extractSprite(sheet, fw, 0, fw, fh)
                };
                poweredLeftFrames = new BufferedImage[] {
                    ext.extractSprite(sheet, 2 * fw, 0, fw, fh),
                    ext.extractSprite(sheet, 3 * fw, 0, fw, fh)
                };
                // Row 1: lose charge facing right (3 frames)
                loseChargeRightFrames = ext.extractRow(sheet, 1, 3, fw, fh);
                // Row 2: lose charge facing left (3 frames)
                loseChargeLeftFrames = ext.extractRow(sheet, 2, 3, fw, fh);
                // Row 3: retreat (cols 0-1) + projectile (col 3)
                retreatFrames = new BufferedImage[] {
                    ext.extractSprite(sheet, 0, 3 * fh, fw, fh),
                    ext.extractSprite(sheet, fw, 3 * fh, fw, fh)
                };
                projectileSprite = ext.extractSprite(sheet, 3 * fw, 3 * fh, fw, fh);

                animFrames = poweredRightFrames;
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

        // Losing-charge animation countdown
        if (animState == AnimState.LOSING_CHARGE) {
            loseChargeTimer--;
            if (loseChargeTimer <= 0) {
                if (charges <= 0) {
                    animState = AnimState.RETREATING;
                    retreating = true;
                    reloadTimer = RELOAD_TICKS;
                } else {
                    animState = AnimState.POWERED;
                }
            }
            animate();
            updateAnimFrames();
            return; // pause actions during lose-charge animation
        }

        // Proximity stun — costs a charge
        if (distanceToPlayer() <= STUN_RANGE && !player.isStunned()) {
            player.stun(STUN_DURATION_MS);
            spendCharge();
        }

        if (retreating) {
            // Move away from player
            int dir = player.getX() > x ? -1 : 1;
            x += dir * RETREAT_SPEED;
            reloadTimer--;
            if (reloadTimer <= 0) {
                charges = MAX_CHARGES;
                retreating = false;
                animState = AnimState.POWERED;
            }
        } else if (charges > 0 && playerInRange(SHOOT_RANGE)) {
            if (shootTimer <= 0) {
                shootAtPlayer();
                spendCharge();
                shootTimer = SHOOT_INTERVAL_TICKS;
            }
        }

        if (shootTimer > 0) shootTimer--;

        facePlayer();
        updateAnimFrames();
        animate();
    }

    /** Spend one charge and trigger the lose-charge animation. */
    private void spendCharge() {
        charges--;
        animState = AnimState.LOSING_CHARGE;
        loseChargeTimer = LOSE_CHARGE_TICKS;
        currentFrame = 0;
        animTick = 0;
        updateAnimFrames();
    }

    /** Switch animFrames based on current state and facing direction. */
    private void updateAnimFrames() {
        BufferedImage[] target = null;
        switch (animState) {
            case POWERED:
                target = facingRight ? poweredRightFrames : poweredLeftFrames;
                break;
            case LOSING_CHARGE:
                target = facingRight ? loseChargeRightFrames : loseChargeLeftFrames;
                break;
            case RETREATING:
                target = retreatFrames;
                break;
        }
        if (target != null && target != animFrames) {
            animFrames = target;
            currentFrame = 0;
            animTick = 0;
        }
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
            Projectile.Type.ENEMY_ELECTRIC, ORB_DAMAGE, projectileSprite));
    }
}
