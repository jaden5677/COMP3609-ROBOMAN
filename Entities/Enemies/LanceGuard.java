package Entities.Enemies;

import java.awt.Color;
import java.awt.image.BufferedImage;

import Entities.Player.Player;
import Entities.Projectiles.Projectile;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

/**
 * LanceGuard - Stationary guard enemy that chases the player when in
 * detection range and retreats when the player leaves. Can parry light
 * projectiles (40 % chance) and deals melee damage up close. Worth 300 points.
 */
public class LanceGuard extends Enemy {

    private static final int POINTS = 300;
    private static final int HEALTH = 60;
    private static final int CHASE_SPEED = 3;
    private static final int RETREAT_SPEED = 2;
    private static final int DETECT_RANGE = 160;
    private static final int ATTACK_RANGE = 40;
    private static final int RETREAT_RANGE = 200;
    private static final int ATTACK_COOLDOWN_TICKS = 30;
    private static final int ATTACK_DAMAGE = 20;
    private static final int PARRY_CHANCE_PERCENT = 40;

    private int guardX; // original guard position
    private int attackCooldown;

    public LanceGuard(int x, int y, Player player, Level level) {
        super(x, y, 90, 96, HEALTH, POINTS, player, level);
        this.guardX = x;
        this.attackCooldown = 0;
        this.fallbackColor = Color.BLUE;
        this.animSpeed = 6;
        this.affectedByGravity = true;
        loadSprites();
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("SpriteSheets/LanceGaurd.png");
            if (sheet != null) {
                // LanceGaurd.png is 512x512: 3 cols x 3 rows (row 2 mostly empty)
                int fw = 170;
                int fh = 170;
                BufferedImage[] row0 = ext.extractRow(sheet, 0, 3, fw, fh);
                BufferedImage[] row1 = ext.extractRow(sheet, 1, 3, fw, fh);
                animFrames = new BufferedImage[6];
                System.arraycopy(row0, 0, animFrames, 0, 3);
                System.arraycopy(row1, 0, animFrames, 3, 3);
            }
        } catch (Exception e) {
            System.out.println("Could not load LanceGuard sprites: " + e.getMessage());
        }
    }

    @Override
    public void takeDamage(int dmg, Projectile.Type attackType) {
        if (!alive) return;
        // Parry light projectiles
        if (attackType == Projectile.Type.PLAYER_LIGHT) {
            if (Math.random() * 100 < PARRY_CHANCE_PERCENT) {
                return; // parried
            }
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
            // Melee attack
            if (attackCooldown <= 0) {
                player.takeDamage(ATTACK_DAMAGE);
                attackCooldown = ATTACK_COOLDOWN_TICKS;
            }
        } else if (dist <= DETECT_RANGE) {
            // Chase the player
            int dir = player.getX() > x ? 1 : -1;
            int newX = x + dir * CHASE_SPEED;
            boolean floorBelow = level.isSolid(newX + width / 2, y + height + 1);
            boolean wallAhead  = level.isSolid(newX + (dir > 0 ? width : 0), y) ||
                                 level.isSolid(newX + (dir > 0 ? width : 0), y + height - 1);
            if (floorBelow && !wallAhead) {
                x = newX;
            }
        } else if (dist > RETREAT_RANGE && Math.abs(x - guardX) > 10) {
            // Return to guard position
            int dir = guardX > x ? 1 : -1;
            x += dir * RETREAT_SPEED;
        }

        animate();
    }
}
