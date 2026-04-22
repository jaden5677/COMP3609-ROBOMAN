package Entities.Player;

import java.util.List;

import Entities.EntityInterface;
import Entities.Projectiles.Projectile;

/**
 * Capabilities every Player must expose.
 *
 * Items interact with the player through this interface (not the concrete
 * Player class) so item code can never reach into enemy- or projectile-only
 * methods. Because it extends {@link EntityInterface}, players still fit in
 * the unified entity collection.
 */
public interface PlayerInterface extends EntityInterface {

    // --- Combat / health ---
    void takeDamage(int dmg);
    void heal(int amount);
    int  getHealth();
    int  getMaxHealth();
    void setMaxHealth(int max);
    int  getLives();
    int  getDeathsInLevel();
    int  getCurrentLevel();

    // --- Status effects ---
    void stun(int durationMs);
    boolean isStunned();

    // --- Item-driven boosts (placeholder, no images yet) ---
    /** Multiplies / adds outgoing projectile damage. */
    void boostDamage(int amount);
    /** Increases jump strength (negative dy applied at jump). */
    void boostJump(int amount);
    /** Increases horizontal movement speed. */
    void boostSpeed(int amount);
    /** Switch the gun to a particular projectile pattern. */
    void setGunType(GunType gunType);

    // --- Projectiles produced this tick ---
    List<Projectile> getProjectiles();

    /**
     * The set of weapon modes a player can carry.
     * Concrete behaviour for each is implemented in {@link Player#shoot()}.
     */
    enum GunType { NORMAL, TRIPLE_SHOT, CHARGE_SHOT }
}
