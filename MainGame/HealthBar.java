package MainGame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import Entities.Player.Player;

public class HealthBar {

    private static final int BAR_WIDTH = 450;
    private static final int BAR_HEIGHT = 60;
    private static final int X = 30;
    private static final int Y = 30;

    public void draw(Graphics2D g2, Player player) {

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(X, Y, BAR_WIDTH, BAR_HEIGHT);

        float pct = (float) player.getHealth() / player.getMaxHealth();
        Color fill;
        if (pct > 0.5f) {
            fill = Color.GREEN;
        } else if (pct > 0.25f) {
            fill = Color.YELLOW;
        } else {
            fill = Color.RED;
        }
        g2.setColor(fill);
        g2.fillRect(X, Y, (int)(BAR_WIDTH * pct), BAR_HEIGHT);

        g2.setColor(Color.WHITE);
        g2.drawRect(X, Y, BAR_WIDTH, BAR_HEIGHT);

        g2.setFont(new Font("Arial", Font.BOLD, 36));
        g2.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(),
                       X + 15, Y + 45);

        g2.drawString("Lives: " + player.getLives()
                     + "  Deaths: " + player.getDeathsInLevel() + "/3",
                       X, Y + BAR_HEIGHT + 45);
    }
}
