package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import app.packed.base.TypeLiteral;
import app.packed.introspection.ExecutableDescriptor;
import packed.internal.inject.ServiceDependency;
import packed.internal.inject.util.FindInjectableConstructor;

/** This class is responsible for finding an injectable executable. */

//Det der er speciel ved den.. Er at den ikke noedvendigvis har adgang til et Lookup object...
public class FactoryFindInjectableExecutable {

    public static <T> FactorySupport<T> find(Class<T> implementation) {
        ExecutableDescriptor executable = findExecutable(implementation);
        return new FactorySupport<>(new ExecutableFactoryHandle<>(TypeLiteral.of(implementation), executable, null),
                ServiceDependency.fromExecutable(executable));
    }

    public static <T> FactorySupport<T> find(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        ExecutableDescriptor executable = findExecutable(implementation.rawType());
        return new FactorySupport<>(new ExecutableFactoryHandle<>(implementation, executable, null), ServiceDependency.fromExecutable(executable));
    }

    // Should we have a strict type? For example, a static method on MyExtension.class
    // must return MyExtension... Det maa de sgu alle.. Den anden er findMethod()...
    // MyExtension.class create()
    private static ExecutableDescriptor findExecutable(Class<?> type) {
        return ExecutableDescriptor.from(FindInjectableConstructor.find(type));
    }
}