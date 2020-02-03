package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.base.InvalidDeclarationException;
import app.packed.base.TypeLiteral;
import app.packed.base.reflect.ExecutableDescriptor;
import app.packed.inject.Inject;
import packed.internal.inject.ServiceDependency;
import packed.internal.util.StringFormatter;
import packed.internal.util.types.TypeUtil;

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
        return ExecutableDescriptor.from(findExecutable0(type));
    }

    private static Executable findExecutable0(Class<?> type) {
        if (type.isArray()) {
            throw new IllegalArgumentException(format(type) + " is an array and cannot be instantiated");
        } else if (type.isAnnotation()) {
            throw new IllegalArgumentException(format(type) + ") is an annotation and cannot be instantiated");
        }

        // Try to find a single static method annotated with @Inject
        Method method = null;
        for (Method m : type.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && m.isAnnotationPresent(Inject.class)) {
                if (method != null) {
                    throw new IllegalArgumentException("There are multiple static methods annotated with @Inject on " + format(type));
                }
                method = m;
            }
        }

        // If a single method has been found, use it
        if (method != null) {
            // Det er jo i virkeligheden en Key vi laver her, burde havde det samme checkout..
            if (method.getReturnType() == void.class /* || returnType == Void.class */) {
                throw new IllegalArgumentException("Static method " + method + " annotated with @Inject cannot have a void return type."
                        + " (@Inject on static methods are used to indicate that the method is a factory for a specific type, not for injecting values");
            } else if (TypeUtil.isOptionalType(method.getReturnType())) {
                throw new IllegalArgumentException("Static method " + method + " annotated with @Inject cannot have an optional return type ("
                        + method.getReturnType().getSimpleName() + "). A valid instance needs to be provided by the method");
            }
            // Sporgsmaalet er om den skal have this this.class som return type...
            // Og saa brugere skal bruge Factory.findStaticInject(Class, Type); <----
            return method;
        }

        if (Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException("'" + StringFormatter.format(type) + "' cannot be an abstract class");
        }

        Constructor<?>[] constructors = type.getDeclaredConstructors();

        // If we only have 1 constructor, return it.
        if (constructors.length == 1) {
            return constructors[0];
        }

        // See if we have a single constructor annotated with @Inject
        Constructor<?> constructor = null;
        for (Constructor<?> c : constructors) {
            if (c.isAnnotationPresent(Inject.class)) {
                if (constructor != null) {
                    throw new InvalidDeclarationException(
                            "Multiple constructors annotated with @" + Inject.class.getSimpleName() + " on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        for (Constructor<?> c : constructors) {
            if (Modifier.isPublic(c.getModifiers())) {
                if (constructor != null) {
                    throw new IllegalArgumentException(
                            "No constructor annotated with @" + Inject.class.getSimpleName() + ". And multiple public constructors on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        for (Constructor<?> c : constructors) {
            if (Modifier.isProtected(c.getModifiers())) {
                if (constructor != null) {
                    throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
                            + ". And multiple protected constructors on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // Remaining constructors are private or package private
        for (Constructor<?> c : constructors) {
            if (!Modifier.isPrivate(c.getModifiers())) {
                if (constructor != null) {
                    throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
                            + ". And multiple package-private constructors on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        throw new IllegalArgumentException(
                "No constructor annotated with @" + Inject.class.getSimpleName() + ". And multiple private constructors on class " + format(type));

//        // Try and find one constructor with maximum number of parameters.
//        for (Constructor<?> c : constructors) {
//            if (c.getParameterCount() == maxParameters) {
//                if (constructor != null) {
//                    throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
//                            + ". And multiple constructors having the maximum number of parameters (" + maxParameters + ") on class " + format(type));
//                }
//                constructor = c;
//            }
//        }
//        return ConstructorDescriptor.of(constructor);
    }
}