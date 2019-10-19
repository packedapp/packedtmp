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
package app.packed.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.lang.InvalidDeclarationException;
import app.packed.lang.Nullable;
import app.packed.lang.reflect.FieldDescriptor;
import app.packed.lang.reflect.UncheckedIllegalAccessException;
import app.packed.lang.reflect.VarOperator;
import packed.internal.component.ComponentModel;
import packed.internal.hook.applicator.PackedFieldHookApplicator;
import packed.internal.module.AppPackedHookAccess;
import packed.internal.module.ModuleAccess;
import packed.internal.util.StringFormatter;

/**
 * A hook representing a field annotated with a specific annotation.
 * 
 * <p>
 * AnnotatedFieldHook are never safe to use by multiple threads. Furthermore something about some methods only being
 * active when building XX
 * 
 * @param <T>
 *            the type of annotation this hook matches
 **/
public final class AnnotatedFieldHook<T extends Annotation> implements Hook {

    static {
        ModuleAccess.initialize(AppPackedHookAccess.class, new AppPackedHookAccess() {

            /** {@inheritDoc} */
            @Override
            public <T extends Annotation> AnnotatedFieldHook<T> newAnnotatedFieldHook(ComponentModel.Builder builder, Field field, T annotation) {
                return new AnnotatedFieldHook<>(builder, field, annotation);
            }

            /** {@inheritDoc} */
            @Override
            public <T extends Annotation> AnnotatedMethodHook<T> newAnnotatedMethodHook(packed.internal.component.ComponentModel.Builder builder, Method method,
                    T annotation) {
                return new AnnotatedMethodHook<>(builder, method, annotation);
            }

            /** {@inheritDoc} */
            @Override
            public <T extends Annotation> AnnotatedTypeHook<T> newAnnotatedTypeHook(ComponentModel.Builder builder, Class<?> type, T annotation) {
                return new AnnotatedTypeHook<>(builder, type, annotation);
            }
        });
    }

    /** The annotation value. */
    private final T annotation;

    /** The builder for the component type. */
    private final ComponentModel.Builder builder;

    /** A field descriptor, is lazily created via {@link #field()}. */
    @Nullable
    private FieldDescriptor descriptor;

    /** The annotated field. */
    private final Field field;

    /** A method handle getter, is lazily created via {@link #getter()}. */
    @Nullable
    private MethodHandle getter;

    /** A method handle setter, is lazily created via {@link #setter()}. */
    @Nullable
    private MethodHandle setter;

    /** A var handle, is lazily created via {@link #varHandle()}. */
    @Nullable
    private VarHandle varHandle;

    /**
     * Creates a new hook instance.
     * 
     * @param builder
     *            a builder for the component type
     * @param field
     *            the annotated field
     * @param annotation
     *            the annotation value
     */
    AnnotatedFieldHook(ComponentModel.Builder builder, Field field, T annotation) {
        this.builder = requireNonNull(builder);
        this.field = requireNonNull(field);
        this.annotation = requireNonNull(annotation);
    }

    /**
     * Returns the annotation value.
     *
     * @return the annotation value
     */

    public T annotation() {
        return annotation;
    }

    public <E> HookApplicator<E> applicator(VarOperator<E> operator) {
        builder.checkActive(); // we do not want people to invoke this method, after the aggregate has been built
        return new PackedFieldHookApplicator<E>(this, operator, field);
    }

    /**
     * Applies the specified operator to the underlying field.
     * 
     * @param <E>
     *            the type of result from applying the operator
     * @param operator
     *            the operator to apply
     * @return the result from applying the operator to the static field
     * @throws UnsupportedOperationException
     *             if the underlying field is not a static field. Or if the underlying field is final, and the operator
     *             needs write access
     * @throws UncheckedIllegalAccessException
     *             if access checking failed when accessing the field
     */
    public <E> E applyStatic(VarOperator<E> operator) {
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new UnsupportedOperationException("Cannot invoke this method on a non-static field " + field);
        }
        builder.checkActive(); // we do not want people to invoke this method, after the aggregate has been built
        // Should it be per hook group instead of container model?
        return operator.applyStaticHook(this);
    }

    public AnnotatedFieldHook<T> checkAssignableTo(Class<?> type) {
        requireNonNull(type, "type is null");
        if (type.isAssignableFrom(field.getType())) {
            throw new InvalidDeclarationException("OOPS ");
        }
        return this;
    }

    public AnnotatedFieldHook<T> checkExactType(Class<?> type) {
        requireNonNull(type, "type is null");
        if (field.getType() != type) {
            throw new InvalidDeclarationException("OOPS ");
        }
        return this;
    }

    /**
     * Checks that the underlying field is final. Throwing an {@link InvalidDeclarationException} if the field is not final.
     * 
     * @return this hook
     * @throws InvalidDeclarationException
     *             if the underlying field is not final
     * 
     * @see Modifier#isFinal(int)
     */
    public AnnotatedFieldHook<T> checkFinal() {
        if (!Modifier.isFinal(field.getModifiers())) {
            throw new InvalidDeclarationException(failedModifierCheck(false, "final"));
        }
        return this;
    }

    /**
     * Checks that the underlying field is not final. Throwing an {@link InvalidDeclarationException} if the field is final.
     * 
     * @return this hook
     * @throws InvalidDeclarationException
     *             if the underlying field is final
     * 
     * @see Modifier#isFinal(int)
     */
    public AnnotatedFieldHook<T> checkNotFinal() {
        if (Modifier.isFinal(field.getModifiers())) {
            throw new InvalidDeclarationException(failedModifierCheck(true, "final"));
        }
        return this;
    }

    /**
     * Checks that the underlying field is not static. Throwing an {@link InvalidDeclarationException} if the field is
     * static.
     * 
     * @return this hook
     * @throws InvalidDeclarationException
     *             if the underlying field is static
     * 
     * @see Modifier#isStatic(int)
     */
    public AnnotatedFieldHook<T> checkNotStatic() {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new InvalidDeclarationException(failedModifierCheck(true, "static"));
        }
        return this;
    }

    /**
     * Checks that the underlying field is static. Throwing an {@link InvalidDeclarationException} if the field is not
     * static.
     * 
     * @return this hook
     * @throws InvalidDeclarationException
     *             if the underlying field is not static
     * 
     * @see Modifier#isStatic(int)
     */
    public AnnotatedFieldHook<T> checkStatic() {
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new InvalidDeclarationException(failedModifierCheck(false, "static"));
        }
        return this;
    }

    private String failedModifierCheck(boolean isNot, String type) {
        String msg = (isNot ? "not be " : "be ") + type;
        return "Fields annotated with @" + annotation.annotationType().getSimpleName() + " must " + msg + ", field = " + StringFormatter.format(field);
        // throw new InvalidDeclarationException(
        // ErrorMessageBuilder.of(field).cannot("be static when using the @" + annotationType.getSimpleName() + " annotation")
        // .toResolve("remove @" + annotationType.getSimpleName() + " or make the field non-static"));
        // //
        // // throw new InvalidDeclarationException("Cannot use @" + annotationType.getSimpleName() + " on static field: " +
        // field
        // // + ", to resolve remove @"
        // // + annotationType.getSimpleName() + " or make the field non-static");
        // Methods annotated with @Dooo cannot be static
        // Methods annotated with @Dooo must be static
        // Annotations of type @Dooo are not allowed on static methods
        // Annotations of type @Dooo are only allowed on static methods
        // throw new InvalidDeclarationException("Fields annotated with @" + annotationType.getSimpleName() + " must be final,
        // field = " + field
        // + ", to resolve remove @" + annotationType.getSimpleName() + " or make the field final");
        //
        // throw new InvalidDeclarationException(
        // ErrorMessageBuilder.of(field).cannot("be static when using the @" + annotationType.getSimpleName() + " annotation")
        // .toResolve("remove @" + annotationType.getSimpleName() + " or make the field non-static"));
        //

    }

    /**
     * Returns a descriptor for the underlying field.
     * 
     * @return a descriptor for the underlying field
     */
    public FieldDescriptor field() {
        FieldDescriptor d = descriptor;
        if (d == null) {
            descriptor = d = FieldDescriptor.of(field);
        }
        return d;
    }

    /**
     * Returns a method handle giving read access to the underlying field.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying field is an instance field.
     * 
     * @return a method handle for the underlying field with read access
     * @throws UncheckedIllegalAccessException
     *             if access checking fails
     * @see Lookup#unreflectGetter(java.lang.reflect.Field)
     */
    public MethodHandle getter() {
        MethodHandle g = getter;
        if (g == null) {
            getter = g = builder.lookup().unreflectGetter(field);
        }
        return g;
    }

    /**
     * Returns a method handle giving read access to the underlying field.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying field is an instance field.
     * 
     * @return a method handle for the underlying field with write access
     * @throws UncheckedIllegalAccessException
     *             if access checking fails
     * @throws UnsupportedOperationException
     *             if the field is final
     * @see Lookup#unreflectSetter(java.lang.reflect.Field)
     */
    public MethodHandle setter() {
        MethodHandle s = setter;
        if (s == null) {
            if (Modifier.isFinal(field.getModifiers())) {
                throw new UnsupportedOperationException("Field is final, cannot create a setter for this field, field = " + field);
            }
            setter = s = builder.lookup().unreflectSetter(field);
        }
        return s;
    }

    /**
     * Returns a {@link VarHandle} for the underlying field.
     * 
     * @return a VarHandle for the underlying field
     * @throws UncheckedIllegalAccessException
     *             if access checking fails
     * @see Lookup#unreflectVarHandle(java.lang.reflect.Field)
     */
    public VarHandle varHandle() {
        VarHandle vh = varHandle;
        if (vh == null) {
            varHandle = vh = builder.lookup().unreflectVarhandle(field);
        }
        return vh;
    }
}
