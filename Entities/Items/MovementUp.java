package Entities.Items;

import java.awt.Color;
import java.awt.image.BufferedImage;

import Entities.Player.PlayerInterface;
import ImageManager.SpriteSheetExtractor;

public class MovementUp extends AbstractItem {

    public enum BoostType { JUMP, SPEED }

    public final BoostType boostType;
    public final int amount;

    private static BufferedImage jumpSprite;
    private static BufferedImage speedSprite;
    private static void ensureSprites() {
        if (jumpSprite != null && speedSprite != null) return;
        SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
        BufferedImage sheet = ext.loadSpriteSheet("TileSets/fruit.png");
        if (sheet == null) return;
        jumpSprite  = ext.extractSprite(sheet, 0 * 16, 0 * 16, 16, 16);
        speedSprite = ext.extractSprite(sheet, 0 * 16, 2 * 16, 16, 16);
    }

    public MovementUp(int x, int y, String imagePath, BoostType boostType, int amount) {
        super(x, y, imagePath, ItemType.MovementUp);
        this.boostType = boostType;
        this.amount = amount;
        ensureSprites();
        if (boostType == BoostType.JUMP) {
            this.sprite = jumpSprite;
        } else {
            this.sprite = speedSprite;
        }
    }

    @Override
    protected Color placeholderColor() {
        if (boostType == BoostType.JUMP) {
            return new Color(80, 200, 255);
        }
        return new Color(120, 255, 120);
    }

    @Override
    public void applyToPlayer(PlayerInterface player) {
        if (!isVisible) return;
        switch (boostType) {
            case JUMP:  player.boostJump(amount);  break;
            case SPEED: player.boostSpeed(amount); break;
        }
        isVisible = false;
    }
}
