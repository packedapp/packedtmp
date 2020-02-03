package app.packed.base.reflect;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import packed.internal.base.reflect.PackedFieldDescriptor;

public interface FieldDescriptor extends VariableDescriptor, MemberDescriptor {

    /**
     * Returns whether or not this field is a final field.
     *
     * @return whether or not this field is a final field
     * @see Modifier#isFinal(int)
     */
    boolean isFinal();

    boolean isStatic();

    /**
     * Returns whether or not the field is volatile.
     * 
     * @return whether or not the field is volatile
     */
    boolean isVolatile();
    //
    // /**
    // * Creates a new {@link Field} corresponding to this descriptor.
    // *
    // * @return a new field
    // */
    // // Hide/Remove
    // public Field newField() {
    // Class<?> declaringClass = field.getDeclaringClass();
    // try {
    // return declaringClass.getDeclaredField(field.getName());
    // } catch (NoSuchFieldException e) {
    // throw new InternalErrorException("field", field, e);// We should never get to here
    // }
    // }

    MethodHandle unreflectGetter(Lookup lookup) throws IllegalAccessException;

    MethodHandle unreflectSetter(Lookup lookup) throws IllegalAccessException;

    /**
     * Unreflects this field.
     * 
     * @param lookup
     *            the lookup object to use for unreflecting this field
     * @return a VarHandle corresponding to this field
     * @throws IllegalAccessException
     *             if the lookup object does not have access to the field
     * @see Lookup#unreflectVarHandle(Field)
     */
    VarHandle unreflectVarHandle(Lookup lookup) throws IllegalAccessException;

    /**
     * Returns a field descriptor representing the specified field.
     *
     * @param field
     *            the field for which to return a descriptor for
     * @return the descriptor
     */
    static FieldDescriptor from(Field field) {
        return PackedFieldDescriptor.from(field);
    }

    /**
     * Creates a new field descriptor by trying to find a declared on the specified class with the specified name.
     *
     * @param clazz
     *            the type on which the field is located
     * @param fieldName
     *            the name of the field
     * @return a new descriptor
     */
    public static FieldDescriptor of(Class<?> clazz, String fieldName) {
        requireNonNull(clazz, "clazz is null");
        requireNonNull(fieldName, "fieldName is null");
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return from(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("The specified field '" + fieldName + "' could not be found on class: " + format(clazz), e);
        }
    }
}