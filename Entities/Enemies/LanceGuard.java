package Entities.Enemies;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.Map;

import Entities.Player.Player;
import Entities.Projectiles.Projectile;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

public class LanceGuard extends Enemy {

    private static final int POINTS                 = 300;
    private static final int HEALTH                 = 60;
    private static final int DEFENSE_RANGE          = 220;
    private static final int ATTACK_RANGE           = 110;
    private static final int ATTACK_DAMAGE          = 20;
    private static final int ATTACK_ANIM_TICKS      = 10;
    private static final long SWIPE_COOLDOWN_MS     = 3000;
    private static final int SHOCKWAVE_DAMAGE       = 10;
    private static final int SHOCKWAVE_SPEED        = 8;
    private static final int SHOCKWAVE_SIZE         = 64;

    private enum AnimState { IDLE, DEFENSE, ATTACK }
    private AnimState animState;
    private int  attackAnimTimer;
    private long lastSwipeTime;
    private boolean meleeHitDelivered;

    private BufferedImage idleLeft,    defenseLeft,    attackLeft;
    private BufferedImage idleRight,   defenseRight,   attackRight;
    private BufferedImage shockwaveRight, shockwaveLeft;

    private int srcCellW = 1, srcCellH = 1;
    private final Map<BufferedImage, Rectangle> hitboxCache = new IdentityHashMap<>();

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

            int cellW = sheet.getWidth()  / 3;
            int cellH = sheet.getHeight() / 3;
            srcCellW = cellW;
            srcCellH = cellH;

            defenseLeft = ext.extractSprite(sheet, 0 * cellW, 0 * cellH, cellW, cellH);
            idleLeft    = ext.extractSprite(sheet, 1 * cellW, 0 * cellH, cellW, cellH);
            attackLeft  = ext.extractSprite(sheet, 2 * cellW, 0 * cellH, cellW, cellH);

            attackRight  = ext.extractSprite(sheet, 0 * cellW, 1 * cellH, cellW, cellH);
            idleRight    = ext.extractSprite(sheet, 1 * cellW, 1 * cellH, cellW, cellH);
            defenseRight = ext.extractSprite(sheet, 2 * cellW, 1 * cellH, cellW, cellH);

            shockwaveRight = ext.extractSprite(sheet, 0 * cellW, 2 * cellH, cellW, cellH);
            shockwaveLeft  = ext.extractSprite(sheet, 1 * cellW, 2 * cellH, cellW, cellH);
        } catch (Exception e) {
            System.out.println("Could not load LanceGuard sprites: " + e.getMessage());
        }
    }

    @Override
    public void takeDamage(int dmg, Projectile.Type attackType) {
        if (!alive) return;

        if (attackType == Projectile.Type.PLAYER_LIGHT ||
            attackType == Projectile.Type.PLAYER_HEAVY) {
            if (canSwipe()) {
                facePlayer();
                startSwipe();
                return;
            }
        }
        super.takeDamage(dmg, attackType);
    }

    private boolean canSwipe() {
        return System.currentTimeMillis() - lastSwipeTime >= SWIPE_COOLDOWN_MS;
    }

    private void startSwipe() {
        animState         = AnimState.ATTACK;
        attackAnimTimer   = ATTACK_ANIM_TICKS;
        lastSwipeTime     = System.currentTimeMillis();
        meleeHitDelivered = false;
        spawnShockwave();
        applyStateFrames();
    }

    private void spawnShockwave() {
        int dir;
        int projX;
        BufferedImage sprite;
        if (facingRight) {
            dir = 1;
            projX = x + width;
            sprite = shockwaveRight;
        } else {
            dir = -1;
            projX = x - SHOCKWAVE_SIZE;
            sprite = shockwaveLeft;
        }
        int projY = y + height / 2 - SHOCKWAVE_SIZE / 2;
        Projectile shockwave = new Projectile(
            projX, projY,
            dir * SHOCKWAVE_SPEED, 0,
            Projectile.Type.ENEMY_SHOCKWAVE,
            SHOCKWAVE_DAMAGE,
            sprite);

        shockwave.width  = SHOCKWAVE_SIZE;
        shockwave.height = SHOCKWAVE_SIZE;
        projectiles.add(shockwave);
    }

    @Override
    public void update() {
        if (!alive) return;

        applyGravity();

        if (animState == AnimState.ATTACK) {
            attackAnimTimer--;
            facePlayer();
            if (!meleeHitDelivered && distanceToPlayer() <= ATTACK_RANGE) {
                player.takeDamage(ATTACK_DAMAGE);
                meleeHitDelivered = true;
            }
            if (attackAnimTimer <= 0) {
                if (playerInRange(DEFENSE_RANGE)) {
                    animState = AnimState.DEFENSE;
                } else {
                    animState = AnimState.IDLE;
                }
                applyStateFrames();
            }
            return;
        }

        facePlayer();

        double dist = distanceToPlayer();
        AnimState desired;
        if (dist <= DEFENSE_RANGE) {
            desired = AnimState.DEFENSE;
        } else {
            desired = AnimState.IDLE;
        }
        if (desired != animState) {
            animState = desired;
            applyStateFrames();
        }

        if (dist <= ATTACK_RANGE && canSwipe()) {
            startSwipe();
        }
    }

    private void applyStateFrames() {
        BufferedImage frame;
        switch (animState) {
            case ATTACK:
                if (facingRight) {
                    frame = attackRight;
                } else {
                    frame = attackLeft;
                }
                break;
            case DEFENSE:
                if (facingRight) {
                    frame = defenseRight;
                } else {
                    frame = defenseLeft;
                }
                break;
            case IDLE:
            default:
                if (facingRight) {
                    frame = idleRight;
                } else {
                    frame = idleLeft;
                }
                break;
        }
        if (frame != null) {
            animFrames   = new BufferedImage[]{ frame };
            currentFrame = 0;
            animTick     = 0;
        }
    }

    @Override
    protected void drawSelf(Graphics2D g2) {
        if (!alive) return;
        BufferedImage frame = getCurrentFrame();
        if (frame != null) {
            g2.drawImage(frame, x, y, width, height, null);
        } else {
            g2.setColor(fallbackColor);
            g2.fillRect(x, y, width, height);
        }
    }

    @Override
    public Rectangle2D.Double getBoundingRectangle() {
        BufferedImage frame = getCurrentFrame();
        if (frame == null || srcCellW <= 0 || srcCellH <= 0) return super.getBoundingRectangle();
        Rectangle b = hitboxCache.get(frame);
        if (b == null) {
            b = opaqueBounds(frame);
            hitboxCache.put(frame, b);
        }
        double sx = (double) width  / srcCellW;
        double sy = (double) height / srcCellH;
        return new Rectangle2D.Double(
            x + b.x * sx,
            y + b.y * sy,
            b.width  * sx,
            b.height * sy);
    }
}
