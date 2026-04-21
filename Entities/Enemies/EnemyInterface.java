package Entities.Enemies;

import java.util.List;

import Entities.EntityInterface;
import Entities.Projectiles.Projectile;

/**
 * Capabilities every enemy must expose.
 * Extends EntityInterface so enemies fit into the unified entity collection,
 * while adding combat / scoring methods that only make sense for enemies.
 */
public interface EnemyInterface extends EntityInterface {
    void takeDamage(int dmg, Projectile.Type attackType);
    boolean isAlive();
    int getPoints();
    int getEnemyHealth();
    List<Projectile> getProjectiles();
}
