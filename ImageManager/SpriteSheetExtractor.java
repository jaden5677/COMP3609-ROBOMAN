package ImageManager;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
            return img;
        }

        int tw = right - left + 1;
        int th = bottom - top + 1;
        return img.getSubimage(left, top, tw, th);
    }

    public BufferedImage[] extractRowTrimmed(BufferedImage sheet, int row, int numFrames,
                                             int spriteWidth, int spriteHeight) {
        BufferedImage[] frames = extractRow(sheet, row, numFrames, spriteWidth, spriteHeight);
        for (int i = 0; i < frames.length; i++) {
            frames[i] = trimTransparent(frames[i]);
        }
        return frames;
    }

    public BufferedImage[] extractRowAt(BufferedImage sheet, int yOffset, int numFrames,
                                        int spriteWidth, int spriteHeight) {
        BufferedImage[] frames = new BufferedImage[numFrames];
        for (int i = 0; i < numFrames; i++) {
            frames[i] = sheet.getSubimage(i * spriteWidth, yOffset, spriteWidth, spriteHeight);
        }
        return frames;
    }

    public BufferedImage[] loadGifFrames(String filePath) {
        ImageInputStream iis = null;
        ImageReader reader = null;
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                System.out.println("GIF file not found: " + filePath);
                return null;
            }
            iis = ImageIO.createImageInputStream(f);
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) return null;
            reader = readers.next();
            reader.setInput(iis, false);

            int n = reader.getNumImages(true);

            int canvasW = 0, canvasH = 0;
            int[] leftPos     = new int[n];
            int[] topPos      = new int[n];
            int[] fws         = new int[n];
            int[] fhs         = new int[n];
            String[] disposal = new String[n];
            for (int i = 0; i < n; i++) {
                fws[i] = reader.getWidth(i);
                fhs[i] = reader.getHeight(i);
                IIOMetadata md = reader.getImageMetadata(i);
                int[] off = readGifFrameOffset(md);
                leftPos[i] = off[0];
                topPos[i]  = off[1];
                disposal[i] = readGifDisposal(md);
                if (off[0] + fws[i] > canvasW) canvasW = off[0] + fws[i];
                if (off[1] + fhs[i] > canvasH) canvasH = off[1] + fhs[i];
            }

            BufferedImage[] frames = new BufferedImage[n];
            BufferedImage canvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();

            g.setComposite(java.awt.AlphaComposite.Src);

            for (int i = 0; i < n; i++) {
                BufferedImage frame = reader.read(i);

                BufferedImage snapshot = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D sg = snapshot.createGraphics();
                sg.setComposite(java.awt.AlphaComposite.Src);
                sg.drawImage(canvas, 0, 0, null);
                sg.dispose();

                g.drawImage(frame, leftPos[i], topPos[i], null);

                BufferedImage copy = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D cg = copy.createGraphics();
                cg.setComposite(java.awt.AlphaComposite.Src);
                cg.drawImage(canvas, 0, 0, null);
                cg.dispose();
                frames[i] = copy;

                String d;
                if (disposal[i] == null) {
                    d = "none";
                } else {
                    d = disposal[i];
                }
                if ("restoreToBackgroundColor".equals(d)) {
                    g.setComposite(java.awt.AlphaComposite.Clear);
                    g.fillRect(leftPos[i], topPos[i], fws[i], fhs[i]);
                    g.setComposite(java.awt.AlphaComposite.Src);
                } else if ("restoreToPrevious".equals(d)) {
                    g.setComposite(java.awt.AlphaComposite.Clear);
                    g.fillRect(0, 0, canvasW, canvasH);
                    g.setComposite(java.awt.AlphaComposite.Src);
                    g.drawImage(snapshot, 0, 0, null);
                }

            }
            g.dispose();
            return frames;
        } catch (Exception e) {
            System.out.println("Could not load GIF frames: " + filePath + " - " + e.getMessage());
            return null;
        } finally {
            if (reader != null) reader.dispose();
            try { if (iis != null) iis.close(); } catch (Exception ignore) {}
        }
    }

    private String readGifDisposal(IIOMetadata md) {
        try {
            Node tree = md.getAsTree("javax_imageio_gif_image_1.0");
            NodeList children = tree.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if ("GraphicControlExtension".equals(child.getNodeName())) {
                    NamedNodeMap attrs = child.getAttributes();
                    Node dn = attrs.getNamedItem("disposalMethod");
                    if (dn != null) return dn.getNodeValue();
                }
            }
        } catch (Exception ignore) { }
        return "none";
    }

    private int[] readGifFrameOffset(IIOMetadata md) {
        int left = 0, top = 0;
        try {
            Node tree = md.getAsTree("javax_imageio_gif_image_1.0");
            NodeList children = tree.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if ("ImageDescriptor".equals(child.getNodeName())) {
                    NamedNodeMap attrs = child.getAttributes();
                    Node ln = attrs.getNamedItem("imageLeftPosition");
                    Node tn = attrs.getNamedItem("imageTopPosition");
                    if (ln != null) left = Integer.parseInt(ln.getNodeValue());
                    if (tn != null) top  = Integer.parseInt(tn.getNodeValue());
                }
            }
        } catch (Exception ignore) { }
        return new int[] { left, top };
    }
}
