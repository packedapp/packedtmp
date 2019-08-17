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
package packed.internal.extension.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import app.packed.extension.AnnotatedFieldHook;
import app.packed.extension.FieldOperator;
import app.packed.extension.HookApplicator;
import app.packed.util.FieldDescriptor;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.container.model.ComponentLookup;
import packed.internal.container.model.ComponentModel;
import packed.internal.extension.hook.field.PackedFieldRuntimeAccessor;
import packed.internal.util.ErrorMessageBuilder;
import packed.internal.util.StringFormatter;
import packed.internal.util.descriptor.InternalFieldDescriptor;

/** The default implementation of {@link AnnotatedFieldHook}. */
// TODO ISE for apply + applicator
final class PackedAnnotatedFieldHook<T extends Annotation> implements AnnotatedFieldHook<T> {

    /** The annotation value. */
    private final T annotation;

    /** The builder for the component type. */
    private final ComponentModel.Builder builder;

    /** A cached field descriptor, is lazily created via {@link #field()}. */
    private FieldDescriptor descriptor;

    /** The annotated field. */
    private final Field field;

    /** A cached method handle getter for the field. */
    @Nullable
    private MethodHandle getter;

    /** A component lookup object used to create the various handlers. */
    // Must have an owner.... And then ComponentConfiguration must have the same owner....
    // And I guess access mode as well, owner, for example, bundle.getClass();
    // Maybe check against the same lookup object...
    // Owner_Type, Component_Instance_Type, Field, FunctionalType, AccessMode
    /// Maybe we can just check ComponentConfiguration.lookup == this.lookup
    /// I think we actually need to check this this way
    private final ComponentLookup lookup;

    /** A cached method handle setter for the field. */
    @Nullable
    private MethodHandle setter;

    /** A cached var handle for the field. */
    @Nullable
    private VarHandle varHandle;

    /**
     * Creates a new instance.
     * 
     * @param builder
     *            the builder for the component type
     * @param field
     *            the field that is annotated
     * @param annotation
     *            the annotation value
     */
    PackedAnnotatedFieldHook(ComponentModel.Builder builder, Field field, T annotation) {
        this.builder = builder;
        this.lookup = builder.lookup();
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
    public <E> HookApplicator<E> applicator(FieldOperator<E> operator) {
        requireNonNull(operator, "operator is null");
        builder.checkActive();
        PackedFieldOperation<E> o = (PackedFieldOperation<E>) operator;
        return new PackedFieldRuntimeAccessor<E>(of(o), field, (PackedFieldOperation<E>) operator);
    }

    /** {@inheritDoc} */
    @Override
    public <E> E applyOnStaticField(FieldOperator<E> operator) {
        requireNonNull(operator, "operator is null");
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException("Cannot invoke this method on non-static field " + field);
        }
        builder.checkActive();
        PackedFieldOperation<E> o = (PackedFieldOperation<E>) operator;
        return o.applyStaticHook(this);
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
        MethodHandle g = getter;
        if (g == null) {
            getter = g = lookup.unreflectGetter(field);
        }
        return g;
    }

    /** {@inheritDoc} */
    @Override
    public Lookup lookup() {
        return lookup.lookup();// Temporary method
    }

    private MethodHandle of(PackedFieldOperation<?> o) {
        if (o.isSimpleGetter()) {
            return getter();
        } else {
            return setter();
        }
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle setter() {
        MethodHandle s = setter;
        if (s == null) {
            if (Modifier.isFinal(field.getModifiers())) {
                throw new UnsupportedOperationException("Field is final, cannot create a setter for the field, field = " + field);
            }
            setter = s = lookup.unreflectSetter(field);
        }
        return s;
    }

    /** {@inheritDoc} */
    @Override
    public VarHandle varHandle() {
        VarHandle vh = varHandle;
        if (vh == null) {
            varHandle = vh = lookup.unreflectVarhandle(field);
        }
        return vh;
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
}
