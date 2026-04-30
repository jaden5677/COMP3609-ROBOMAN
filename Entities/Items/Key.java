package Entities.Items;

import java.awt.Color;
import java.awt.image.BufferedImage;

import Entities.Player.PlayerInterface;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

public class Key extends AbstractItem {

    private final Level level;

    private static BufferedImage keySprite;
    private static void ensureSprite() {
        if (keySprite != null) return;
        SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
        BufferedImage sheet = ext.loadSpriteSheet("TileSets/Dungeon_Tile_Set.png");
        if (sheet == null) return;
        keySprite = ext.extractSprite(sheet, 11 * 16, 9 * 16, 16, 16);
    }

    public Key(int x, int y, String imagePath, Level level) {
        super(x, y, imagePath, ItemType.Key);
        this.level = level;
        ensureSprite();
        this.sprite = keySprite;
    }

    @Override
    protected Color placeholderColor() { return new Color(255, 215, 0); }

    @Override
    public void applyToPlayer(PlayerInterface player) {
        if (!isVisible) return;
        if (level != null) level.collectKey();
        isVisible = false;
    }
}
