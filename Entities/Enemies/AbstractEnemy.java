package Entities.Enemies;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Behaviours.Behaviour;
import Entities.AbstractEntity;
import ImageManager.SpriteSheetExtractor;

/**
 * Common base for every enemy. Adds combat / scoring fields on top of
 * the rendering and animation features inherited from {@link AbstractEntity}.
 */
public abstract class AbstractEnemy extends AbstractEntity implements EnemyInterface {

    public int points;
    public int health;
    public int damage;

    protected SpriteSheetExtractor extractor = SpriteSheetExtractor.getInstance();
    public ArrayList<Behaviour> behaviours;

    public AbstractEnemy(int x, int y, String imagePath) {
        super(x, y, 0, 0);
        this.imagePath = imagePath;
        this.behaviours = new ArrayList<>();
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

    public AbstractEnemy(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.behaviours = new ArrayList<>();
    }

    @Override
    public void draw(Graphics2D g) {
        if (!isVisible) return;
        BufferedImage frame = getCurrentFrame();
        if (frame != null) {
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
        } else {
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, width, height);
        }
    }

    // EnemyInterface ----------------------------------------------------------

    @Override public int getPoints()      { return points; }
    @Override public int getEnemyHealth() { return health; }

    @Override
    public abstract void update();
}

