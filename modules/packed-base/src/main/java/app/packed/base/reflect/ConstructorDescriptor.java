package app.packed.base.reflect;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.formatSimple;

import java.lang.reflect.Constructor;

import app.packed.base.TypeLiteral;
import packed.internal.base.reflect.PackedConstructorDescriptor;

public interface ConstructorDescriptor<T> extends ExecutableDescriptor {

    /**
     * Creates a new descriptor from the specified constructor.
     *
     * @param constructor
     *            the constructor to create a descriptor from
     * @return a new constructor descriptor
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