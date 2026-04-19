package Entities;

import java.awt.Color;
import java.awt.Graphics2D;
import MainGame.Level;

public class Projectile extends AbstractEntity {

    public enum Type {
        PLAYER_LIGHT,
        PLAYER_HEAVY,
        ENEMY_NORMAL,
        ENEMY_ELECTRIC
    }

    private Type type;
    private int projectileDamage;
    private int lifetime;
    private static final int MAX_LIFETIME = 100;

    public Projectile(int x, int y, int dx, int dy, Type type, int damage) {
        super(x, y, 24, 24);
        this.dx = dx;
        this.dy = dy;
        this.type = type;
        this.projectileDamage = damage;
        this.lifetime = MAX_LIFETIME;
    }

    @Override
    public void update() {
        x += dx;
        y += dy;
        lifetime--;
        if (lifetime <= 0) {
            isVisible = false;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!isVisible) return;
        Color color;
        switch (type) {
            case PLAYER_LIGHT:   color = Color.YELLOW; break;
            case PLAYER_HEAVY:   color = Color.ORANGE; break;
            case ENEMY_NORMAL:   color = Color.RED;    break;
            case ENEMY_ELECTRIC: color = Color.CYAN;   break;
            default:             color = Color.WHITE;
        }
        g2.setColor(color);
        g2.fillOval(x, y, width, height);
    }

    public boolean isActive() {
        return isVisible && lifetime > 0;
    }

    public boolean collidesWithTile(Level level) {
        return level.isSolid(x, y) || level.isSolid(x + width, y + height);
    }

    public Type getType() { return type; }
    public int getProjectileDamage() { return projectileDamage; }
}
