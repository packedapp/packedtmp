package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.base.Key;
import app.packed.base.TypeLiteral;
import app.packed.introspection.ExecutableDescriptor;
import packed.internal.classscan.util.ConstructorUtil;
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

    static <T> FactorySupport<T> find(Class<T> implementation) {
        ExecutableDescriptor executable = findExecutable(implementation);
        return new FactorySupport<>(new ExecutableFactoryHandle<>(TypeLiteral.of(implementation), executable, null),
                DependencyDescriptor.fromExecutable(executable));
    }

    static <T> FactorySupport<T> find(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        ExecutableDescriptor executable = findExecutable(implementation.rawType());
        return new FactorySupport<>(new ExecutableFactoryHandle<>(implementation, executable, null), DependencyDescriptor.fromExecutable(executable));
    }

    // Should we have a strict type? For example, a static method on MyExtension.class
    // must return MyExtension... Det maa de sgu alle.. Den anden er findMethod()...
    // MyExtension.class create()
    static ExecutableDescriptor findExecutable(Class<?> type) {
        return ExecutableDescriptor.from(ConstructorUtil.findInjectableIAE(type));
    }
}