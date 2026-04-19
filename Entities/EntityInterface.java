package Entities;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public interface EntityInterface {
    void draw(Graphics2D g2);
    int getX();
    int getY();
    void update();
    Rectangle2D.Double getBoundingRectangle();
}
