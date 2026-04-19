package ImageManager;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;


public class SpriteSheetExtractor {

    private static SpriteSheetExtractor instance;

   
    private final Map<String, BufferedImage> spriteCache;

    private SpriteSheetExtractor() {
        spriteCache = new HashMap<>();
    }

    public static synchronized SpriteSheetExtractor getInstance() {
        if (instance == null) {
            instance = new SpriteSheetExtractor();
        }
        return instance;
    }


    public BufferedImage loadSpriteSheet(String filePath) {
        return ImageManager.loadBufferedImage(filePath);
    }


    public BufferedImage extractSprite(BufferedImage sheet, int x, int y, int width, int height) {
        return sheet.getSubimage(x, y, width, height);
    }

    public BufferedImage extractSprite(BufferedImage sheet, int col, int row, int spriteWidth, int spriteHeight, boolean byGrid) {
        int x = col * spriteWidth;
        int y = row * spriteHeight;
        return sheet.getSubimage(x, y, spriteWidth, spriteHeight);
    }

    public BufferedImage[] extractRow(BufferedImage sheet, int row, int numFrames, int spriteWidth, int spriteHeight) {
        BufferedImage[] frames = new BufferedImage[numFrames];
        for (int i = 0; i < numFrames; i++) {
            frames[i] = sheet.getSubimage(i * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight);
        }
        return frames;
    }

    public BufferedImage[] extractColumn(BufferedImage sheet, int col, int numFrames, int spriteWidth, int spriteHeight) {
        BufferedImage[] frames = new BufferedImage[numFrames];
        for (int i = 0; i < numFrames; i++) {
            frames[i] = sheet.getSubimage(col * spriteWidth, i * spriteHeight, spriteWidth, spriteHeight);
        }
        return frames;
    }


    public BufferedImage[][] extractAll(BufferedImage sheet, int cols, int rows, int spriteWidth, int spriteHeight) {
        BufferedImage[][] sprites = new BufferedImage[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                sprites[r][c] = sheet.getSubimage(c * spriteWidth, r * spriteHeight, spriteWidth, spriteHeight);
            }
        }
        return sprites;
    }

    public BufferedImage getCachedSprite(String filePath, int x, int y, int spriteWidth, int spriteHeight) {
        String key = filePath + "_" + x + "_" + y + "_" + spriteWidth + "_" + spriteHeight;

        if (spriteCache.containsKey(key)) {
            return spriteCache.get(key);
        }

        BufferedImage sheet = loadSpriteSheet(filePath);
        if (sheet == null) {
            return null;
        }

        BufferedImage sprite = sheet.getSubimage(x, y, spriteWidth, spriteHeight);
        spriteCache.put(key, sprite);
        return sprite;
    }


    public void clearCache() {
        spriteCache.clear();
    }

    /**
     * Trims transparent pixels from all edges of an image.
     * Returns a new BufferedImage containing only the visible content.
     */
    public BufferedImage trimTransparent(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int top = height, left = width, bottom = 0, right = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = (img.getRGB(x, y) >> 24) & 0xFF;
                if (alpha > 0) {
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                    if (x < left) left = x;
                    if (x > right) right = x;
                }
            }
        }

        if (top > bottom || left > right) {
            return img; // fully transparent, return as-is
        }

        int tw = right - left + 1;
        int th = bottom - top + 1;
        return img.getSubimage(left, top, tw, th);
    }

    /**
     * Extracts a row of frames and trims transparent edges from each.
     */
    public BufferedImage[] extractRowTrimmed(BufferedImage sheet, int row, int numFrames,
                                             int spriteWidth, int spriteHeight) {
        BufferedImage[] frames = extractRow(sheet, row, numFrames, spriteWidth, spriteHeight);
        for (int i = 0; i < frames.length; i++) {
            frames[i] = trimTransparent(frames[i]);
        }
        return frames;
    }

    /**
     * Extracts a sprite by pixel coordinates with an explicit y-offset.
     */
    public BufferedImage[] extractRowAt(BufferedImage sheet, int yOffset, int numFrames,
                                        int spriteWidth, int spriteHeight) {
        BufferedImage[] frames = new BufferedImage[numFrames];
        for (int i = 0; i < numFrames; i++) {
            frames[i] = sheet.getSubimage(i * spriteWidth, yOffset, spriteWidth, spriteHeight);
        }
        return frames;
    }
}
