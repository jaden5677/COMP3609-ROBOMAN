package Entities.Enemies;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.Map;

import Entities.Player.Player;
import Entities.Projectiles.Projectile;
import ImageManager.SpriteSheetExtractor;
import MainGame.Level;

public class Boss extends Enemy {

    public static final int DRAW_WIDTH  = 320;
    public static final int DRAW_HEIGHT = 188;

    private static final int HP                  = 800;
    private static final int POINTS              = 5000;
    private static final int ATTACK_DAMAGE       = 80;
    private static final int ATTACK_RANGE        = 200;

    private static final int VISION_RANGE        = 3000;

    private static final long RANGED_TRIGGER_MS  = 5000;
    private static final int WALK_SPEED          = 2;
    private static final int SHOCKWAVE_DAMAGE    = 50;
    private static final int SHOCKWAVE_SPEED     = 4;
    private static final int SHOCKWAVE_SIZE      = 64;
    private static final long ATTACK_COOLDOWN_MS = 4500;
    private static final int  ANIM_SPEED         = 4;

    private static final int  ATTACK_HIT_FRAME   = 20;

    private static final int  MAX_BOSS_FALL      = 16;

    private static final int SRC_FW = 320;
    private static final int SRC_FH = 188;

    private BufferedImage[] introFrames;
    private BufferedImage[] walkFrames;
    private BufferedImage[] idleFrames;
    private BufferedImage[] attackFrames;
    private BufferedImage[] deathFrames;

    private int bossFallSpeed;

    private enum State { INTRO, WALK, IDLE, ATTACK, DEATH }
    private State state;
    private boolean introPlayed;
    private boolean meleeHitDelivered;
    private boolean shockwaveSpawned;
    private long lastAttackTime;

    private long visionStartTime = -1;

    private static final long DEATH_FADE_MS = 3000L;

    private boolean deathAnimComplete = false;

    private long deathFadeStart = -1L;

    private final Map<BufferedImage, Rectangle> hitboxCache = new IdentityHashMap<>();

    private final Map<BufferedImage, BufferedImage> grayscaleCache = new IdentityHashMap<>();

    public Boss(int x, int y, Player player, Level level) {
        super(x, y, DRAW_WIDTH, DRAW_HEIGHT, HP, POINTS, player, level);
        this.fallbackColor     = new Color(120, 0, 60);
        this.affectedByGravity = true;
        this.animSpeed         = ANIM_SPEED;
        this.lastAttackTime    = -ATTACK_COOLDOWN_MS;
        this.state             = State.IDLE;
        this.introPlayed       = false;
        loadSprites();
        applyStateFrames();
    }

    private void loadSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage[] all = ext.loadGifFrames("Spritesheets/BossGif.gif");
            if (all == null || all.length < 98) {
                int loadedFrameCount;
                if (all == null) {
                    loadedFrameCount = 0;
                } else {
                    loadedFrameCount = all.length;
                }
                System.out.println("Boss GIF returned "
                    + loadedFrameCount + " frames; expected 98.");

                introPlayed = true;
                return;
            }
            introFrames  = sliceAndCrop(all,  0, 13);
            walkFrames   = sliceAndCrop(all, 13, 25);
            idleFrames   = sliceAndCrop(all, 38, 18);
            attackFrames = sliceAndCrop(all, 56, 28);
            deathFrames  = sliceAndCrop(all, 84, 14);
        } catch (Exception e) {
            System.out.println("Could not load Boss sprites: " + e.getMessage());
            introPlayed = true;
        }
    }

    private static BufferedImage[] sliceAndCrop(BufferedImage[] all, int start, int count) {
        BufferedImage[] out = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            BufferedImage f = all[start + i];
            int cw = Math.min(SRC_FW, f.getWidth());
            int ch = Math.min(SRC_FH, f.getHeight());
            try {
                out[i] = f.getSubimage(0, 0, cw, ch);
            } catch (Exception ex) {
                out[i] = f;
            }
        }
        return out;
    }

    private static final int REFLECT_START_FRAME = 17;
    private static final int REFLECT_END_FRAME   = 26;

    public boolean isAttacking() {
        return state == State.ATTACK
            && currentFrame >= REFLECT_START_FRAME
            && currentFrame <= REFLECT_END_FRAME;
    }

    private long lastSpikeMs = -1L;
    private static final long SPIKE_COOLDOWN_MS = 6000L;

    public java.util.List<int[]> pollSpikeAttack(long nowMs,
                                                 java.util.List<int[]> emitters) {
        java.util.List<int[]> picked = new java.util.ArrayList<>();
        if (!alive || state == State.DEATH) return picked;
        if (emitters == null || emitters.isEmpty()) return picked;
        if (distanceToPlayer() > VISION_RANGE) return picked;
        if (lastSpikeMs < 0) lastSpikeMs = nowMs;
        if (nowMs - lastSpikeMs < SPIKE_COOLDOWN_MS) return picked;
        lastSpikeMs = nowMs;

        java.util.Random rng = new java.util.Random();
        int target = Math.max(2, emitters.size() * 4 / 10);
        java.util.List<int[]> shuffled = new java.util.ArrayList<>(emitters);
        java.util.Collections.shuffle(shuffled, rng);
        for (int i = 0; i < Math.min(target, shuffled.size()); i++) {
            picked.add(shuffled.get(i));
        }
        return picked;
    }

    @Override
    public void takeDamage(int dmg, Projectile.Type attackType) {
        if (!alive) return;

        super.takeDamage(dmg, attackType);
        if (!alive) {

            isVisible = true;
            state = State.DEATH;
            applyStateFrames();
            return;
        }

        if (attackType == Projectile.Type.PLAYER_LIGHT
                || attackType == Projectile.Type.PLAYER_HEAVY) {
            introPlayed = true;
            if (state != State.ATTACK && state != State.DEATH && canAttack()) {
                startAttack();
            }
        }
    }

    private boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime >= ATTACK_COOLDOWN_MS;
    }

    private void startIntro() {
        state = State.INTRO;
        applyStateFrames();
    }

    private void startAttack() {
        state = State.ATTACK;
        applyStateFrames();
        meleeHitDelivered = false;
        shockwaveSpawned  = false;
        lastAttackTime    = System.currentTimeMillis();

        visionStartTime   = -1;
    }

    private void pickIdleOrWalk() {
        double dist = distanceToPlayer();
        if (dist > ATTACK_RANGE && dist <= VISION_RANGE) {
            state = State.WALK;
        } else {
            state = State.IDLE;
        }
        applyStateFrames();
    }

    private void applyStateFrames() {
        switch (state) {
            case INTRO:  animFrames = introFrames;  break;
            case WALK:   animFrames = walkFrames;   break;
            case IDLE:   animFrames = idleFrames;   break;
            case ATTACK: animFrames = attackFrames; break;
            case DEATH:  animFrames = deathFrames;  break;
        }
        currentFrame = 0;
        animTick = 0;
    }

    private void spawnShockwave() {
        int dir;
        int projX;
        if (facingRight) {
            dir = 1;
            projX = x + width;
        } else {
            dir = -1;
            projX = x - SHOCKWAVE_SIZE;
        }
        int projY = y + height - SHOCKWAVE_SIZE - 8;
        Projectile shock = new Projectile(
            projX, projY,
            dir * SHOCKWAVE_SPEED, 0,
            Projectile.Type.ENEMY_SHOCKWAVE,
            SHOCKWAVE_DAMAGE);
        shock.width  = SHOCKWAVE_SIZE;
        shock.height = SHOCKWAVE_SIZE;
        projectiles.add(shock);
    }

    @Override
    public void update() {
        applyBossGravity();

        animate();

        if (state == State.DEATH) {
            if (deathFrames != null && !deathAnimComplete) {
                if (currentFrame >= deathFrames.length - 1 && animTick == 0) {
                    deathAnimComplete = true;
                    deathFadeStart    = System.currentTimeMillis();
                }
            } else if (deathFrames == null && !deathAnimComplete) {

                deathAnimComplete = true;
                deathFadeStart    = System.currentTimeMillis();
            }
            if (deathAnimComplete
                && System.currentTimeMillis() - deathFadeStart >= DEATH_FADE_MS) {
                isVisible = false;
            }
            return;
        }

        if (!alive) return;

        if (state == State.INTRO) {
            if (introFrames == null
                || (currentFrame >= introFrames.length - 1 && animTick == 0)) {
                introPlayed = true;
                pickIdleOrWalk();
            }
            return;
        }

        if (state == State.ATTACK) {
            facePlayer();
            if (attackFrames != null && currentFrame >= ATTACK_HIT_FRAME) {
                if (!shockwaveSpawned) {
                    spawnShockwave();
                    shockwaveSpawned = true;
                }
                if (!meleeHitDelivered && distanceToPlayer() <= ATTACK_RANGE) {
                    player.takeDamage(ATTACK_DAMAGE);
                    meleeHitDelivered = true;
                }
            }
            if (attackFrames == null
                || (currentFrame >= attackFrames.length - 1 && animTick == 0)) {
                pickIdleOrWalk();
            }
            return;
        }

        double dist = distanceToPlayer();
        if (!introPlayed && dist <= VISION_RANGE) {
            startIntro();
            return;
        }

        long now = System.currentTimeMillis();
        if (dist <= VISION_RANGE) {
            if (visionStartTime < 0) visionStartTime = now;
        } else {
            visionStartTime = -1;
        }

        facePlayer();

        if (dist <= ATTACK_RANGE && canAttack()) {
            startAttack();
            return;
        }

        if (dist <= VISION_RANGE
                && visionStartTime >= 0
                && now - visionStartTime >= RANGED_TRIGGER_MS
                && canAttack()) {
            startAttack();
            return;
        }

        if (dist <= VISION_RANGE && dist > ATTACK_RANGE) {
            if (state != State.WALK) { state = State.WALK; applyStateFrames(); }
            int dir;
            if (facingRight) {
                dir = 1;
            } else {
                dir = -1;
            }
            int newX = x + dir * WALK_SPEED;

            int probeX;
            if (dir > 0) {
                probeX = newX + width - 1;
            } else {
                probeX = newX;
            }
            if (!level.isSolid(probeX, y + height - 1)) {
                x = newX;
            }
        } else {
            if (state != State.IDLE) { state = State.IDLE; applyStateFrames(); }
        }
    }

    private void applyBossGravity() {
        if (!affectedByGravity) return;

        int centerX = x + width / 2;
        int leftX   = x + 4;
        int rightX  = x + width - 4;

        int below       = y + height + 1;
        boolean centerSolid = level.isSolid(centerX, below);
        boolean anyFootSolid = centerSolid
            || level.isSolid(leftX,  below)
            || level.isSolid(rightX, below);

        boolean grounded;
        if (bossFallSpeed > 0) {

            grounded = anyFootSolid;
        } else {

            grounded = centerSolid;
        }

        if (!grounded) {
            bossFallSpeed += 1;
            if (bossFallSpeed > MAX_BOSS_FALL) bossFallSpeed = MAX_BOSS_FALL;
            y += bossFallSpeed;
        } else {

            int snappedY = ((y + height) / Level.TILE_SIZE) * Level.TILE_SIZE - height;
            if (snappedY >= y - Level.TILE_SIZE) {
                y = snappedY;
            }
            bossFallSpeed = 0;
        }
    }

    @Override
    protected void drawSelf(Graphics2D g2) {
        if (!alive && state != State.DEATH) return;
        BufferedImage frame = getCurrentFrame();
        if (frame == null) {
            g2.setColor(fallbackColor);
            g2.fillRect(x, y, width, height);
            return;
        }
        Rectangle b = getOpaqueBounds(frame);
        int fw = frame.getWidth();
        int fh = frame.getHeight();
        int destBottomY = y + height;
        int destCenterX = x + width / 2;
        int destFrameX, destFrameY, drawW;
        if (facingRight) {
            destFrameX = destCenterX - (b.x + b.width / 2);
            destFrameY = destBottomY - (b.y + b.height);
            drawW      = fw;
        } else {

            destFrameX = destCenterX + (b.x + b.width / 2);
            destFrameY = destBottomY - (b.y + b.height);
            drawW      = -fw;
        }

        g2.drawImage(frame, destFrameX, destFrameY, drawW, fh, null);

        if (deathAnimComplete && deathFadeStart > 0) {
            float p = (System.currentTimeMillis() - deathFadeStart) / (float) DEATH_FADE_MS;
            if (p < 0f) p = 0f;
            if (p > 1f) p = 1f;
            if (p > 0f) {
                BufferedImage gray = getGrayscaleFrame(frame);
                Composite prev = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p));
                g2.drawImage(gray, destFrameX, destFrameY, drawW, fh, null);
                g2.setComposite(prev);
            }
        }
    }

    private BufferedImage getGrayscaleFrame(BufferedImage src) {
        BufferedImage cached = grayscaleCache.get(src);
        if (cached != null) return cached;
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[w * h];
        src.getRGB(0, 0, w, h, pixels, 0, w);
        for (int i = 0; i < pixels.length; i++) {
            int px = pixels[i];
            int a = (px >> 24) & 0xFF;
            int r = (px >> 16) & 0xFF;
            int g = (px >>  8) & 0xFF;
            int bl = px        & 0xFF;
            int gy = (int) (0.2126 * r + 0.7152 * g + 0.0722 * bl);
            if (gy > 255) gy = 255;
            pixels[i] = (a << 24) | (gy << 16) | (gy << 8) | gy;
        }
        out.setRGB(0, 0, w, h, pixels, 0, w);
        grayscaleCache.put(src, out);
        return out;
    }

    public boolean isDeathSequenceComplete() {
        return state == State.DEATH
            && deathAnimComplete
            && deathFadeStart > 0
            && System.currentTimeMillis() - deathFadeStart >= DEATH_FADE_MS;
    }

    private Rectangle getOpaqueBounds(BufferedImage frame) {
        Rectangle b = hitboxCache.get(frame);
        if (b == null) {
            b = opaqueBounds(frame);
            hitboxCache.put(frame, b);
        }
        return b;
    }

    @Override
    public Rectangle2D.Double getBoundingRectangle() {
        BufferedImage frame = getCurrentFrame();
        if (frame == null) return super.getBoundingRectangle();
        Rectangle b = getOpaqueBounds(frame);
        int destBottomY = y + height;
        int destCenterX = x + width / 2;
        double bx = destCenterX - b.width / 2.0;
        double by = destBottomY - b.height;
        return new Rectangle2D.Double(bx, by, b.width, b.height);
    }
}
