package Entities.Enemies;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Entities.Player.Player;
import Entities.Projectiles.Projectile;
import ImageManager.SpriteSheetExtractor;

import MainGame.Level;

public class Thundorb extends Enemy {

    private static final int POINTS = 350;
    private static final int HEALTH = 40;
    private static final int MAX_CHARGES = 4;
    private static final int RELOAD_TICKS = 60;
    private static final int SHOOT_INTERVAL_TICKS = 15;
    private static final int STUN_RANGE = 40;
    private static final int STUN_DURATION_MS = 2000;
    private static final int SHOOT_RANGE = 200;
    private static final int RETREAT_SPEED = 3;
    private static final int ORB_DAMAGE = 5;
    private static final int LOSE_CHARGE_TICKS = 18;

    private int charges;
    private int reloadTimer;
    private int shootTimer;
    private boolean retreating;
    private int floatTick;
    private int baseY;

    private enum AnimState { POWERED, LOSING_CHARGE, RETREATING }
    private AnimState animState;
    private int loseChargeTimer;

    private BufferedImage[] poweredRightFrames;
    private BufferedImage[] poweredLeftFrames;
    private BufferedImage[] loseChargeRightFrames;
    private BufferedImage[] loseChargeLeftFrames;
    private BufferedImage[] retreatFrames;
    private BufferedImage projectileSprite;

    public Thundorb(int x, int y, Player player, Level level) {
        super(x, y, 72, 72, HEALTH, POINTS, player, level);
        this.charges = MAX_CHARGES;
        this.reloadTimer = 0;
        this.shootTimer = 0;
        this.retreating = false;
        this.floatTick = 0;

        this.baseY = clampAboveSolids(level, x, y);
        this.y = baseY;
        this.fallbackColor = Color.CYAN;
        this.animSpeed = 8;
        this.affectedByGravity = false;
        this.animState = AnimState.POWERED;
        this.loseChargeTimer = 0;
        loadSprites();
    }

    private int clampAboveSolids(Level level, int spawnX, int spawnY) {
        int probeXLeft  = spawnX + 4;
        int probeXRight = spawnX + width - 4;
        int floatPad    = 8;
        int bottom      = spawnY + height + floatPad;
        int maxScan     = Level.TILE_SIZE * 4;
        for (int dy = 0; dy < maxScan; dy += 4) {
            int b = bottom + dy;
            if (level.isSolid(probeXLeft, b) || level.isSolid(probeXRight, b)) {
                int tileTop = (b / Level.TILE_SIZE) * Level.TILE_SIZE;
                return tileTop - height - floatPad - 1;
            }
        }
        return spawnY;
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("Spritesheets/Thundorb.png");
            if (sheet != null) {
                int fw = 56;
                int fh = 56;

                poweredRightFrames = new BufferedImage[] {
                    ext.extractSprite(sheet, 0, 0, fw, fh),
                    ext.extractSprite(sheet, fw, 0, fw, fh)
                };
                poweredLeftFrames = new BufferedImage[] {
                    ext.extractSprite(sheet, 2 * fw, 0, fw, fh),
                    ext.extractSprite(sheet, 3 * fw, 0, fw, fh)
                };

                loseChargeRightFrames = ext.extractRow(sheet, 1, 3, fw, fh);

                loseChargeLeftFrames = ext.extractRow(sheet, 2, 3, fw, fh);

                retreatFrames = new BufferedImage[] {
                    ext.extractSprite(sheet, 0, 3 * fh, fw, fh),
                    ext.extractSprite(sheet, fw, 3 * fh, fw, fh)
                };
                projectileSprite = ext.extractSprite(sheet, 3 * fw, 3 * fh, fw, fh);

                animFrames = poweredRightFrames;
            }
        } catch (Exception e) {
            System.out.println("Could not load Thundorb sprites: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (!alive) return;

        floatTick++;
        y = baseY + (int)(Math.sin(floatTick * 0.1) * 4);

        if (animState == AnimState.LOSING_CHARGE) {
            loseChargeTimer--;
            if (loseChargeTimer <= 0) {
                if (charges <= 0) {
                    animState = AnimState.RETREATING;
                    retreating = true;
                    reloadTimer = RELOAD_TICKS;
                } else {
                    animState = AnimState.POWERED;
                }
            }
            animate();
            updateAnimFrames();
            return;
        }

        if (distanceToPlayer() <= STUN_RANGE && !player.isStunned()) {
            player.stun(STUN_DURATION_MS);
            spendCharge();
        }

        if (retreating) {

            int dir;
            if (player.getX() > x) {
                dir = -1;
            } else {
                dir = 1;
            }
            x += dir * RETREAT_SPEED;
            reloadTimer--;
            if (reloadTimer <= 0) {
                charges = MAX_CHARGES;
                retreating = false;
                animState = AnimState.POWERED;
            }
        } else if (charges > 0 && playerInRange(SHOOT_RANGE)) {
            if (shootTimer <= 0) {
                shootAtPlayer();
                spendCharge();
                shootTimer = SHOOT_INTERVAL_TICKS;
            }
        }

        if (shootTimer > 0) shootTimer--;

        facePlayer();
        updateAnimFrames();
        animate();
    }

    private void spendCharge() {
        charges--;
        animState = AnimState.LOSING_CHARGE;
        loseChargeTimer = LOSE_CHARGE_TICKS;
        currentFrame = 0;
        animTick = 0;
        updateAnimFrames();
    }

    private void updateAnimFrames() {
        BufferedImage[] target = null;
        switch (animState) {
            case POWERED:
                if (facingRight) {
                    target = poweredRightFrames;
                } else {
                    target = poweredLeftFrames;
                }
                break;
            case LOSING_CHARGE:
                if (facingRight) {
                    target = loseChargeRightFrames;
                } else {
                    target = loseChargeLeftFrames;
                }
                break;
            case RETREATING:
                target = retreatFrames;
                break;
        }
        if (target != null && target != animFrames) {
            animFrames = target;
            currentFrame = 0;
            animTick = 0;
        }
    }

    private void shootAtPlayer() {
        int px = player.getX() + player.getWidth() / 2;
        int py = player.getY() + player.getHeight() / 2;
        int ox = x + width / 2;
        int oy = y + height / 2;
        double angle = Math.atan2(py - oy, px - ox);
        int speed = 5;
        int pdx = (int)(Math.cos(angle) * speed);
        int pdy = (int)(Math.sin(angle) * speed);
        projectiles.add(new Projectile(ox - 12, oy - 12, pdx, pdy,
            Projectile.Type.ENEMY_ELECTRIC, ORB_DAMAGE, projectileSprite));
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
