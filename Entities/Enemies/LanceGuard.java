package Entities.Enemies;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Entities.Player.Player;
import Entities.Projectiles.Projectile;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

/**
 * LanceGuard - Stationary guard enemy.
 *
 * Behaviour:
 *   - IDLE     : neutral pose when the player is far away.
 *   - DEFENSE  : raised-shield pose when the player is in detection range.
 *   - ATTACK   : swing pose. Triggered when (a) the player is in melee
 *                range, or (b) a player projectile hits the guard while
 *                the swipe cooldown is ready (deflect). Plays for ~0.5s.
 *
 * Every swipe (attack OR deflect) spawns a shockwave projectile in the
 * direction the guard is facing, and triggers a 3-second cooldown before
 * the next swipe is allowed.
 *
 * Worth 300 points.
 */
public class LanceGuard extends Enemy {

    private static final int POINTS                 = 300;
    private static final int HEALTH                 = 60;
    private static final int DEFENSE_RANGE          = 220;
    private static final int ATTACK_RANGE           = 110;
    private static final int ATTACK_DAMAGE          = 20;
    private static final int ATTACK_ANIM_TICKS      = 10;     // ~0.5s @ 20fps
    private static final long SWIPE_COOLDOWN_MS     = 3000;
    private static final int SHOCKWAVE_DAMAGE       = 10;
    private static final int SHOCKWAVE_SPEED        = 8;
    private static final int SHOCKWAVE_SIZE         = 64;

    // Animation states
    private enum AnimState { IDLE, DEFENSE, ATTACK }
    private AnimState animState;
    private int  attackAnimTimer;
    private long lastSwipeTime;
    private boolean meleeHitDelivered; // ensure each swing only damages once

    // Sprite cells (separate left/right so we never need to mirror)
    private BufferedImage idleLeft,    defenseLeft,    attackLeft;
    private BufferedImage idleRight,   defenseRight,   attackRight;
    private BufferedImage shockwaveRight, shockwaveLeft;

    public LanceGuard(int x, int y, Player player, Level level) {
        super(x, y, 96, 96, HEALTH, POINTS, player, level);
        this.fallbackColor     = Color.BLUE;
        this.animSpeed         = 6;
        this.affectedByGravity = true;
        this.animState         = AnimState.IDLE;
        this.attackAnimTimer   = 0;
        this.lastSwipeTime     = -SWIPE_COOLDOWN_MS;
        this.meleeHitDelivered = false;
        loadSprites();
        applyStateFrames();
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("Spritesheets/LanceGaurd.png");
            if (sheet == null) return;
            // Sheet is a 3x3 grid; cell size derived from the actual image
            // (256x256 sheet => 85x85 cells, even though the artwork is 86x86).
            int cellW = sheet.getWidth()  / 3;
            int cellH = sheet.getHeight() / 3;

            // Row 0: facing LEFT  (defense | idle | attack)
            defenseLeft = ext.extractSprite(sheet, 0 * cellW, 0 * cellH, cellW, cellH);
            idleLeft    = ext.extractSprite(sheet, 1 * cellW, 0 * cellH, cellW, cellH);
            attackLeft  = ext.extractSprite(sheet, 2 * cellW, 0 * cellH, cellW, cellH);

            // Row 1: facing RIGHT (attack | idle | defense)
            attackRight  = ext.extractSprite(sheet, 0 * cellW, 1 * cellH, cellW, cellH);
            idleRight    = ext.extractSprite(sheet, 1 * cellW, 1 * cellH, cellW, cellH);
            defenseRight = ext.extractSprite(sheet, 2 * cellW, 1 * cellH, cellW, cellH);

            // Row 2: shockwave projectiles (col 0 right-facing, col 1 left-facing)
            shockwaveRight = ext.extractSprite(sheet, 0 * cellW, 2 * cellH, cellW, cellH);
            shockwaveLeft  = ext.extractSprite(sheet, 1 * cellW, 2 * cellH, cellW, cellH);
        } catch (Exception e) {
            System.out.println("Could not load LanceGuard sprites: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  Combat
    // ------------------------------------------------------------------

    @Override
    public void takeDamage(int dmg, Projectile.Type attackType) {
        if (!alive) return;
        // Player projectiles can be deflected: if the swipe is off cooldown,
        // perform a swing (which spawns a shockwave back at the player) and
        // ignore the incoming damage. Otherwise take the hit normally.
        if (attackType == Projectile.Type.PLAYER_LIGHT ||
            attackType == Projectile.Type.PLAYER_HEAVY) {
            if (canSwipe()) {
                facePlayer();
                startSwipe();
                return; // deflected
            }
        }
        super.takeDamage(dmg, attackType);
    }

    private boolean canSwipe() {
        return System.currentTimeMillis() - lastSwipeTime >= SWIPE_COOLDOWN_MS;
    }

    /** Begin the attack/deflect animation and spawn a shockwave. */
    private void startSwipe() {
        animState         = AnimState.ATTACK;
        attackAnimTimer   = ATTACK_ANIM_TICKS;
        lastSwipeTime     = System.currentTimeMillis();
        meleeHitDelivered = false;
        spawnShockwave();
        applyStateFrames();
    }

    private void spawnShockwave() {
        int dir   = facingRight ? 1 : -1;
        int projX = facingRight ? x + width : x - SHOCKWAVE_SIZE;
        int projY = y + height / 2 - SHOCKWAVE_SIZE / 2;
        BufferedImage sprite = facingRight ? shockwaveRight : shockwaveLeft;
        Projectile shockwave = new Projectile(
            projX, projY,
            dir * SHOCKWAVE_SPEED, 0,
            Projectile.Type.ENEMY_SHOCKWAVE,
            SHOCKWAVE_DAMAGE,
            sprite);
        // Make the shockwave visually larger than the default 48px projectile.
        shockwave.width  = SHOCKWAVE_SIZE;
        shockwave.height = SHOCKWAVE_SIZE;
        projectiles.add(shockwave);
    }

    // ------------------------------------------------------------------
    //  Per-tick update
    // ------------------------------------------------------------------

    @Override
    public void update() {
        if (!alive) return;

        applyGravity();

        // Currently mid-swing? Tick down the animation, deal melee damage
        // once if the player is in range, then return to a resting pose.
        if (animState == AnimState.ATTACK) {
            attackAnimTimer--;
            facePlayer();
            if (!meleeHitDelivered && distanceToPlayer() <= ATTACK_RANGE) {
                player.takeDamage(ATTACK_DAMAGE);
                meleeHitDelivered = true;
            }
            if (attackAnimTimer <= 0) {
                animState = playerInRange(DEFENSE_RANGE) ? AnimState.DEFENSE : AnimState.IDLE;
                applyStateFrames();
            }
            return;
        }

        facePlayer();

        double dist = distanceToPlayer();
        AnimState desired = (dist <= DEFENSE_RANGE) ? AnimState.DEFENSE : AnimState.IDLE;
        if (desired != animState) {
            animState = desired;
            applyStateFrames();
        }

        // Try to swing if the player walked into melee range.
        if (dist <= ATTACK_RANGE && canSwipe()) {
            startSwipe();
        }
    }

    private void applyStateFrames() {
        BufferedImage frame;
        switch (animState) {
            case ATTACK:  frame = facingRight ? attackRight  : attackLeft;  break;
            case DEFENSE: frame = facingRight ? defenseRight : defenseLeft; break;
            case IDLE:
            default:      frame = facingRight ? idleRight    : idleLeft;    break;
        }
        if (frame != null) {
            animFrames   = new BufferedImage[]{ frame };
            currentFrame = 0;
            animTick     = 0;
        }
    }

    /**
     * Override draw so left/right sprites are used as-is (no mirroring).
     * The parent {@link Enemy#draw(Graphics2D)} mirrors based on
     * {@code facingRight}, which would double-flip our pre-oriented frames.
     */
    @Override
    public void draw(Graphics2D g2) {
        if (!isVisible || !alive) return;
        BufferedImage frame = getCurrentFrame();
        if (frame != null) {
            g2.drawImage(frame, x, y, width, height, null);
        } else {
            g2.setColor(fallbackColor);
            g2.fillRect(x, y, width, height);
        }
    }
}
