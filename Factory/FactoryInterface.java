package Factory;

import Entities.EntityInterface;

/**
 * Generic factory contract.
 *
 * Parameterised so that {@link ItemFactory} can produce items
 * (T = AbstractItem) and {@link EnemyFactory} can produce enemies
 * (T = Enemy) while sharing the same shape. {@code K} is the kind /
 * key used to select what to build (an enum, char, string, etc.).
 */
public interface FactoryInterface<T extends EntityInterface, K> {
    T create(K kind, int x, int y);
    T createRandom(int x, int y);
    T[] createAll(int x, int y);
}
