package app.packed.introspection;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.formatSimple;

import java.lang.reflect.Constructor;

import app.packed.base.TypeLiteral;
import packed.internal.introspection.PackedConstructorDescriptor;

/**
 * An immutable constructor descriptor.
 * <p>
 * Unlike the {@link Constructor} class, this interface contains no mutable operations, so it can be freely shared.
 * 
 * @apiNote In the future, if the Java language permits, {@link ConstructorDescriptor} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 */
public interface ConstructorDescriptor<T> extends ExecutableDescriptor {

    /**
     * Returns a constructor descriptor representing the specified constructor.
     *
     * @param constructor
     *            the constructor for which to return a descriptor for
     * @return the descriptor
     */
    static <T> ConstructorDescriptor<T> from(Constructor<T> constructor) {
        return new PackedConstructorDescriptor<T>(constructor);
    }

    /**
     * Creates a new descriptor by finding a constructor on the specified declaring class with the specified parameter
     * types.
     * 
     * @param <T>
     *            the class in which the constructor is declared
     * @param declaringClass
     *            the class that declares the constructor
     * @param parameterTypes
     *            the parameter types of the constructor
     * @return a new constructor descriptor
     * @throws IllegalArgumentException
     *             if a constructor with the specified parameter types does not exist on the specified type
     * @see Class#getDeclaredConstructor(Class...)
     */
    // find() Must be????
    static <T> ConstructorDescriptor<T> of(Class<T> declaringClass, Class<?>... parameterTypes) {
        requireNonNull(declaringClass, "declaringClass is null");
        Constructor<T> constructor;
        try {
            constructor = declaringClass.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("A constructor with the specified signature does not exist, signature: " + declaringClass.getSimpleName() + "("
                    + formatSimple(parameterTypes) + ")");
        }
        return from(constructor);
    }

    @SuppressWarnings("unchecked")
    static <T> ConstructorDescriptor<T> of(TypeLiteral<T> declaringClass, Class<?>... parameterTypes) {
        requireNonNull(declaringClass, "declaringClass is null");
        return (ConstructorDescriptor<T>) of(declaringClass.rawType(), parameterTypes);
    }
}