package Entities;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * Common base for every drawable, updatable thing in the world.
 * Lets the game keep a single Collection&lt;EntityInterface&gt; for
 * uniform update / draw passes while still allowing each category
 * (Player / Enemy / Item) to expose its own richer interface for
 * category-specific interactions.
 */
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
