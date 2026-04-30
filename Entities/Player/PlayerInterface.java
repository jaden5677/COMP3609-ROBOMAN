package Entities.Player;

import java.util.List;

import Entities.EntityInterface;
import Entities.Projectiles.Projectile;

public interface PlayerInterface extends EntityInterface {

    void takeDamage(int dmg);
    void heal(int amount);
    int  getHealth();
    int  getMaxHealth();
    void setMaxHealth(int max);
    int  getLives();
    int  getDeathsInLevel();
    int  getCurrentLevel();

    void stun(int durationMs);
    boolean isStunned();

    void boostDamage(int amount);

    void boostJump(int amount);

    void boostSpeed(int amount);

    void setGunType(GunType gunType);

    List<Projectile> getProjectiles();

    enum GunType { NORMAL, TRIPLE_SHOT, CHARGE_SHOT }
}
