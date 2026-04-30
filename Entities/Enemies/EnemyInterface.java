package Entities.Enemies;

import java.util.List;

import Entities.EntityInterface;
import Entities.Projectiles.Projectile;

public interface EnemyInterface extends EntityInterface {
    void takeDamage(int dmg, Projectile.Type attackType);
    boolean isAlive();
    int getPoints();
    int getEnemyHealth();
    List<Projectile> getProjectiles();
}
