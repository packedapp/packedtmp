package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

/** An factory support class. */
final class FactorySupport<T> {

    /** The function used to create a new instance. */
    public final FactoryHandle<T> handle;

    public FactorySupport(FactoryHandle<T> function) {
        this.handle = requireNonNull(function);
    }

}