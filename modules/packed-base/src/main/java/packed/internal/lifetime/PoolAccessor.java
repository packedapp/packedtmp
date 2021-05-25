package packed.internal.lifetime;

import java.lang.invoke.MethodHandle;

public final class PoolAccessor {

    private final int index;

    PoolAccessor(int index) {
        this.index = index;
    }

    public void store(LifetimePool pool, Object o) {
        pool.storeObject(index, o);
    }

    public Object read(LifetimePool pool) {
        return pool.read(index);
    }

    public MethodHandle indexedReader(Class<?> clazz) {
        return LifetimePool.indexedReader(index, clazz);
    }
}
