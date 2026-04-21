package Entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Shared implementation for every entity (Player, Enemy, Item, Projectile, ...).
 * Owns position, size, visibility, velocity and basic frame-based animation
 * so concrete subclasses only have to implement category-specific behaviour.
 */
public abstract class AbstractEntity implements EntityInterface {

    // Position / size
    public int x;
    public int y;
    public int width;
    public int height;

    // Velocity
    public int dx;
    public int dy;

    // Rendering
    public boolean isVisible;
    public boolean facingRight;
    public java.awt.Image image;
    public String imagePath;

    // Animation
    protected BufferedImage[] animFrames;
    protected int currentFrame;
    protected int animTick;
    protected int animSpeed;

    // Previous-position tracking (for erase-and-redraw renderers)
    protected int prevX;
    protected int prevY;
    protected int prevWidth;
    protected int prevHeight;

    protected AbstractEntity(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.prevX = x;
        this.prevY = y;
        this.prevWidth = width;
        this.prevHeight = height;
        this.isVisible = true;
        this.facingRight = true;
        this.animSpeed = 5;
    }

    // ---------------- Animation helpers ----------------

    protected BufferedImage getCurrentFrame() {
        if (animFrames != null && animFrames.length > 0) {
            return animFrames[currentFrame % animFrames.length];
        }
        return null;
    }

    protected void animate() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            if (animFrames != null && animFrames.length > 0) {
                currentFrame = (currentFrame + 1) % animFrames.length;
            }
        }
    }

    // ---------------- Erase / draw helpers ----------------

    /** Saves the current position as the previous position (call before updating). */
    public void savePreviousPosition() {
        prevX = x;
        prevY = y;
        prevWidth = width;
        prevHeight = height;
    }

    /** Paints over the previous position with the given background colour. */
    public void erase(Graphics2D g, Color bgColor) {
        g.setColor(bgColor);
        g.fillRect(prevX, prevY,
                   prevWidth  > 0 ? prevWidth  : width,
                   prevHeight > 0 ? prevHeight : height);
    }

    public void eraseAndDraw(Graphics2D g, Color bgColor) {
        erase(g, bgColor);
        draw(g);
    }

    // ---------------- EntityInterface ----------------

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public int getWidth()  { return width; }
    @Override public int getHeight() { return height; }

    @Override public boolean isVisible()             { return isVisible; }
    @Override public void setVisible(boolean visible){ this.isVisible = visible; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    @Override
    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    @Override public abstract void update();
    @Override public abstract void draw(Graphics2D g2);
}
