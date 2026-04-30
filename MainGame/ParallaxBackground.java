package MainGame;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ParallaxBackground {

    private final BufferedImage[] layers;
    private final float[] speeds;
    private final int[] yOffsets;
    private final int screenW;
    private final int screenH;

    public ParallaxBackground(int screenWidth, int screenHeight) {
        this.screenW = screenWidth;
        this.screenH = screenHeight;

        String[] paths = {
            "Background/Grassland/GrassLand_Background_1.png",
            "Background/Grassland/GrassLand_Background_2.png",
            "Background/Grassland/GrassLand_Background_3.png",
            "Background/Grassland/GrassLand_Background_4.png",
            "Background/Grassland/GrassLand_Background_5.png",
        };

        this.speeds = new float[] { 0.05f, 0.15f, 0.30f, 0.50f, 0.75f };

        layers = new BufferedImage[paths.length];
        yOffsets = new int[paths.length];
        for (int i = 0; i < paths.length; i++) {
            try {
                layers[i] = ImageIO.read(new File(paths[i]));
            } catch (Exception e) {
                System.out.println("Could not load parallax layer " + paths[i] + ": " + e.getMessage());
            }
        }

        for (int i = 0; i < layers.length; i++) {
            if (layers[i] == null) continue;
            if (i == 0) {
                yOffsets[i] = 0;
            } else {
                int h = scaledHeight(layers[i]);
                yOffsets[i] = screenH - h;
            }
        }
    }

    private int scaledHeight(BufferedImage img) {
        return (int) Math.round(img.getHeight() * (screenW / (double) img.getWidth()));
    }

    public void draw(Graphics2D g2, int cameraX) {
        for (int i = 0; i < layers.length; i++) {
            BufferedImage img = layers[i];
            if (img == null) continue;

            int drawW = screenW;
            int drawH;
            if (i == 0) {
                drawH = screenH;
            } else {
                drawH = scaledHeight(img);
            }
            int y = yOffsets[i];

            int shift = (int) (cameraX * speeds[i]);
            int offset = -(shift % drawW);
            if (offset > 0) offset -= drawW;

            for (int x = offset; x < screenW; x += drawW) {
                g2.drawImage(img, x, y, drawW, drawH, null);
            }
        }
    }
}
