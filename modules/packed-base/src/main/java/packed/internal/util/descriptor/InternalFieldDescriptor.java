/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package packed.internal.util.descriptor;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import app.packed.container.extension.FieldOperator;
import app.packed.util.FieldDescriptor;
import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;
import packed.internal.container.extension.hook.PackedFieldOperation;
import packed.internal.util.InternalErrorException;

/** The default implementation of {@link FieldDescriptor}. */
public final class InternalFieldDescriptor extends InternalVariableDescriptor implements FieldDescriptor, InternalMemberDescriptor {

    /** The field that is being wrapped. */
    private final Field field;

    /**
     * Creates a new descriptor from the specified field.
     *
     * @param field
     *            the field to create a descriptor for
     */
    private InternalFieldDescriptor(Field field) {
        super(requireNonNull(field, "field is null"));
        this.field = field;
    }

    /** {@inheritDoc} */
    @Override
    public String descriptorTypeName() {
        return "field";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof InternalFieldDescriptor) {
            return ((InternalFieldDescriptor) obj).field.equals(field);
        } else if (obj instanceof FieldDescriptor) {
            return ((FieldDescriptor) obj).newField().equals(field);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
    }

    /** {@inheritDoc} */
    @Override
    public int index() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getModifiers() {
        return field.getModifiers();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return field.getName();
    }

    /** {@inheritDoc} */
    @Override
    public Type getParameterizedType() {
        return field.getGenericType();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getType() {
        return field.getType();
    }

    /** {@inheritDoc} */
    @Override
    public TypeLiteral<?> getTypeLiteral() {
        return TypeLiteral.fromField(field);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return field.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNamePresent() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPrimitiveType() {
        return field.getType().isPrimitive();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSynthetic() {
        return field.isSynthetic();
    }

    /**
     * Returns whether or not the field is volatile.
     * 
     * @return whether or not the field is volatile
     */
    public boolean isVolatile() {
        return Modifier.isVolatile(field.getModifiers());
    }

    /** {@inheritDoc} */
    @Override
    public Field newField() {
        Class<?> declaringClass = field.getDeclaringClass();
        try {
            return declaringClass.getDeclaredField(field.getName());
        } catch (NoSuchFieldException e) {
            throw new InternalErrorException("field", field, e);// We should never get to here
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return format(field);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle unreflectGetter(Lookup lookup) throws IllegalAccessException {
        return lookup.unreflectGetter(field);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle unreflectSetter(Lookup lookup) throws IllegalAccessException {
        return lookup.unreflectSetter(field);
    }

    /** {@inheritDoc} */
    @Override
    public VarHandle unreflectVarHandle(Lookup lookup) throws IllegalAccessException {
        requireNonNull(lookup, "lookup is null");
        return lookup.unreflectVarHandle(field);
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
    public static InternalFieldDescriptor of(Class<?> clazz, String fieldName) {
        requireNonNull(clazz, "clazz is null");
        requireNonNull(fieldName, "fieldName is null");
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return of(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("The specified field '" + fieldName + "' could not be found on class: " + format(clazz), e);
        }
    }

    /**
     * Creates a new descriptor from the specified field.
     *
     * @param field
     *            the field to wrap
     * @return a new field descriptor
     */
    public static InternalFieldDescriptor of(Field field) {
        return new InternalFieldDescriptor(field);
    }

    /**
     * If the specified descriptor is an instance of this class. This method casts and returns the specified descriptor.
     * Otherwise creates a new descriptor.
     *
     * @param descriptor
     *            the descriptor to copy or return
     * @return a field descriptor
     */
    public static InternalFieldDescriptor of(FieldDescriptor descriptor) {
        return descriptor instanceof InternalFieldDescriptor ? (InternalFieldDescriptor) descriptor : of(descriptor.newField());
    }

    /** {@inheritDoc} */
    @Override
    public <T> T apply(Lookup caller, FieldOperator<T> operator, Object instance) {
        requireNonNull(operator, "operator is null");
        return ((PackedFieldOperation<T>) operator).apply(caller, field, instance);
    }
}
