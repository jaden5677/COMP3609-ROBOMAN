package Entities;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public interface EntityInterface {
    void update();
    void draw(Graphics2D g2);

    int getX();
    int getY();
    int getWidth();
    int getHeight();

    boolean isVisible();
    void setVisible(boolean visible);

    Rectangle2D.Double getBoundingRectangle();
}
