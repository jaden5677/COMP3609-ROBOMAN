package Entities.Player;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import Entities.Projectiles.Projectile;
import ImageManager.SpriteSheetExtractor;
import ImageManager.WhiteFlashFX;
import MainGame.Level;

public class Player extends AbstractPlayer {

private int speed = 9;
    private int jumpStrength = -28;
    private static final int GRAVITY = 2;
    private static final int MAX_FALL_SPEED = 18;

    private boolean onGround;

    private long lastShotTime;
    private static final long SHOT_COOLDOWN = 500;
    private int damageBonus = 0;
    private GunType gunType = GunType.NORMAL;
    private long chargeStartTime = -1;

    private boolean stunned;
    private long stunEndTime;

    private int spawnX;
    private int spawnY;

    private boolean moveLeft;
    private boolean moveRight;
    private boolean jumpPressed;
    private boolean shootPressed;

    private Level level;

    private final WhiteFlashFX hitFlash = new WhiteFlashFX();

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

    private static final int SRC_FW = 40;
    private static final int SRC_FH = 36;

    private final Map<BufferedImage, Rectangle> hitboxCache = new IdentityHashMap<>();

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

            BufferedImage row0c0 = ext.extractSprite(sheet, 0 * fw, 0 * fh, fw, fh);
            BufferedImage row0c1 = ext.extractSprite(sheet, 1 * fw, 0 * fh, fw, fh);
            BufferedImage row0c2 = ext.extractSprite(sheet, 2 * fw, 0 * fh, fw, fh);
            BufferedImage row0c3 = ext.extractSprite(sheet, 3 * fw, 0 * fh, fw, fh);
            BufferedImage row0c4 = ext.extractSprite(sheet, 4 * fw, 0 * fh, fw, fh);
            BufferedImage row0c5 = ext.extractSprite(sheet, 5 * fw, 0 * fh, fw, fh);
            idleRightFrames = new BufferedImage[]{ row0c0 };
            runRightFrames  = new BufferedImage[]{ row0c1, row0c2, row0c3 };
            jumpRightFrames = new BufferedImage[]{ row0c4, row0c5 };

            BufferedImage row1c0 = ext.extractSprite(sheet, 0 * fw, 1 * fh, fw, fh);
            BufferedImage row1c1 = ext.extractSprite(sheet, 1 * fw, 1 * fh, fw, fh);
            BufferedImage row1c2 = ext.extractSprite(sheet, 2 * fw, 1 * fh, fw, fh);
            BufferedImage row1c3 = ext.extractSprite(sheet, 3 * fw, 1 * fh, fw, fh);
            BufferedImage row1c4 = ext.extractSprite(sheet, 4 * fw, 1 * fh, fw, fh);
            BufferedImage row1c5 = ext.extractSprite(sheet, 5 * fw, 1 * fh, fw, fh);
            idleLeftFrames = new BufferedImage[]{ row1c3 };
            runLeftFrames  = new BufferedImage[]{ row1c2, row1c1, row1c0 };
            jumpLeftFrames = new BufferedImage[]{ row1c4, row1c5 };

            BufferedImage row2c0 = ext.extractSprite(sheet, 0 * fw, 2 * fh, fw, fh);
            BufferedImage row2c1 = ext.extractSprite(sheet, 1 * fw, 2 * fh, fw, fh);
            BufferedImage row2c2 = ext.extractSprite(sheet, 2 * fw, 2 * fh, fw, fh);
            BufferedImage row2c3 = ext.extractSprite(sheet, 3 * fw, 2 * fh, fw, fh);
            shootIdleLeftFrames = new BufferedImage[]{ row2c3 };
            shootRunLeftFrames  = new BufferedImage[]{ row2c2, row2c1, row2c0 };

            BufferedImage row3c0 = ext.extractSprite(sheet, 0 * fw, 3 * fh, fw, fh);
            BufferedImage row3c1 = ext.extractSprite(sheet, 1 * fw, 3 * fh, fw, fh);
            BufferedImage row3c2 = ext.extractSprite(sheet, 2 * fw, 3 * fh, fw, fh);
            BufferedImage row3c3 = ext.extractSprite(sheet, 3 * fw, 3 * fh, fw, fh);
            shootIdleRightFrames = new BufferedImage[]{ row3c3 };
            shootRunRightFrames  = new BufferedImage[]{ row3c0, row3c1, row3c2 };

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

        dx = 0;
        if (moveLeft)  { dx = -speed; facingRight = false; }
        if (moveRight) { dx =  speed; facingRight = true;  }

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

        int newX = x + dx;
        if (!collidesWithLevel(newX, y)) {
            x = newX;
        }
        applyVerticalCollision();

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

                y = (newY / Level.TILE_SIZE + 1) * Level.TILE_SIZE;
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

        final int BULLET_SIZE = 120;

        int projX;
        if (facingRight) {
            projX = x + width - 52;
        } else {
            projX = x - BULLET_SIZE + 52;
        }
        int projY  = y + height / 3 - BULLET_SIZE / 2 - 22;

        int projDx;
        if (facingRight) {
            projDx = 14;
        } else {
            projDx = -14;
        }
        int dmg    = 10 + damageBonus;

        switch (gunType) {
            case TRIPLE_SHOT:
                addBullet(new Projectile(projX, projY,                projDx,  0,
                    Projectile.Type.PLAYER_LIGHT, dmg, bulletSprite),           BULLET_SIZE);
                addBullet(new Projectile(projX, projY - BULLET_SIZE / 3, projDx, -2,
                    Projectile.Type.PLAYER_LIGHT, dmg, tripleBulletUpSprite),   BULLET_SIZE);
                addBullet(new Projectile(projX, projY + BULLET_SIZE / 3, projDx,  2,
                    Projectile.Type.PLAYER_LIGHT, dmg, tripleBulletDownSprite), BULLET_SIZE);
                break;
            case CHARGE_SHOT:

                long held;
                if (chargeStartTime > 0) {
                    held = now - chargeStartTime;
                } else {
                    held = 0;
                }
                if (held >= 600) {
                    addBullet(new Projectile(projX, projY, projDx * 2, 0,
                        Projectile.Type.PLAYER_HEAVY, dmg * 3, bulletSprite), BULLET_SIZE);
                } else {
                    addBullet(new Projectile(projX, projY, projDx, 0,
                        Projectile.Type.PLAYER_LIGHT, dmg, bulletSprite), BULLET_SIZE);
                }
                chargeStartTime = -1;
                break;
            case NORMAL:
            default:
                addBullet(new Projectile(projX, projY, projDx, 0,
                    Projectile.Type.PLAYER_LIGHT, dmg, bulletSprite), BULLET_SIZE);
                break;
        }
        lastShotTime = now;
    }

    private void addBullet(Projectile p, int size) {
        p.width  = size;
        p.height = size;
        projectiles.add(p);
    }

    private void updateAnimation() {
        BufferedImage[] newFrames;
        String newAnim;

        boolean shooting = shootPressed;
        boolean moving   = dx != 0;

        if (!onGround) {

            if (shooting) {
                if (facingRight) {
                    newFrames = shootRunRightFrames;
                    newAnim = "shoot-jump-right";
                } else {
                    newFrames = shootRunLeftFrames;
                    newAnim = "shoot-jump-left";
                }
            } else {
                if (facingRight) {
                    newFrames = jumpRightFrames;
                    newAnim = "jump-right";
                } else {
                    newFrames = jumpLeftFrames;
                    newAnim = "jump-left";
                }
            }
        } else if (moving) {
            if (shooting) {
                if (facingRight) {
                    newFrames = shootRunRightFrames;
                    newAnim = "shoot-run-right";
                } else {
                    newFrames = shootRunLeftFrames;
                    newAnim = "shoot-run-left";
                }
            } else {
                if (facingRight) {
                    newFrames = runRightFrames;
                    newAnim = "run-right";
                } else {
                    newFrames = runLeftFrames;
                    newAnim = "run-left";
                }
            }
        } else {
            if (shooting) {
                if (facingRight) {
                    newFrames = shootIdleRightFrames;
                    newAnim = "shoot-idle-right";
                } else {
                    newFrames = shootIdleLeftFrames;
                    newAnim = "shoot-idle-left";
                }
            } else {
                if (facingRight) {
                    newFrames = idleRightFrames;
                    newAnim = "idle-right";
                } else {
                    newFrames = idleLeftFrames;
                    newAnim = "idle-left";
                }
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
    protected void drawSelf(Graphics2D g2) {
        BufferedImage frame = getCurrentFrame();
        if (frame != null) {

            int dx = x - 6;
            int dy = y - 3;
            int dw = width + 12;
            int dh = height + 6;
            g2.drawImage(frame, dx, dy, dw, dh, null);

            hitFlash.draw(g2, frame, dx, dy, dw, dh);
        } else {
            if (stunned) {
                g2.setColor(Color.YELLOW);
            } else {
                g2.setColor(Color.RED);
            }
            g2.fillRect(x, y, width, height);
        }
    }

    @Override
    public Rectangle2D.Double getBoundingRectangle() {
        BufferedImage frame = getCurrentFrame();
        if (frame == null) return super.getBoundingRectangle();
        Rectangle b = hitboxCache.get(frame);
        if (b == null) {
            b = opaqueBounds(frame);
            hitboxCache.put(frame, b);
        }
        double drawW = width + 12;
        double drawH = height + 6;
        double sx = drawW / SRC_FW;
        double sy = drawH / SRC_FH;
        return new Rectangle2D.Double(
            (x - 6) + b.x * sx,
            (y - 3) + b.y * sy,
            b.width  * sx,
            b.height * sy);
    }

    public void stun(int durationMs) {
        stunned = true;
        stunEndTime = System.currentTimeMillis() + durationMs;
    }
    public boolean isStunned() { return stunned; }

    public void takeDamage(int dmg) {
        health -= dmg;
        hitFlash.trigger();
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

    @Override public void boostDamage(int amount) { damageBonus += amount; }
    @Override public void boostJump(int amount)   { jumpStrength -= Math.abs(amount); }
    @Override public void boostSpeed(int amount)  { speed += amount; }
    @Override public void setGunType(GunType gunType) {
        if (gunType == null) {
            this.gunType = GunType.NORMAL;
        } else {
            this.gunType = gunType;
        }
        this.chargeStartTime = -1;
    }
    public GunType getGunType() { return gunType; }

    public void setMoveLeft(boolean b)    { moveLeft = b; }
    public void setMoveRight(boolean b)   { moveRight = b; }
    public void setJumpPressed(boolean b) { jumpPressed = b; }
    public void setShootPressed(boolean b){
        if (b && !shootPressed && gunType == GunType.CHARGE_SHOT) {
            chargeStartTime = System.currentTimeMillis();
        }
        shootPressed = b;
    }

    public boolean isOnGround()   { return onGround; }
    @Override public List<Projectile> getProjectiles() { return projectiles; }
    public void setLevel(Level level) { this.level = level; }
}
