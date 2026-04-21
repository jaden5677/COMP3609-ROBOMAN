package Entities.Player;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import Entities.Projectiles.Projectile;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

public class Player extends AbstractPlayer {

    // Physics
    private int speed = 5;
    private int jumpStrength = -20;
    private static final int GRAVITY = 1;
    private static final int MAX_FALL_SPEED = 10;

    private boolean onGround;

    // Shooting
    private long lastShotTime;
    private static final long SHOT_COOLDOWN = 500;
    private int damageBonus = 0;
    private GunType gunType = GunType.NORMAL;
    private long chargeStartTime = -1;

    // Stun
    private boolean stunned;
    private long stunEndTime;

    // Spawn
    private int spawnX;
    private int spawnY;

    // Input state (set by GamePanel key listener)
    private boolean moveLeft;
    private boolean moveRight;
    private boolean jumpPressed;
    private boolean shootPressed;

    // Level reference for collision
    private Level level;

    // Animation sets (all extracted from RoboManFull.png — 6 cols x 6 rows of 40x36)
    // Row 0: facing right        — col 0 idle, cols 1-3 run, cols 4-5 jump/fall
    // Row 1: facing left         — col 3 idle, cols 2-0 run, cols 4-5 jump/fall
    // Row 2: facing left shoot   — col 3 stationary shoot, cols 2-0 run-shoot
    // Row 3: facing right shoot  — col 3 stationary shoot, cols 0-2 run-shoot
    // Row 4: bombs               — cols 0-1 throw right, cols 2-3 throw left, cols 4-5 bomb left/right
    // Row 5: projectiles         — col 0 normal bullet, cols 1 & 2 triple-shot bullets
    private BufferedImage[] idleRightFrames;
    private BufferedImage[] idleLeftFrames;
    private BufferedImage[] runRightFrames;
    private BufferedImage[] runLeftFrames;
    private BufferedImage[] jumpRightFrames;
    private BufferedImage[] jumpLeftFrames;
    private BufferedImage[] shootIdleRightFrames;
    private BufferedImage[] shootIdleLeftFrames;
    private BufferedImage[] shootRunRightFrames;
    private BufferedImage[] shootRunLeftFrames;
    private BufferedImage[] throwRightFrames;
    private BufferedImage[] throwLeftFrames;
    private BufferedImage  bombRightSprite;
    private BufferedImage  bombLeftSprite;
    private BufferedImage  bulletSprite;
    private BufferedImage  tripleBulletUpSprite;
    private BufferedImage  tripleBulletDownSprite;
    private String currentAnim;

    // Projectiles created by shooting
    private List<Projectile> projectiles;

    public Player(int x, int y, Level level) {
        super(x, y, 84, 90);
        this.spawnX = x;
        this.spawnY = y;
        this.level = level;
        this.maxHealth = 100;
        this.health = maxHealth;
        this.lives = 3;
        this.deathsInLevel = 0;
        this.currentLevel = 1;
        this.onGround = false;
        this.stunned = false;
        this.facingRight = true;
        this.projectiles = new ArrayList<>();
        this.currentAnim = "idle";
        this.animSpeed = 5;
        loadSprites();
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("Spritesheets/RoboManFull.png");
            if (sheet == null) return;

            final int fw = 40;
            final int fh = 36;

            // ---- Row 0: facing right (idle / run / jump) ----
            BufferedImage row0c0 = ext.extractSprite(sheet, 0 * fw, 0 * fh, fw, fh);
            BufferedImage row0c1 = ext.extractSprite(sheet, 1 * fw, 0 * fh, fw, fh);
            BufferedImage row0c2 = ext.extractSprite(sheet, 2 * fw, 0 * fh, fw, fh);
            BufferedImage row0c3 = ext.extractSprite(sheet, 3 * fw, 0 * fh, fw, fh);
            BufferedImage row0c4 = ext.extractSprite(sheet, 4 * fw, 0 * fh, fw, fh);
            BufferedImage row0c5 = ext.extractSprite(sheet, 5 * fw, 0 * fh, fw, fh);
            idleRightFrames = new BufferedImage[]{ row0c0 };
            runRightFrames  = new BufferedImage[]{ row0c1, row0c2, row0c3 };
            jumpRightFrames = new BufferedImage[]{ row0c4, row0c5 };

            // ---- Row 1: facing left (idle col 3, run cols 2->0, jump cols 4-5) ----
            BufferedImage row1c0 = ext.extractSprite(sheet, 0 * fw, 1 * fh, fw, fh);
            BufferedImage row1c1 = ext.extractSprite(sheet, 1 * fw, 1 * fh, fw, fh);
            BufferedImage row1c2 = ext.extractSprite(sheet, 2 * fw, 1 * fh, fw, fh);
            BufferedImage row1c3 = ext.extractSprite(sheet, 3 * fw, 1 * fh, fw, fh);
            BufferedImage row1c4 = ext.extractSprite(sheet, 4 * fw, 1 * fh, fw, fh);
            BufferedImage row1c5 = ext.extractSprite(sheet, 5 * fw, 1 * fh, fw, fh);
            idleLeftFrames = new BufferedImage[]{ row1c3 };
            runLeftFrames  = new BufferedImage[]{ row1c2, row1c1, row1c0 };
            jumpLeftFrames = new BufferedImage[]{ row1c4, row1c5 };

            // ---- Row 2: facing left + shooting (stationary col 3, running cols 2->0) ----
            BufferedImage row2c0 = ext.extractSprite(sheet, 0 * fw, 2 * fh, fw, fh);
            BufferedImage row2c1 = ext.extractSprite(sheet, 1 * fw, 2 * fh, fw, fh);
            BufferedImage row2c2 = ext.extractSprite(sheet, 2 * fw, 2 * fh, fw, fh);
            BufferedImage row2c3 = ext.extractSprite(sheet, 3 * fw, 2 * fh, fw, fh);
            shootIdleLeftFrames = new BufferedImage[]{ row2c3 };
            shootRunLeftFrames  = new BufferedImage[]{ row2c2, row2c1, row2c0 };

            // ---- Row 3: facing right + shooting (running cols 0-2, stationary col 3) ----
            BufferedImage row3c0 = ext.extractSprite(sheet, 0 * fw, 3 * fh, fw, fh);
            BufferedImage row3c1 = ext.extractSprite(sheet, 1 * fw, 3 * fh, fw, fh);
            BufferedImage row3c2 = ext.extractSprite(sheet, 2 * fw, 3 * fh, fw, fh);
            BufferedImage row3c3 = ext.extractSprite(sheet, 3 * fw, 3 * fh, fw, fh);
            shootIdleRightFrames = new BufferedImage[]{ row3c3 };
            shootRunRightFrames  = new BufferedImage[]{ row3c0, row3c1, row3c2 };

            // ---- Row 4: bombs ----
            BufferedImage row4c0 = ext.extractSprite(sheet, 0 * fw, 4 * fh, fw, fh);
            BufferedImage row4c1 = ext.extractSprite(sheet, 1 * fw, 4 * fh, fw, fh);
            BufferedImage row4c2 = ext.extractSprite(sheet, 2 * fw, 4 * fh, fw, fh);
            BufferedImage row4c3 = ext.extractSprite(sheet, 3 * fw, 4 * fh, fw, fh);
            BufferedImage row4c4 = ext.extractSprite(sheet, 4 * fw, 4 * fh, fw, fh);
            BufferedImage row4c5 = ext.extractSprite(sheet, 5 * fw, 4 * fh, fw, fh);
            throwRightFrames = new BufferedImage[]{ row4c0, row4c1 };
            throwLeftFrames  = new BufferedImage[]{ row4c2, row4c3 };
            bombLeftSprite   = row4c4;
            bombRightSprite  = row4c5;

            // ---- Row 5: projectile sprites ----
            bulletSprite           = ext.extractSprite(sheet, 0 * fw, 5 * fh, fw, fh);
            tripleBulletUpSprite   = ext.extractSprite(sheet, 1 * fw, 5 * fh, fw, fh);
            tripleBulletDownSprite = ext.extractSprite(sheet, 2 * fw, 5 * fh, fw, fh);

            animFrames = idleRightFrames;
        } catch (Exception e) {
            System.out.println("Could not load player sprites: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (stunned) {
            if (System.currentTimeMillis() >= stunEndTime) {
                stunned = false;
            }
            applyGravity();
            applyVerticalCollision();
            return;
        }

        // Horizontal movement
        dx = 0;
        if (moveLeft)  { dx = -speed; facingRight = false; }
        if (moveRight) { dx =  speed; facingRight = true;  }

        // Jump
        if (jumpPressed && onGround) {
            dy = jumpStrength;
            onGround = false;
        }

        applyGravity();
        moveWithCollision();

        if (shootPressed) {
            shoot();
        }

        updateAnimation();
        animate();
    }

    private void applyGravity() {
        dy += GRAVITY;
        if (dy > MAX_FALL_SPEED) dy = MAX_FALL_SPEED;
    }

    private void moveWithCollision() {
        // Horizontal
        int newX = x + dx;
        if (!collidesWithLevel(newX, y)) {
            x = newX;
        }
        applyVerticalCollision();

        // Fallen off map
        if (y > level.getMapHeight() * Level.TILE_SIZE + 300) {
            die();
        }
    }

    private void applyVerticalCollision() {
        int newY = y + dy;
        if (dy > 0) {
            if (collidesWithLevel(x, newY)) {
                y = ((newY + height) / Level.TILE_SIZE) * Level.TILE_SIZE - height;
                dy = 0;
                onGround = true;
            } else {
                y = newY;
                onGround = false;
            }
        } else if (dy < 0) {
            if (collidesWithLevel(x, newY)) {
                y = (y / Level.TILE_SIZE + 1) * Level.TILE_SIZE;
                dy = 0;
            } else {
                y = newY;
            }
        }
    }

    private boolean collidesWithLevel(int testX, int testY) {
        int left = testX;
        int right = testX + width - 1;
        int top = testY;
        int bottom = testY + height - 1;
        return level.isSolid(left, top) || level.isSolid(right, top) ||
               level.isSolid(left, bottom) || level.isSolid(right, bottom);
    }

    private void shoot() {
        long now = System.currentTimeMillis();
        if (now - lastShotTime < SHOT_COOLDOWN) return;

        int projX  = facingRight ? x + width : x - 24;
        int projY  = y + height / 2 - 12;
        int projDx = facingRight ? 8 : -8;
        int dmg    = 10 + damageBonus;

        switch (gunType) {
            case TRIPLE_SHOT:
                projectiles.add(new Projectile(projX, projY,      projDx,  0,
                    Projectile.Type.PLAYER_LIGHT, dmg, bulletSprite));
                projectiles.add(new Projectile(projX, projY - 14, projDx, -2,
                    Projectile.Type.PLAYER_LIGHT, dmg, tripleBulletUpSprite));
                projectiles.add(new Projectile(projX, projY + 14, projDx,  2,
                    Projectile.Type.PLAYER_LIGHT, dmg, tripleBulletDownSprite));
                break;
            case CHARGE_SHOT:
                // Hold-to-charge: longer hold => heavier shot.
                long held = chargeStartTime > 0 ? now - chargeStartTime : 0;
                if (held >= 600) {
                    projectiles.add(new Projectile(projX, projY, projDx * 2, 0,
                        Projectile.Type.PLAYER_HEAVY, dmg * 3, bulletSprite));
                } else {
                    projectiles.add(new Projectile(projX, projY, projDx, 0,
                        Projectile.Type.PLAYER_LIGHT, dmg, bulletSprite));
                }
                chargeStartTime = -1;
                break;
            case NORMAL:
            default:
                projectiles.add(new Projectile(projX, projY, projDx, 0,
                    Projectile.Type.PLAYER_LIGHT, dmg, bulletSprite));
                break;
        }
        lastShotTime = now;
    }

    private void updateAnimation() {
        BufferedImage[] newFrames;
        String newAnim;

        boolean shooting = shootPressed;
        boolean moving   = dx != 0;

        if (!onGround) {
            // Jumping / falling — use jump frames; if also shooting, fall back to
            // the shoot-run frames so the gun is visible mid-air.
            if (shooting) {
                newFrames = facingRight ? shootRunRightFrames : shootRunLeftFrames;
                newAnim   = facingRight ? "shoot-jump-right"  : "shoot-jump-left";
            } else {
                newFrames = facingRight ? jumpRightFrames : jumpLeftFrames;
                newAnim   = facingRight ? "jump-right"   : "jump-left";
            }
        } else if (moving) {
            if (shooting) {
                newFrames = facingRight ? shootRunRightFrames : shootRunLeftFrames;
                newAnim   = facingRight ? "shoot-run-right"   : "shoot-run-left";
            } else {
                newFrames = facingRight ? runRightFrames : runLeftFrames;
                newAnim   = facingRight ? "run-right"   : "run-left";
            }
        } else {
            if (shooting) {
                newFrames = facingRight ? shootIdleRightFrames : shootIdleLeftFrames;
                newAnim   = facingRight ? "shoot-idle-right"   : "shoot-idle-left";
            } else {
                newFrames = facingRight ? idleRightFrames : idleLeftFrames;
                newAnim   = facingRight ? "idle-right"   : "idle-left";
            }
        }

        if (newFrames != null && !newAnim.equals(currentAnim)) {
            currentAnim = newAnim;
            animFrames = newFrames;
            currentFrame = 0;
            animTick = 0;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!isVisible) return;
        BufferedImage frame = getCurrentFrame();
        if (frame != null) {
            // Sprite sheet already contains separate left/right frames, so just
            // draw the current frame as-is (no mirroring).
            g2.drawImage(frame, x - 6, y - 3, width + 12, height + 6, null);
        } else {
            g2.setColor(stunned ? Color.YELLOW : Color.RED);
            g2.fillRect(x, y, width, height);
        }
    }

    // --- Stun ---
    public void stun(int durationMs) {
        stunned = true;
        stunEndTime = System.currentTimeMillis() + durationMs;
    }
    public boolean isStunned() { return stunned; }

    // --- Damage / death ---
    public void takeDamage(int dmg) {
        health -= dmg;
        if (health <= 0) {
            die();
        }
    }

    private void die() {
        deathsInLevel++;
        lives--;
        if (lives <= 0) {
            lives = 3;
            deathsInLevel = 0;
            currentLevel = 1;
        } else if (currentLevel > 1 && deathsInLevel >= 3) {
            currentLevel = 1;
            deathsInLevel = 0;
        }
        respawn();
    }

    public void respawn() {
        x = spawnX;
        y = spawnY;
        health = maxHealth;
        dy = 0;
        dx = 0;
        stunned = false;
        onGround = false;
    }
    @Override
    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }

    // --- Item-driven boosts ---
    @Override public void boostDamage(int amount) { damageBonus += amount; }
    @Override public void boostJump(int amount)   { jumpStrength -= Math.abs(amount); }
    @Override public void boostSpeed(int amount)  { speed += amount; }
    @Override public void setGunType(GunType gunType) {
        this.gunType = gunType == null ? GunType.NORMAL : gunType;
        this.chargeStartTime = -1;
    }
    public GunType getGunType() { return gunType; }

    // --- Input setters ---
    public void setMoveLeft(boolean b)    { moveLeft = b; }
    public void setMoveRight(boolean b)   { moveRight = b; }
    public void setJumpPressed(boolean b) { jumpPressed = b; }
    public void setShootPressed(boolean b){
        if (b && !shootPressed && gunType == GunType.CHARGE_SHOT) {
            chargeStartTime = System.currentTimeMillis();
        }
        shootPressed = b;
    }

    // --- Getters ---
    public boolean isOnGround()   { return onGround; }
    @Override public List<Projectile> getProjectiles() { return projectiles; }
    public void setLevel(Level level) { this.level = level; }
}
