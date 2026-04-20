package Entities;
import java.awt.image.BufferedImage;
public abstract class AbstractItem implements EntityInterface {
    public enum ItemType {
        HealthPack,
        DamageUp,
        MovementBoost,
        GunType
    }

    public int x;
    public int y;
    public int width = 16;
    public int height = 16;
    public String imagePath;
    public boolean isVisible;
    public java.awt.Image image;
    public ItemType itemType;
    public BufferedImage sprite;

    public AbstractItem(int x, int y, String imagePath, ItemType itemType) {
        this.x = x;
        this.y = y;
        this.imagePath = imagePath;
        this.itemType = itemType;
        this.isVisible = true;
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                this.image = new javax.swing.ImageIcon(imagePath).getImage();
            } catch (Exception e) {
                System.out.println("Could not load sprite: " + imagePath);
            }
        }
    }
    
    // All Items should be 16 by 16 for simplicity
    @Override
    public void draw(java.awt.Graphics2D g2) {
        if (!isVisible) return;
        if (sprite != null) {
            g2.drawImage(sprite, x, y, width * 2, height * 2, null);
        } else {
            g2.setColor(java.awt.Color.GREEN);
            g2.fillRect(x, y, width, height);
        }
    }

    @Override
    public void update() {
        // Default items do not have any behavior, but this can be overridden by subclasses
    }

    
    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public java.awt.geom.Rectangle2D.Double getBoundingRectangle() {
        return new java.awt.geom.Rectangle2D.Double(x, y, width, height);
    }


}