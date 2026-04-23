package Entities.Projectiles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.Map;

import Entities.AbstractEntity;
import MainGame.Level;

public class Projectile extends AbstractEntity {

    public enum Type {
        PLAYER_LIGHT,
        PLAYER_HEAVY,
        ENEMY_NORMAL,
        ENEMY_ELECTRIC,
        ENEMY_SHOCKWAVE
    }

    private Type type;
    private int projectileDamage;
    private int lifetime;
    private static final int MAX_LIFETIME = 100;
    private BufferedImage sprite;
    private final Map<BufferedImage, Rectangle> hitboxCache = new IdentityHashMap<>();

    public Projectile(int x, int y, int dx, int dy, Type type, int damage) {
        super(x, y, 48, 48);
        this.dx = dx;
        this.dy = dy;
        this.type = type;
        this.projectileDamage = damage;
        this.lifetime = MAX_LIFETIME;
    }

    public Projectile(int x, int y, int dx, int dy, Type type, int damage, BufferedImage sprite) {
        this(x, y, dx, dy, type, damage);
        this.sprite = sprite;
    }

    @Override
    public void update() {
        x += dx;
        y += dy;
        lifetime--;
        if (lifetime <= 0) {
            isVisible = false;
        }
    }

    @Override
    protected void drawSelf(Graphics2D g2) {
        if (sprite != null) {
            g2.drawImage(sprite, x, y, width, height, null);
            return;
        }
        Color color;
        switch (type) {
            case PLAYER_LIGHT:   color = Color.YELLOW; break;
            case PLAYER_HEAVY:   color = Color.ORANGE; break;
            case ENEMY_NORMAL:   color = Color.RED;    break;
            case ENEMY_ELECTRIC: color = Color.CYAN;   break;
            default:             color = Color.WHITE;
        }
        g2.setColor(color);
        g2.fillOval(x, y, width, height);
    }

    public boolean isActive() {
        return isVisible && lifetime > 0;
    }

    public boolean collidesWithTile(Level level) {
        // Use the tight opaque-pixel hitbox, not the (often much larger)
        // draw-cell rect. Otherwise a big bullet sprite spawned next to the
        // player whose draw box dips into the adjacent floor would be
        // killed on its very first tick before ever leaving the muzzle.
        Rectangle2D.Double r = getBoundingRectangle();
        int left   = (int) r.x;
        int right  = (int) (r.x + r.width  - 1);
        int top    = (int) r.y;
        int bottom = (int) (r.y + r.height - 1);
        return level.isSolid(left, top)    || level.isSolid(right, top) ||
               level.isSolid(left, bottom) || level.isSolid(right, bottom);
    }

    public Type getType() { return type; }
    public int getProjectileDamage() { return projectileDamage; }

    /**
     * Per-frame hitbox: when a sprite is supplied, use its tight opaque-pixel
     * bounds mapped onto the on-screen draw rect (`x, y, width, height`).
     * For the placeholder oval/no-sprite path, fall back to the full rect.
     */
    @Override
    public Rectangle2D.Double getBoundingRectangle() {
        if (sprite == null) return super.getBoundingRectangle();
        Rectangle b = hitboxCache.get(sprite);
        if (b == null) {
            b = opaqueBounds(sprite);
            hitboxCache.put(sprite, b);
        }
        int fw = sprite.getWidth();
        int fh = sprite.getHeight();
        double sx = (double) width  / fw;
        double sy = (double) height / fh;
        return new Rectangle2D.Double(
            x + b.x * sx,
            y + b.y * sy,
            b.width  * sx,
            b.height * sy);
    }
}
