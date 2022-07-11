package internal.app.packed.lifetime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import internal.app.packed.util.MethodHandleUtil;

public final class PoolEntryHandle {

    private final Class<?> clazz;
    private final int index;

    PoolEntryHandle(Class<?> clazz, int index) {
        this.clazz = requireNonNull(clazz);
        this.index = index;
    }

    // Skal vi vide hvor vi bliver laest fra???
    public MethodHandle poolReader() {
        // (LifetimePool, int)Object -> (LifetimePool)Object
        MethodHandle mh = MethodHandles.insertArguments(LifetimeConstantPool.MH_CONSTANT_POOL_READER, 1, index);
        return MethodHandleUtil.castReturnType(mh, clazz); // (LifetimePool)Object -> (LifetimePool)clazz
    }

    public Object read(LifetimeConstantPool pool) {
        return pool.read(index);
    }

    public void store(LifetimeConstantPool pool, Object o) {
        if (!clazz.isInstance(o)) {
            throw new Error("Expected " + clazz + ", was " + o.getClass());
        }
        pool.storeObject(index, o);
    }
}
