package Entities.Items;

import java.awt.Color;
import java.awt.image.BufferedImage;

import Entities.Player.PlayerInterface;
import ImageManager.SpriteSheetExtractor;

public class DamageUp extends AbstractItem {

    public enum Tier {
        SMALL(5),
        MEDIUM(10),
        LARGE(20);

        public final int damageBonus;
        Tier(int damageBonus) { this.damageBonus = damageBonus; }
    }

    public final Tier tier;

    private static BufferedImage iconSprite;
    private static void ensureSprite() {
        if (iconSprite != null) return;
        SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
        BufferedImage sheet = ext.loadSpriteSheet("TileSets/Gun.png");
        if (sheet == null) return;
        iconSprite = ext.extractSprite(sheet, 7 * 16, 0 * 16, 16, 16);
    }

    public DamageUp(int x, int y, String imagePath, Tier tier) {
        super(x, y, imagePath, ItemType.DamageUp);
        this.tier = tier;
        ensureSprite();
        this.sprite = iconSprite;
    }

    @Override
    protected Color placeholderColor() {
        switch (tier) {
            case SMALL:  return new Color(255, 200, 100);
            case MEDIUM: return new Color(255, 140,   0);
            case LARGE:  return new Color(180,  60,   0);
            default:     return Color.ORANGE;
        }
    }

    @Override
    public void applyToPlayer(PlayerInterface player) {
        if (!isVisible) return;
        player.boostDamage(tier.damageBonus);
        isVisible = false;
    }
}
