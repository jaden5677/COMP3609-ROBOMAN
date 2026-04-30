package Entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public abstract class AbstractEntity implements EntityInterface {

    public static boolean DEBUG_HITBOXES = true;
    public static Color DEBUG_HITBOX_COLOR = Color.RED;

    protected void drawHitbox(Graphics2D g2) {
        if (!DEBUG_HITBOXES) return;
        Rectangle2D.Double r = getBoundingRectangle();
        Color prev = g2.getColor();
        g2.setColor(DEBUG_HITBOX_COLOR);
        g2.drawRect((int) r.x, (int) r.y, (int) r.width, (int) r.height);
        g2.setColor(prev);
    }

    public int x;
    public int y;
    public int width;
    public int height;

    public int dx;
    public int dy;

    public boolean isVisible;
    public boolean facingRight;
    public java.awt.Image image;
    public String imagePath;

    protected BufferedImage[] animFrames;
    protected int currentFrame;
    protected int animTick;
    protected int animSpeed;

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

    public void savePreviousPosition() {
        prevX = x;
        prevY = y;
        prevWidth = width;
        prevHeight = height;
    }

    public void erase(Graphics2D g, Color bgColor) {
        int eraseWidth;
        int eraseHeight;
        if (prevWidth > 0) {
            eraseWidth = prevWidth;
        } else {
            eraseWidth = width;
        }
        if (prevHeight > 0) {
            eraseHeight = prevHeight;
        } else {
            eraseHeight = height;
        }
        g.setColor(bgColor);
        g.fillRect(prevX, prevY, eraseWidth, eraseHeight);
    }

    public void eraseAndDraw(Graphics2D g, Color bgColor) {
        erase(g, bgColor);
        draw(g);
    }

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

    @Override
    public final void draw(Graphics2D g2) {
        if (!isVisible) return;
        drawSelf(g2);
        drawHitbox(g2);
    }

    protected abstract void drawSelf(Graphics2D g2);

    public static Rectangle opaqueBounds(BufferedImage img) {
        if (img == null) return new Rectangle(0, 0, 0, 0);
        int w = img.getWidth();
        int h = img.getHeight();
        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                int alpha = (img.getRGB(xx, yy) >> 24) & 0xff;
                if (alpha > 16) {
                    if (xx < minX) minX = xx;
                    if (yy < minY) minY = yy;
                    if (xx > maxX) maxX = xx;
                    if (yy > maxY) maxY = yy;
                }
            }
        }
        if (maxX < 0) return new Rectangle(0, 0, w, h);
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}
