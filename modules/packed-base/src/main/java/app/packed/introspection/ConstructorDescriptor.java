package app.packed.introspection;

import java.lang.reflect.Constructor;

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
     * @param <T>
     *            some type
     * @param constructor
     *            the constructor for which to return a descriptor for
     * @return the descriptor
     */
    static <T> ConstructorDescriptor<T> from(Constructor<T> constructor) {
        return new PackedConstructorDescriptor<T>(constructor);
    }
}