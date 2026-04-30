package Entities.Items;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Entities.AbstractEntity;
import Entities.Player.PlayerInterface;

public abstract class AbstractItem extends AbstractEntity implements ItemInterface {

    public enum ItemType {
        HealthPack,
        DamageUp,
        MovementUp,
        GunType,
        Key
    }

    public final ItemType itemType;
    public BufferedImage sprite;

    protected AbstractItem(int x, int y, String imagePath, ItemType itemType) {
        super(x, y, 32, 32);
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

    protected Color placeholderColor() { return Color.GREEN; }

    @Override
    protected void drawSelf(Graphics2D g2) {
        if (sprite != null) {
            g2.drawImage(sprite, x, y, width, height, null);
            return;
        }
        if (image != null) {
            g2.drawImage(image, x, y, width, height, null);
            return;
        }

        g2.setColor(placeholderColor());
        g2.fillRect(x, y, width, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width - 1, height - 1);
        g2.setColor(Color.WHITE);
        g2.drawRect(x + 4, y + 4, width - 9, height - 9);
    }

    @Override
    public void update() {

    }

    @Override public ItemType getItemType() { return itemType; }

    @Override
    public abstract void applyToPlayer(PlayerInterface player);
}