package Entities;

import java.awt.Color;
import java.awt.image.BufferedImage;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

/**
 * MetalChomp - Aggressive chaser enemy that moves only along the x-axis.
 * Hard outer shell: immune to light attacks; heavy attacks do half damage
 * when in defense mode. Short-range bite attack. Worth 400 points.
 */
public class MetalChomp extends Enemy {

    private static final int POINTS = 400;
    private static final int HEALTH = 80;
    private static final int CHASE_SPEED = 3;
    private static final int DETECT_RANGE = 200;
    private static final int ATTACK_RANGE = 32;
    private static final int ATTACK_COOLDOWN_TICKS = 25;
    private static final int ATTACK_DAMAGE = 15;

    private boolean defenseMode;
    private int attackCooldown;

    public MetalChomp(int x, int y, Player player, Level level) {
        super(x, y, 84, 84, HEALTH, POINTS, player, level);
        this.defenseMode = true;
        this.attackCooldown = 0;
        this.fallbackColor = new Color(0, 128, 0);
        this.animSpeed = 4;
        this.affectedByGravity = true;
        loadSprites();
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("SpriteSheets/MetalChomp.png");
            if (sheet != null) {
                // MetalChomp.png is 512x512: 2 cols x 3 rows (256x170)
                int fw = 256;
                int fh = 170;
                animFrames = ext.extractRow(sheet, 0, 2, fw, fh);
            }
        } catch (Exception e) {
            System.out.println("Could not load MetalChomp sprites: " + e.getMessage());
        }
    }

    @Override
    public void takeDamage(int dmg, Projectile.Type attackType) {
        if (!alive) return;
        // Immune to light attacks
        if (attackType == Projectile.Type.PLAYER_LIGHT) {
            return;
        }
        // Heavy attacks do half damage while in defense mode
        if (attackType == Projectile.Type.PLAYER_HEAVY && defenseMode) {
            super.takeDamage(dmg / 2, attackType);
            return;
        }
        super.takeDamage(dmg, attackType);
    }

    @Override
    public void update() {
        if (!alive) return;

        applyGravity();

        double dist = distanceToPlayer();
        facePlayer();

        if (attackCooldown > 0) attackCooldown--;

        if (dist <= ATTACK_RANGE) {
            // Short-range bite
            defenseMode = false;
            if (attackCooldown <= 0) {
                player.takeDamage(ATTACK_DAMAGE);
                attackCooldown = ATTACK_COOLDOWN_TICKS;
            }
        } else if (dist <= DETECT_RANGE) {
            // Chase on x-axis only
            defenseMode = true;
            int dir = player.getX() > x ? 1 : -1;
            int newX = x + dir * CHASE_SPEED;
            boolean floorAhead = level.isSolid(
                newX + (dir > 0 ? width : 0), y + height + 1);
            boolean wallAhead = level.isSolid(
                newX + (dir > 0 ? width : 0), y) ||
                level.isSolid(
                    newX + (dir > 0 ? width : 0), y + height - 1);
            if (floorAhead && !wallAhead) {
                x = newX;
            }
        } else {
            defenseMode = true;
        }

        animate();
    }
}
