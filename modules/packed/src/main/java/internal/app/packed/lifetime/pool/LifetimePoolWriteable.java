package internal.app.packed.lifetime.pool;

// TODO vi burde jo gemme
@FunctionalInterface
public interface LifetimePoolWriteable {
    void writeToPool(LifetimeConstantPool pool);
}
