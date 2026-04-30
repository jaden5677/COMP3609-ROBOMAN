package Entities.Items;

import java.awt.Color;
import java.awt.image.BufferedImage;

import Entities.Player.PlayerInterface;
import ImageManager.SpriteSheetExtractor;

public class HealthPacks extends AbstractItem {

    public enum HealthPackType {
        SMALL(15),
        MEDIUM(30),
        LARGE(45);

        public final int healAmount;
        HealthPackType(int healAmount) { this.healAmount = healAmount; }
    }

    public final HealthPackType healthPackType;
    public final int healAmount;

    private static BufferedImage[] sprites;
    private static void ensureSprites() {
        if (sprites != null) return;
        SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
        BufferedImage sheet = ext.loadSpriteSheet("TileSets/fruit.png");
        if (sheet == null) return;
        sprites = new BufferedImage[3];
        sprites[0] = ext.extractSprite(sheet, 0 * 16, 3 * 16, 16, 16);
        sprites[1] = ext.extractSprite(sheet, 1 * 16, 3 * 16, 16, 16);
        sprites[2] = ext.extractSprite(sheet, 2 * 16, 3 * 16, 16, 16);
    }

    public HealthPacks(int x, int y, String imagePath, HealthPackType healthPackType) {
        super(x, y, imagePath, ItemType.HealthPack);
        this.healthPackType = healthPackType;
        this.healAmount = healthPackType.healAmount;
        ensureSprites();
        if (sprites != null) this.sprite = sprites[healthPackType.ordinal()];
    }

    public int getHealAmount() { return healAmount; }

    @Override
    protected Color placeholderColor() {

        switch (healthPackType) {
            case SMALL:  return new Color(255, 150, 150);
            case MEDIUM: return new Color(220,  60,  60);
            case LARGE:  return new Color(140,   0,   0);
            default:     return Color.RED;
        }
    }

    @Override
    public void applyToPlayer(PlayerInterface player) {
        if (!isVisible) return;
        player.heal(healAmount);
        isVisible = false;
    }
}
