package app.packed.introspection;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

import packed.internal.base.reflect.PackedFieldDescriptor;

/**
 * An immutable field descriptor.
 * <p>
 * Unlike the {@link Field} class, this interface contains no mutable operations, so it can be freely shared.
 * 
 * @apiNote In the future, if the Java language permits, {@link FieldDescriptor} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface FieldDescriptor extends VariableDescriptor, MemberDescriptor {

    /**
     * Returns whether or not this field is a final field.
     *
     * @return whether or not this field is a final field
     * @see Modifier#isFinal(int)
     */
    boolean isFinal();

    /**
     * Returns whether or not this field is a static field.
     *
     * @return whether or not this field is a static field
     * @see Modifier#isFinal(int)
     */
    boolean isStatic();

    /**
     * Returns whether or not the field is volatile.
     * 
     * @return whether or not the field is volatile
     */
    boolean isVolatile();

    /**
     * Produces a method handle giving read access to a field.
     * 
     * @param lookup
     *            the lookup object to use for unreflecting this field
     * @return a method handle which can read values into the this field
     * @throws IllegalAccessException
     *             if access checking fails
     * @see Lookup#unreflectGetter(Field)
     */
    MethodHandle unreflectGetter(Lookup lookup) throws IllegalAccessException;

    /**
     * Produces a method handle giving write access to a field.
     * 
     * @param lookup
     *            the lookup object to use for unreflecting this field
     * @return a method handle which can store values into the this field
     * @throws IllegalAccessException
     *             if access checking fails
     * @see Lookup#unreflectSetter(Field)
     */
    MethodHandle unreflectSetter(Lookup lookup) throws IllegalAccessException;

    /**
     * Unreflects this field.
     * 
     * @param lookup
     *            the lookup object to use for unreflecting this field
     * @return a VarHandle corresponding to this field
     * @throws IllegalAccessException
     *             if access checking fails
     * @see Lookup#unreflectVarHandle(Field)
     */
    VarHandle unreflectVarHandle(Lookup lookup) throws IllegalAccessException;

    /**
     * Returns a field descriptor representing a field on the specified class with the specified name. Or fails with
     * {@link IllegalArgumentException} if no such field exists.
     *
     * @param clazz
     *            the type on which the field is located
     * @param fieldName
     *            the name of the field
     * @return the descriptor
     * @throws IllegalArgumentException
     *             if a field with the specified name could not be found on the specified type
     * @see Class#getDeclaredField(String)
     */
    // Maaske skal vi kun have optional???
    // locate() for at differencer den for Factory.find
    static FieldDescriptor find(Class<?> clazz, String fieldName) {
        requireNonNull(clazz, "clazz is null");
        requireNonNull(fieldName, "fieldName is null");
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return from(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("The specified field '" + fieldName + "' could not be found on class: " + format(clazz), e);
        }
    }

    /**
     * Returns a field descriptor representing the specified field.
     *
     * @param field
     *            the field to return a descriptor for
     * @return the descriptor
     */
    static FieldDescriptor from(Field field) {
        return PackedFieldDescriptor.from(field);
    }

    static Optional<FieldDescriptor> tryFind(Class<?> clazz, String fieldName) {
        requireNonNull(clazz, "clazz is null");
        requireNonNull(fieldName, "fieldName is null");
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return Optional.of(from(field));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }
}