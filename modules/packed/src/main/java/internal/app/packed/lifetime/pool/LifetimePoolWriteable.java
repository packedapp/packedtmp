package internal.app.packed.lifetime.pool;

import internal.app.packed.lifetime.LifetimeObjectArena;

@FunctionalInterface
public interface LifetimePoolWriteable {
    void writeToPool(LifetimeObjectArena pool);
}
