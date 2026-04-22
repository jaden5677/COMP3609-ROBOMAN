package MainGame;

import Entities.Player.Player;

public class Camera {

    private int x;
    private int y;
    private int screenWidth;
    private int screenHeight;

    public Camera(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void follow(Player player, int mapPixelWidth, int mapPixelHeight) {
        // Centre on player
        x = player.getX() + player.getWidth() / 2 - screenWidth / 2;
        y = player.getY() + player.getHeight() / 2 - screenHeight / 2;

        // Clamp to map bounds
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        int maxX = mapPixelWidth - screenWidth;
        int maxY = mapPixelHeight - screenHeight;
        if (maxX > 0 && x > maxX) x = maxX;
        if (maxY > 0 && y > maxY) y = maxY;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
