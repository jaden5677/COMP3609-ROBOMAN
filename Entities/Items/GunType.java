package Entities.Items;

import java.awt.Color;
import java.awt.image.BufferedImage;

import Entities.Player.PlayerInterface;
import ImageManager.SpriteSheetExtractor;

public class GunType extends AbstractItem {

    public enum Variant {
        TRIPLE_SHOT(PlayerInterface.GunType.TRIPLE_SHOT),
        CHARGE_SHOT(PlayerInterface.GunType.CHARGE_SHOT);

        public final PlayerInterface.GunType playerGun;
        Variant(PlayerInterface.GunType playerGun) { this.playerGun = playerGun; }
    }

    public final Variant variant;

    private static BufferedImage tripleSprite;
    private static BufferedImage chargeSprite;
    private static void ensureSprites() {
        if (tripleSprite != null && chargeSprite != null) return;
        SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
        BufferedImage sheet = ext.loadSpriteSheet("TileSets/Gun.png");
        if (sheet == null) return;
        chargeSprite = ext.extractSprite(sheet, 5 * 16, 0 * 16, 16, 16);
        tripleSprite = ext.extractSprite(sheet, 6 * 16, 0 * 16, 16, 16);
    }

    public GunType(int x, int y, String imagePath, Variant variant) {
        super(x, y, imagePath, ItemType.GunType);
        this.variant = variant;
        ensureSprites();
        if (variant == Variant.TRIPLE_SHOT) {
            this.sprite = tripleSprite;
        } else {
            this.sprite = chargeSprite;
        }
    }

    @Override
    protected Color placeholderColor() {
        if (variant == Variant.TRIPLE_SHOT) {
            return new Color(255, 220, 40);
        }
        return new Color(180, 60, 220);
    }

    @Override
    public void applyToPlayer(PlayerInterface player) {
        if (!isVisible) return;
        player.setGunType(variant.playerGun);
        isVisible = false;
    }
}
