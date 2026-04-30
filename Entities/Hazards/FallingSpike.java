package Entities.Hazards;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import MainGame.Level;

public class FallingSpike {

    private static final int FALL_SPEED   = 14;
    private static final int LIFETIME_CAP = 240;
    private static final int SIZE         = Level.TILE_SIZE;
    public  static final int DAMAGE       = 30;

    private int x;
    private int y;
    private int ticks;
    private boolean alive = true;
    private final BufferedImage sprite;

    public FallingSpike(int x, int y, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.sprite = sprite;
    }

    public void update(Level level) {
        if (!alive) return;
        ticks++;
        if (ticks > LIFETIME_CAP) { alive = false; return; }
        y += FALL_SPEED;

        if (level.isSolid(x + SIZE / 2, y + SIZE - 1)) {
            alive = false;
        }
    }

    public void draw(Graphics2D g2) {
        if (!alive) return;
        if (sprite != null) {
            g2.drawImage(sprite, x, y, SIZE, SIZE, null);
        } else {
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(x, y, SIZE, SIZE);
        }
    }

    public Rectangle2D.Double getBoundingRectangle() {

        int pad = SIZE / 5;
        return new Rectangle2D.Double(x + pad, y + pad / 2, SIZE - 2 * pad, SIZE - pad);
    }

    public boolean isAlive()   { return alive; }
    public void    kill()      { alive = false; }
    public int     getDamage() { return DAMAGE; }
    public int     getX()      { return x; }
    public int     getY()      { return y; }
}
