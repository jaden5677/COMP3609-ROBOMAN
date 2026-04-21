package Factory;

import Entities.EntityInterface;

/**
 * Optional convenience base for factories. Subclasses only have to
 * override the methods relevant to their domain.
 */
public abstract class AbstractFactory<T extends EntityInterface, K>
        implements FactoryInterface<T, K> {

    @Override
    public abstract T create(K kind, int x, int y);

    @Override
    public T createRandom(int x, int y) {
        throw new UnsupportedOperationException("createRandom not implemented");
    }

    @Override
    public T[] createAll(int x, int y) {
        throw new UnsupportedOperationException("createAll not implemented");
    }
}
