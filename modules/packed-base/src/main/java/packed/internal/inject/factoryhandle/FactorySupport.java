package packed.internal.inject.factoryhandle;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.lang.Key;
import app.packed.lifecycle.OnStart;
import app.packed.service.Provide;
import packed.internal.inject.Dependency;

/** An factory support class. */
public final class FactorySupport<T> {

    /** The key that this factory will be registered under by default with an injector. */
    public final Key<T> defaultKey;

    /** A list of all of this factory's dependencies. */
    public final List<Dependency> dependencies;

    /** The function used to create a new instance. */
    public final FactoryHandle<T> handle;

    public FactorySupport(FactoryHandle<T> function, List<Dependency> dependencies) {
        this.dependencies = requireNonNull(dependencies, "dependencies is null");
        this.handle = requireNonNull(function);
        this.defaultKey = function.typeLiteral.toKey();
    }

    /**
     * Returns the scannable type of this factory. This is the type that will be used for scanning for annotations such as
     * {@link OnStart} and {@link Provide}.
     *
     * @return the scannable type of this factory
     */
    public Class<? super T> getScannableType() {
        return handle.returnTypeRaw();
    }
}