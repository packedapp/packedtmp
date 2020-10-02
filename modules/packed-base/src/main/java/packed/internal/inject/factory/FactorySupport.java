package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodType;
import java.util.List;

import app.packed.base.Key;
import app.packed.inject.Provide;
import app.packed.statemachine.OnStart;
import packed.internal.inject.dependency.DependencyDescriptor;

/** An factory support class. */
final class FactorySupport<T> {

    /** A list of all of this factory's dependencies. */
    public final List<DependencyDescriptor> dependencies;

    /** The function used to create a new instance. */
    public final FactoryHandle<T> handle;

    /** The key that this factory will be registered under by default with an injector. */
    public final Key<T> key;

    public FactorySupport(FactoryHandle<T> function, List<DependencyDescriptor> dependencies) {
        this.dependencies = requireNonNull(dependencies, "dependencies is null");
        this.handle = requireNonNull(function);
        this.key = Key.fromTypeLiteral(function.typeLiteral);
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

    /**
     * Returns the method type of the factory.
     * 
     * @return the method type of the factory
     */
    public MethodType methodType() {
        return handle.methodType();
    }
}