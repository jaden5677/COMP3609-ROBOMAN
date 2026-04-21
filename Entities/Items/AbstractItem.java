package Entities.Items;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Entities.AbstractEntity;
import Entities.Player.PlayerInterface;

/**
 * Common base for every collectible item.
 * Items don't move on their own and use a placeholder coloured rectangle
 * until real sprites are wired in. Subclasses override {@link #placeholderColor()}
 * to make their on-screen rectangle visually distinct.
 */
public abstract class AbstractItem extends AbstractEntity implements ItemInterface {

    /** Top-level item category. */
    public enum ItemType {
        HealthPack,
        DamageUp,
        MovementUp,
        GunType
    }

    public final ItemType itemType;
    public BufferedImage sprite;

    protected AbstractItem(int x, int y, String imagePath, ItemType itemType) {
        super(x, y, 32, 32); // 32x32 on screen (16x16 logical, drawn 2x)
        this.imagePath = imagePath;
        this.itemType = itemType;
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                this.image = new javax.swing.ImageIcon(imagePath).getImage();
            } catch (Exception e) {
                System.out.println("Could not load sprite: " + imagePath);
            }
        }
    }

    /** Placeholder colour used while a real sprite hasn't been supplied yet. */
    protected Color placeholderColor() { return Color.GREEN; }

    @Override
    public void draw(Graphics2D g2) {
        if (!isVisible) return;
        if (sprite != null) {
            g2.drawImage(sprite, x, y, width, height, null);
            return;
        }
        if (image != null) {
            g2.drawImage(image, x, y, width, height, null);
            return;
        }
        // Placeholder rectangle with a small inner highlight + black border
        g2.setColor(placeholderColor());
        g2.fillRect(x, y, width, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width - 1, height - 1);
        g2.setColor(Color.WHITE);
        g2.drawRect(x + 4, y + 4, width - 9, height - 9);
    }

    @Override
    public void update() {
        // Items are stationary by default; subclasses can override (e.g. floating animation).
    }

    @Override public ItemType getItemType() { return itemType; }

    @Override
    public abstract void applyToPlayer(PlayerInterface player);
}