package Entities.Enemies;

import java.awt.Color;
import java.awt.image.BufferedImage;

import Entities.Player.Player;
import Entities.Projectiles.Projectile;
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

    // Animation
    private BufferedImage defendRight, floatRight, biteStartRight, biteEndRight;
    private BufferedImage defendLeft, floatLeft, biteStartLeft, biteEndLeft;
    private boolean biting;
    private int biteTick;
    private static final int BITE_ANIM_TICKS = 8; // ticks per bite frame

    public MetalChomp(int x, int y, Player player, Level level) {
        super(x, y, 126, 126, HEALTH, POINTS, player, level);
        this.defenseMode = true;
        this.attackCooldown = 0;
        this.biting = false;
        this.biteTick = 0;
        this.fallbackColor = new Color(0, 128, 0);
        this.animSpeed = 4;
        this.affectedByGravity = true;
        loadSprites();
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("Spritesheets/MetalChomp.png");
            if (sheet != null) {
                int fw = 32;
                int fh = 32;
                // Row 0: facing right
                defendRight   = ext.extractSprite(sheet, 0 * fw, 0, fw, fh);
                floatRight    = ext.extractSprite(sheet, 1 * fw, 0, fw, fh);
                biteEndRight  = ext.extractSprite(sheet, 2 * fw, 0, fw, fh);
                biteStartRight = ext.extractSprite(sheet, 3 * fw, 0, fw, fh);
                // Row 1: facing left
                defendLeft    = ext.extractSprite(sheet, 0 * fw, fh, fw, fh);
                floatLeft     = ext.extractSprite(sheet, 1 * fw, fh, fw, fh);
                biteEndLeft   = ext.extractSprite(sheet, 2 * fw, fh, fw, fh);
                biteStartLeft = ext.extractSprite(sheet, 3 * fw, fh, fw, fh);
                // Default: defend right
                animFrames = new BufferedImage[]{ defendRight };
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
            super.takeDamage(dmg/2, attackType);
            return;
        }
        // Heavy attacks do half damage while in defense mode
        if (attackType == Projectile.Type.PLAYER_HEAVY && defenseMode) {
            super.takeDamage(dmg, attackType);
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

        // Handle bite animation
        if (biting) {
            biteTick++;
            if (biteTick < BITE_ANIM_TICKS) {
                // First frame: bite start
                animFrames = new BufferedImage[]{ facingRight ? biteStartRight : biteStartLeft };
            } else if (biteTick < BITE_ANIM_TICKS * 2) {
                // Second frame: bite end
                animFrames = new BufferedImage[]{ facingRight ? biteEndRight : biteEndLeft };
            } else {
                // Bite over — revert to defend
                biting = false;
                biteTick = 0;
                animFrames = new BufferedImage[]{ facingRight ? defendRight : defendLeft };
            }
            return;
        }

        if (dist <= ATTACK_RANGE) {
            // Short-range bite
            defenseMode = false;
            if (attackCooldown <= 0) {
                player.takeDamage(ATTACK_DAMAGE);
                attackCooldown = ATTACK_COOLDOWN_TICKS;
                biting = true;
                biteTick = 0;
                animFrames = new BufferedImage[]{ facingRight ? biteStartRight : biteStartLeft };
            } else {
                animFrames = new BufferedImage[]{ facingRight ? defendRight : defendLeft };
            }
        } else if (dist <= DETECT_RANGE) {
            // Chase on x-axis only — show float sprite
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
            animFrames = new BufferedImage[]{ facingRight ? floatRight : floatLeft };
        } else {
            // Idle — defend
            defenseMode = true;
            animFrames = new BufferedImage[]{ facingRight ? defendRight : defendLeft };
        }

        animate();
    }
}
