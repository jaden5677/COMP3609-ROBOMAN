package MainGame;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class LevelCompleteOverlay {

    public void draw(Graphics2D g2, int screenW, int screenH,
                     String headline, String subtitle) {

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, screenW, screenH);

        drawCentered(g2, headline, screenH / 2 - 40,
            new Font("Arial", Font.BOLD, 72), new Color(120, 255, 140), screenW);
        drawCentered(g2, subtitle, screenH / 2 + 30,
            new Font("Arial", Font.PLAIN, 28), Color.WHITE, screenW);
    }

    private static void drawCentered(Graphics2D g2, String text, int y,
                                     Font font, Color color, int screenW) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int x = (screenW - fm.stringWidth(text)) / 2;
        g2.setColor(Color.BLACK);
        g2.drawString(text, x + 3, y + 3);
        g2.setColor(color);
        g2.drawString(text, x, y);
    }
}
