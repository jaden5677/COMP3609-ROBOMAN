package Entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import Behaviours.Behaviour;
import ImageManager.SpriteSheetExtractor;

public abstract class AbstractEntity implements EntityInterface {
    public int x;
    public int y;
    public int width;
    public int height;
    public String imagePath;
    public boolean isVisible;
    public int points;
    public int health;
    public int damage;
    public java.awt.Image image;
    SpriteSheetExtractor extractor = SpriteSheetExtractor.getInstance();
    public ArrayList<Behaviour> behaviours;

    // Previous position for erasing
    protected int prevX;
    protected int prevY;
    protected int prevWidth;
    protected int prevHeight;

    // Movement and animation fields
    protected int dx;
    protected int dy;
    protected BufferedImage[] animFrames;
    protected int currentFrame;
    protected int animTick;
    protected int animSpeed;
    protected boolean facingRight;

    public AbstractEntity(int x, int y, String imagePath) {
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
        this.imagePath = imagePath;
        this.behaviours = new ArrayList<>();
        this.isVisible = true;
        this.facingRight = true;
        this.animSpeed = 5;
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                BufferedImage spriteSheet = extractor.loadSpriteSheet(imagePath);
                if (spriteSheet != null) {
                    BufferedImage[] sprites = extractor.extractRow(spriteSheet, 0, 1,
                        spriteSheet.getWidth(), spriteSheet.getHeight());
                    this.image = sprites[0];
                }
            } catch (Exception e) {
                System.out.println("Could not load sprite: " + imagePath);
            }
        }
    }

    public AbstractEntity(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
        this.prevWidth = width;
        this.prevHeight = height;
        this.width = width;
        this.height = height;
        this.behaviours = new ArrayList<>();
        this.isVisible = true;
        this.facingRight = true;
        this.animSpeed = 5;
    }

    /** Clears the entity's previous position by painting over it with the given background colour. */
    public void erase(Graphics2D g, Color bgColor) {
        g.setColor(bgColor);
        g.fillRect(prevX, prevY,
                   prevWidth  > 0 ? prevWidth  : width,
                   prevHeight > 0 ? prevHeight : height);
    }

    /** Saves the current position as the previous position (call before updating). */
    public void savePreviousPosition() {
        prevX = x;
        prevY = y;
        prevWidth = width;
        prevHeight = height;
    }

    @Override
    public void draw(Graphics2D g) {
        if (!isVisible) return;
        BufferedImage frame = getCurrentFrame();
        if (frame != null) {
            // Preserve aspect ratio: fit frame into width x height box
            int fw = frame.getWidth();
            int fh = frame.getHeight();
            double scale = Math.min((double) width / fw, (double) height / fh);
            int drawW = (int)(fw * scale);
            int drawH = (int)(fh * scale);
            int offsetX = (width - drawW) / 2;
            int offsetY = height - drawH; // align to bottom (feet on ground)
            if (facingRight) {
                g.drawImage(frame, x + offsetX, y + offsetY, drawW, drawH, null);
            } else {
                g.drawImage(frame, x + offsetX + drawW, y + offsetY, -drawW, drawH, null);
            }
        } else if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        }
    }

    /** Convenience: erase old position, then draw at current position. */
    public void eraseAndDraw(Graphics2D g, Color bgColor) {
        erase(g, bgColor);
        draw(g);
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

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    @Override
    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    @Override
    public abstract void update();
}

