package Factory;

import Entities.EntityInterface;

public interface FactoryInterface<T extends EntityInterface, K> {
    T create(K kind, int x, int y);
    T createRandom(int x, int y);
    T[] createAll(int x, int y);
}
