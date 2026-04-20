package Entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

public class Player extends AbstractEntity {

    // Physics
    private static final int SPEED = 5;
    private static final int JUMP_STRENGTH = -14;
    private static final int GRAVITY = 1;
    private static final int MAX_FALL_SPEED = 10;

    private boolean onGround;

    // Health
    private int maxHealth;
    private int lives;
    private int deathsInLevel;
    private int currentLevel;

    // Shooting
    private long lastShotTime;
    private static final long SHOT_COOLDOWN = 300;

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

    // Animation sets
    private BufferedImage[] rightframes;
    private BufferedImage[] leftframes;
    private BufferedImage[] idleFrames;
    private BufferedImage[] rightShootFrames;
    private BufferedImage[] leftShootFrames;
    private BufferedImage[] jumpFrames;
    private BufferedImage[] walkFrames;
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
            BufferedImage sheet = ext.loadSpriteSheet("SpriteSheets/RoboManFull.png");
            if (sheet != null) {
                // RoboManFull.png is 512x522: 6 cols x 5 rows
                int fw = 40;
                int fh = 36;
                rightframes = ext.extractRow(sheet, 0, 6, fw, fh);
                leftframes = ext.extractRow(sheet, 1, 6, fw, fh);
                idleFrames = new BufferedImage[]{ ext.extractSprite(sheet, 0, 0, fw, fh) };
                rightShootFrames = ext.extractRow(sheet, 3, 3, fw, fh);
                leftShootFrames = ext.extractRow(sheet, 4, 3, fw, fh);
                animFrames = idleFrames;
            }
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
        if (moveLeft)  { dx = -SPEED; facingRight = false; }
        if (moveRight) { dx = SPEED;  facingRight = true;  }

        // Jump
        if (jumpPressed && onGround) {
            dy = JUMP_STRENGTH;
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
        if (now - lastShotTime >= SHOT_COOLDOWN) {
            int projX = facingRight ? x + width : x - 24;
            int projY = y + height / 2 - 12;
            int projDx = facingRight ? 8 : -8;
            projectiles.add(new Projectile(projX, projY, projDx, 0,
                Projectile.Type.PLAYER_LIGHT, 10));
            lastShotTime = now;
        }
    }

    private void updateAnimation() {
        BufferedImage[] newFrames = idleFrames;
        String newAnim = "idle";

        if (!onGround)     { newFrames = facingRight ? rightframes : leftframes;  newAnim = "jump";  }
        else if (dx != 0)  { newFrames = facingRight ? rightframes : leftframes;  newAnim = "walk";  }
        else if (shootPressed) { newFrames = facingRight ? rightShootFrames : leftShootFrames; newAnim = "shoot"; }

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
            if (facingRight) {
                g2.drawImage(frame, x - 6, y - 3, width + 12, height + 6, null);
            } else {
                g2.drawImage(frame, x + width + 6, y - 3, -(width + 12), height + 6, null);
            }
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
    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }

    // --- Input setters ---
    public void setMoveLeft(boolean b)    { moveLeft = b; }
    public void setMoveRight(boolean b)   { moveRight = b; }
    public void setJumpPressed(boolean b) { jumpPressed = b; }
    public void setShootPressed(boolean b){ shootPressed = b; }

    // --- Getters ---
    public int getHealth()        { return health; }
    public int getMaxHealth()     { return maxHealth; }
    public int getLives()         { return lives; }
    public int getDeathsInLevel() { return deathsInLevel; }
    public int getCurrentLevel()  { return currentLevel; }
    public boolean isOnGround()   { return onGround; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public void setLevel(Level level) { this.level = level; }
}
