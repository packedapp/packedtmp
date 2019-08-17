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
package packed.internal.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.FieldOperator;
import app.packed.hook.DelayedHookOperator;
import app.packed.hook.field.PackedFieldOperation;
import app.packed.hook.field.PackedFieldRuntimeAccessor;
import app.packed.util.FieldDescriptor;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.InvalidDeclarationException;
import packed.internal.util.ErrorMessageBuilder;
import packed.internal.util.StringFormatter;
import packed.internal.util.descriptor.InternalFieldDescriptor;

/** The default implementation of {@link AnnotatedFieldHook}. */
final class PackedAnnotatedFieldHook<T extends Annotation> implements AnnotatedFieldHook<T> {

    /** The annotation value. */
    private final T annotation;

    /** A cached field descriptor, is lazily created via {@link #field()}. */
    private volatile FieldDescriptor descriptor;

    /** The annotated field. */
    private final Field field;

    // Must have an owner.... And then ComponentConfiguration must have the same owner....
    // And I guess access mode as well
    // owner, for example, bundle.getClass();

    // Owner_Type, Component_Instance_Type, Field, FunctionalType, AccessMode

    /** A lookup object used to create various handlers. */
    private final Lookup lookup;

    PackedAnnotatedFieldHook(Lookup lookup, Field field, T annotation) {
        this.lookup = requireNonNull(lookup);
        this.field = requireNonNull(field);
        this.annotation = requireNonNull(annotation);
    }

    /** {@inheritDoc} */
    @Override
    public T annotation() {
        return annotation;
    }

    /** {@inheritDoc} */
    @Override
    public <E> DelayedHookOperator<E> applyDelayed(FieldOperator<E> mapper) {
        return new PackedFieldRuntimeAccessor<E>(lookup, field, (PackedFieldOperation<E>) mapper);
    }

    /** {@inheritDoc} */
    @Override
    public <E> E applyStatic(FieldOperator<E> operator) {
        requireNonNull(operator, "operator is null");
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException("Cannot invoke this method on non-static field " + field);
        }
        return operator.applyStatic(lookup, field);

    }

    /** {@inheritDoc} */
    @Override
    public AnnotatedFieldHook<T> checkAssignableTo(Class<?> type) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public AnnotatedFieldHook<T> checkExactType(Class<?> type) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public AnnotatedFieldHook<T> checkFinal() {
        // Methods annotated with @Dooo cannot be static
        // Methods annotated with @Dooo must be static
        // Annotations of type @Dooo are not allowed on static methods
        // Annotations of type @Dooo are only allowed on static methods
        if (!Modifier.isFinal(field.getModifiers())) {

            // throw new InvalidDeclarationException("Fields annotated with @" + annotationType.getSimpleName() + " must be final,
            // field = " + field
            // + ", to resolve remove @" + annotationType.getSimpleName() + " or make the field final");
            //
            // throw new InvalidDeclarationException(
            // ErrorMessageBuilder.of(field).cannot("be static when using the @" + annotationType.getSimpleName() + " annotation")
            // .toResolve("remove @" + annotationType.getSimpleName() + " or make the field non-static"));
            //
            throw new IllegalStateException(
                    "Fields annotated with @" + annotation.annotationType().getSimpleName() + " must be final, field = " + StringFormatter.format(field));
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotatedFieldHook<T> checkNotFinal() {
        if (Modifier.isFinal(field.getModifiers())) {
            throw new IllegalStateException(
                    "Fields annotated with @" + annotation.annotationType().getSimpleName() + " must not be final, field = " + StringFormatter.format(field));

            // return "Cannot use @" + annotationType.getSimpleName() + " on final field: " + field + ", to resolve remove @" +
            // annotationType.getSimpleName()
            // + " or make the field non-final";
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotatedFieldHook<T> checkNotStatic() {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new IllegalStateException(
                    "Fields annotated with @" + annotation.annotationType().getSimpleName() + " must not be static, field = " + StringFormatter.format(field));
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotatedFieldHook<T> checkStatic() {
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new IllegalStateException(
                    "Fields annotated with @" + annotation.annotationType().getSimpleName() + " must be static, field = " + StringFormatter.format(field));
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public FieldDescriptor field() {
        FieldDescriptor d = descriptor;
        if (d == null) {
            descriptor = d = FieldDescriptor.of(field);
        }
        return d;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle getter() {
        field.setAccessible(true);
        try {
            return lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Could not create a MethodHandle", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Lookup lookup() {
        return lookup;// Temporary method
    }

    @Override
    public MethodHandle setter() {
        field.setAccessible(true);
        try {
            return lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Could not create a MethodHandle", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public VarHandle varHandle() {
        field.setAccessible(true);
        try {
            return lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Could not create a VarHandle", e);
        }
    }

    /**
     * Checks that an annotated field is not static.
     * 
     * @param field
     *            the field to check
     * @param annotationType
     *            the type of annotation that forced the check
     */
    protected static void checkAnnotatedFieldIsNotStatic(InternalFieldDescriptor field, Class<? extends Annotation> annotationType) {
        if ((Modifier.isStatic(field.getModifiers()))) {
            throw new InvalidDeclarationException(
                    ErrorMessageBuilder.of(field).cannot("be static when using the @" + annotationType.getSimpleName() + " annotation")
                            .toResolve("remove @" + annotationType.getSimpleName() + " or make the field non-static"));
            //
            // throw new InvalidDeclarationException("Cannot use @" + annotationType.getSimpleName() + " on static field: " + field
            // + ", to resolve remove @"
            // + annotationType.getSimpleName() + " or make the field non-static");
        }
    }

    protected static String fieldCannotHaveBothAnnotations(InternalFieldDescriptor field, Class<? extends Annotation> annotationType1,
            Class<? extends Annotation> annotationType2) {
        return "Cannot use both @" + annotationType1.getSimpleName() + " and @" + annotationType1.getSimpleName() + " on field: " + field
                + ", to resolve remove one of the annotations.";
    }
}
