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
package app.packed.reflect;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;
import packed.internal.util.InternalErrorException;

/**
 * A field descriptor.
 * <p>
 * Unlike the {@link Field} class, this interface contains no mutable operations, so it can be freely shared.
 */
public final class FieldDescriptor extends VarDescriptor implements Member, AnnotatedElement {

    /** The field that is being wrapped. */
    private final Field field;

    /**
     * Creates a new descriptor from the specified field.
     *
     * @param field
     *            the field to create a descriptor for
     */
    private FieldDescriptor(Field field) {
        this.field = requireNonNull(field, "field is null");
    }

    public <T> T apply(Lookup caller, VarOperator<T> operator, Object instance) {
        requireNonNull(operator, "operator is null");
        return operator.apply(caller, field, instance);
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
        } else if (obj instanceof FieldDescriptor) {
            return ((FieldDescriptor) obj).field.equals(field);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return field.getAnnotationsByType(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return field.getDeclaredAnnotation(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return field.getDeclaredAnnotations();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return field.getDeclaredAnnotationsByType(annotationClass);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
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
    public int index() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    /**
     * Returns whether or not this field is a final field.
     *
     * @return whether or not this field is a final field
     * @see Modifier#isFinal(int)
     */
    public final boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNamePresent() {
        return true;
    }

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

    /**
     * Creates a new {@link Field} corresponding to this descriptor.
     *
     * @return a new field
     */
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

    public MethodHandle unreflectGetter(Lookup lookup) throws IllegalAccessException {
        return lookup.unreflectGetter(field);
    }

    public MethodHandle unreflectSetter(Lookup lookup) throws IllegalAccessException {
        return lookup.unreflectSetter(field);
    }

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
    public VarHandle unreflectVarHandle(Lookup lookup) throws IllegalAccessException {
        requireNonNull(lookup, "lookup is null");
        return lookup.unreflectVarHandle(field);
    }

    /**
     * Returns the field that this descriptor wraps.
     * 
     * @return the field that this descriptor wraps
     */
    public Field unsafeField() {
        return field;
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
    public static FieldDescriptor of(Field field) {
        return new FieldDescriptor(field);
    }
}
