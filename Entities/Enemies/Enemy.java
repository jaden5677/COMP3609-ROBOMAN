package Entities.Enemies;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import Entities.Player.Player;
import Entities.Projectiles.Projectile;
import MainGame.Level;

public abstract class Enemy extends AbstractEnemy {

    protected int maxHealth;
    protected boolean alive;
    protected Player player;
    protected Level level;
    protected List<Projectile> projectiles;
    protected Color fallbackColor;

    // Gravity for ground-based enemies
    protected boolean affectedByGravity;
    protected int fallSpeed;
    private static final int MAX_FALL = 8;

    public Enemy(int x, int y, int width, int height, int hp, int pts,
                 Player player, Level level) {
        super(x, y, width, height);
        this.maxHealth = hp;
        this.health = hp;
        this.points = pts;
        this.alive = true;
        this.player = player;
        this.level = level;
        this.projectiles = new ArrayList<>();
        this.fallbackColor = Color.MAGENTA;
        this.affectedByGravity = false;
        this.fallSpeed = 0;
    }

    public void takeDamage(int dmg, Projectile.Type attackType) {
        if (!alive) return;
        health -= dmg;
        if (health <= 0) {
            health = 0;
            alive = false;
            isVisible = false;
        }
    }

    @Override
    protected void drawSelf(Graphics2D g2) {
        if (!alive) return;
        BufferedImage frame = getCurrentFrame();
        if (frame != null) {
            if (facingRight) {
                g2.drawImage(frame, x, y, width, height, null);
            } else {
                g2.drawImage(frame, x + width, y, -width, height, null);
            }
        } else {
            g2.setColor(fallbackColor);
            g2.fillRect(x, y, width, height);
        }
    }

    /**
     * Per-frame hitbox: tight opaque-pixel bounds of the current frame mapped
     * onto this entity's full draw rect (which {@link #drawSelf} stretches
     * from the source frame to {@code width x height}).
     */
    @Override
    public java.awt.geom.Rectangle2D.Double getBoundingRectangle() {
        BufferedImage frame = getCurrentFrame();
        if (frame == null) return super.getBoundingRectangle();
        java.awt.Rectangle b = enemyHitboxCache.get(frame);
        if (b == null) {
            b = opaqueBounds(frame);
            enemyHitboxCache.put(frame, b);
        }
        int fw = frame.getWidth();
        int fh = frame.getHeight();
        double sx = (double) width  / fw;
        double sy = (double) height / fh;
        double bx = facingRight ? b.x : (fw - b.x - b.width);
        return new java.awt.geom.Rectangle2D.Double(
            x + bx * sx,
            y + b.y * sy,
            b.width  * sx,
            b.height * sy);
    }
    private final java.util.Map<BufferedImage, java.awt.Rectangle> enemyHitboxCache =
        new java.util.IdentityHashMap<>();

    protected void applyGravity() {
        if (!affectedByGravity) return;
        if (!level.isSolid(x + width / 2, y + height + 1)) {
            fallSpeed += 1;
            if (fallSpeed > MAX_FALL) fallSpeed = MAX_FALL;
            y += fallSpeed;
        } else {
            y = ((y + height) / Level.TILE_SIZE) * Level.TILE_SIZE - height;
            fallSpeed = 0;
        }
    }

    protected double distanceToPlayer() {
        int px = player.getX() + player.getWidth() / 2;
        int py = player.getY() + player.getHeight() / 2;
        int ex = x + width / 2;
        int ey = y + height / 2;
        return Math.sqrt((double)(px - ex) * (px - ex) + (double)(py - ey) * (py - ey));
    }

    protected boolean playerInRange(int range) {
        return distanceToPlayer() <= range;
    }

    protected void facePlayer() {
        facingRight = player.getX() > x;
    }

    public boolean isAlive()       { return alive; }
    public int getPoints()         { return points; }
    public int getEnemyHealth()    { return health; }
    public List<Projectile> getProjectiles() { return projectiles; }
}
