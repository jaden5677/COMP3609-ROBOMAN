package Entities.Enemies;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Entities.Player.Player;
import Entities.Projectiles.Projectile;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

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

    private BufferedImage defendRight, floatRight, biteStartRight, biteEndRight;
    private BufferedImage defendLeft, floatLeft, biteStartLeft, biteEndLeft;
    private boolean biting;
    private int biteTick;
    private static final int BITE_ANIM_TICKS = 8;

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

                defendRight   = ext.extractSprite(sheet, 0 * fw, 0, fw, fh);
                floatRight    = ext.extractSprite(sheet, 1 * fw, 0, fw, fh);
                biteEndRight  = ext.extractSprite(sheet, 2 * fw, 0, fw, fh);
                biteStartRight = ext.extractSprite(sheet, 3 * fw, 0, fw, fh);

                defendLeft    = ext.extractSprite(sheet, 0 * fw, fh, fw, fh);
                floatLeft     = ext.extractSprite(sheet, 1 * fw, fh, fw, fh);
                biteEndLeft   = ext.extractSprite(sheet, 2 * fw, fh, fw, fh);
                biteStartLeft = ext.extractSprite(sheet, 3 * fw, fh, fw, fh);

                animFrames = new BufferedImage[]{ defendRight };
            }
        } catch (Exception e) {
            System.out.println("Could not load MetalChomp sprites: " + e.getMessage());
        }
    }

    @Override
    public void takeDamage(int dmg, Projectile.Type attackType) {
        if (!alive) return;

        if (attackType == Projectile.Type.PLAYER_LIGHT) {
            super.takeDamage(dmg/2, attackType);
            return;
        }

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

        if (biting) {
            biteTick++;
            if (biteTick < BITE_ANIM_TICKS) {

                if (facingRight) {
                    animFrames = new BufferedImage[]{ biteStartRight };
                } else {
                    animFrames = new BufferedImage[]{ biteStartLeft };
                }
            } else if (biteTick < BITE_ANIM_TICKS * 2) {

                if (facingRight) {
                    animFrames = new BufferedImage[]{ biteEndRight };
                } else {
                    animFrames = new BufferedImage[]{ biteEndLeft };
                }
            } else {

                biting = false;
                biteTick = 0;
                if (facingRight) {
                    animFrames = new BufferedImage[]{ defendRight };
                } else {
                    animFrames = new BufferedImage[]{ defendLeft };
                }
            }
            return;
        }

        if (dist <= ATTACK_RANGE) {

            defenseMode = false;
            if (attackCooldown <= 0) {
                player.takeDamage(ATTACK_DAMAGE);
                attackCooldown = ATTACK_COOLDOWN_TICKS;
                biting = true;
                biteTick = 0;
                if (facingRight) {
                    animFrames = new BufferedImage[]{ biteStartRight };
                } else {
                    animFrames = new BufferedImage[]{ biteStartLeft };
                }
            } else {
                if (facingRight) {
                    animFrames = new BufferedImage[]{ defendRight };
                } else {
                    animFrames = new BufferedImage[]{ defendLeft };
                }
            }
        } else if (dist <= DETECT_RANGE) {

            defenseMode = true;
            int dir;
            if (player.getX() > x) {
                dir = 1;
            } else {
                dir = -1;
            }
            int newX = x + dir * CHASE_SPEED;
            int probeX;
            if (dir > 0) {
                probeX = newX + width;
            } else {
                probeX = newX;
            }
            boolean floorAhead = level.isSolid(
                probeX, y + height + 1);
            boolean wallAhead = level.isSolid(
                probeX, y) ||
                level.isSolid(
                    probeX, y + height - 1);
            if (floorAhead && !wallAhead) {
                x = newX;
            }
            if (facingRight) {
                animFrames = new BufferedImage[]{ floatRight };
            } else {
                animFrames = new BufferedImage[]{ floatLeft };
            }
        } else {

            defenseMode = true;
            if (facingRight) {
                animFrames = new BufferedImage[]{ defendRight };
            } else {
                animFrames = new BufferedImage[]{ defendLeft };
            }
        }

        animate();
    }

    @Override
    protected void drawSelf(Graphics2D g) {
        BufferedImage frame = getCurrentFrame();
        if (frame == null) { super.drawSelf(g); return; }
        int fw = frame.getWidth();
        int fh = frame.getHeight();
        double scale = Math.min((double) width / fw, (double) height / fh);
        int drawW = (int)(fw * scale);
        int drawH = (int)(fh * scale);
        int offsetX = (width - drawW) / 2;
        int offsetY = height - drawH;
        g.drawImage(frame, x + offsetX, y + offsetY, drawW, drawH, null);
    }
}
