package packed.internal.lifetime;

// TODO vi burde jo gemme
@FunctionalInterface
public interface LifetimePoolWriteable {
    void writeToPool(LifetimePool pool);
}
