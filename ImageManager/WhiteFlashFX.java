package ImageManager;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.Map;

public class WhiteFlashFX {

    public static final long DEFAULT_DURATION_MS = 140L;

    private final long durationMs;
    private long flashStartMs = -1L;

    private final Map<BufferedImage, BufferedImage> whiteCache = new IdentityHashMap<>();

    public WhiteFlashFX() { this(DEFAULT_DURATION_MS); }

    public WhiteFlashFX(long durationMs) {
        this.durationMs = durationMs;
    }

    public void trigger() { trigger(durationMs); }

    public void trigger(long ms) {
        flashStartMs = System.currentTimeMillis();
    }

    public boolean isActive() {
        return flashStartMs >= 0
            && System.currentTimeMillis() - flashStartMs < durationMs;
    }

    public void draw(Graphics2D g2, BufferedImage frame,
                     int dx, int dy, int dw, int dh) {
        if (!isActive() || frame == null) return;
        long elapsed = System.currentTimeMillis() - flashStartMs;
        float alpha = 1f - (elapsed / (float) durationMs);
        if (alpha <= 0f) return;
        if (alpha > 1f) alpha = 1f;
        BufferedImage white = getWhiteSilhouette(frame);
        Composite prev = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.drawImage(white, dx, dy, dw, dh, null);
        g2.setComposite(prev);
    }

    private BufferedImage getWhiteSilhouette(BufferedImage src) {
        BufferedImage cached = whiteCache.get(src);
        if (cached != null) return cached;
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[w * h];
        src.getRGB(0, 0, w, h, pixels, 0, w);
        for (int i = 0; i < pixels.length; i++) {
            int a = (pixels[i] >> 24) & 0xFF;

            pixels[i] = (a << 24) | 0x00FFFFFF;
        }
        out.setRGB(0, 0, w, h, pixels, 0, w);
        whiteCache.put(src, out);
        return out;
    }
}
