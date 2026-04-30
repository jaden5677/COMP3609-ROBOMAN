package MainGame;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class MenuScreen {

    private final ParallaxBackground background;
    private int scroll;

    public MenuScreen(ParallaxBackground background) {
        this.background = background;
    }

    public void update() {
        scroll += 3;
    }

    public void draw(Graphics2D g2, int screenW, int screenH) {
        if (background != null) {
            background.draw(g2, scroll);
        } else {
            g2.setColor(new Color(20, 20, 40));
            g2.fillRect(0, 0, screenW, screenH);
        }

        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRect(0, 0, screenW, screenH);

        drawCentered(g2, "ROBOMAN", screenH / 4,
            new Font("Arial", Font.BOLD, 96), new Color(255, 230, 120), screenW);
        drawCentered(g2, "SELECT A LEVEL", screenH / 4 + 90,
            new Font("Arial", Font.BOLD, 28), new Color(220, 220, 220), screenW);

        int y0 = screenH / 2 + 30;
        drawCentered(g2, "[ 1 ]   LEVEL 1  -  Plains Arena", y0,
            new Font("Arial", Font.BOLD, 36), Color.WHITE, screenW);
        drawCentered(g2, "[ 2 ]   LEVEL 2  -  Boss Chamber", y0 + 56,
            new Font("Arial", Font.BOLD, 36), Color.WHITE, screenW);

        drawCentered(g2, "Press the number key for the level you want to play.",
            screenH - 80, new Font("Arial", Font.PLAIN, 18),
            new Color(200, 200, 200), screenW);
        drawCentered(g2, "WASD/Arrows to move - SPACE jump - F shoot - P pause - ESC menu",
            screenH - 50, new Font("Arial", Font.PLAIN, 16),
            new Color(170, 170, 170), screenW);
    }

    private static void drawCentered(Graphics2D g2, String text, int y,
                                     Font font, Color color, int screenW) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int x = (screenW - fm.stringWidth(text)) / 2;
        g2.setColor(Color.BLACK);
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(color);
        g2.drawString(text, x, y);
    }
}
