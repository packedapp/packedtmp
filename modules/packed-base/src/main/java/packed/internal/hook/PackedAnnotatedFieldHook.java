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
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.util.FieldDescriptor;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.TypeLiteral;
import packed.internal.container.DefaultComponentConfiguration;
import packed.internal.container.InstantiatedComponentConfiguration;
import packed.internal.util.ErrorMessageBuilder;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.descriptor.InternalFieldDescriptor;

/** The default implementation of {@link AnnotatedFieldHook}. */
final class PackedAnnotatedFieldHook<T extends Annotation> implements AnnotatedFieldHook<T> {

    /** The annotation value */
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

    /**
     * Checks that an annotated field is not final.
     * 
     * @param field
     *            the field to check
     * @param annotationType
     *            the type of annotation that forced the check
     */
    protected static void checkAnnotatedFieldIsNotFinal(InternalFieldDescriptor field, Class<? extends Annotation> annotationType) {
        if ((Modifier.isStatic(field.getModifiers()))) {

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

    /**
     * Creates an error message for using an annotation on a final field.
     *
     * @param field
     *            the field
     * @param annotationType
     *            the annotation
     * @return the error message
     */
    protected static String fieldWithAnnotationCannotBeFinal(InternalFieldDescriptor field, Class<? extends Annotation> annotationType) {
        return "Cannot use @" + annotationType.getSimpleName() + " on final field: " + field + ", to resolve remove @" + annotationType.getSimpleName()
                + " or make the field non-final";
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
    public Lookup lookup() {
        return lookup;// Temporary method
    }

    /** {@inheritDoc} */
    @Override
    public <A> A newAccessor(ComponentConfiguration cc, Class<A> accessorType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <A> A newAccessor(ComponentConfiguration cc, Class<A> accessorType, AccessMode accessMode) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <A> A newAccessor(ComponentConfiguration cc, TypeLiteral<A> accessorType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <A> A newAccessor(ComponentConfiguration cc, TypeLiteral<A> accessorType, AccessMode accessMode) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public BiPredicate<?, ?> newCompareAndSetAccessor(ComponentConfiguration cc) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <E> BiPredicate<E, E> newCompareAndSetAccessor(ComponentConfiguration cc, Class<E> fieldType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <E> BiPredicate<E, E> newCompareAndSetAccessor(ComponentConfiguration cc, TypeLiteral<E> fieldType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Supplier<?> newGetAccessor(ComponentConfiguration cc) {
        MethodHandle mh = newMethodHandleGetter();
        if (field().isStatic()) {
            return new Supplier<Object>() {

                @Override
                public Object get() {
                    try {
                        return mh.invoke();
                    } catch (Throwable e) {
                        ThrowableUtil.rethrowErrorOrRuntimeException(e);
                        throw new UndeclaredThrowableException(e);
                    }
                }
            };
        } else {
            DefaultComponentConfiguration dcc = (DefaultComponentConfiguration) cc;
            if (dcc instanceof InstantiatedComponentConfiguration) {
                Object instance = ((InstantiatedComponentConfiguration) dcc).getInstance();
                return new Supplier<Object>() {

                    @Override
                    public Object get() {
                        try {
                            return mh.invoke(instance);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
            throw new UnsupportedOperationException();
        }
    }

    /** {@inheritDoc} */
    @Override
    public <E> Supplier<E> newGetAccessor(ComponentConfiguration cc, Class<E> fieldType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <E> Supplier<E> newGetAccessor(ComponentConfiguration cc, TypeLiteral<E> fieldType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Function<?, ?> newGetAndSetAccessor(ComponentConfiguration cc) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <E> Function<? super E, E> newGetAndSetAccessor(ComponentConfiguration cc, Class<E> fieldType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <E> Function<? super E, E> newGetAndSetAccessor(ComponentConfiguration cc, TypeLiteral<E> fieldType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle newMethodHandleGetter() {
        field.setAccessible(true);
        try {
            return lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Could not create a MethodHandle", e);
        }
    }

    @Override
    public MethodHandle newMethodHandleSetter() {
        field.setAccessible(true);
        try {
            return lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Could not create a MethodHandle", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Consumer<?> newSetAccessor(ComponentConfiguration cc) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <E> Consumer<? super E> newSetAccessor(ComponentConfiguration cc, Class<E> fieldType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <E> Consumer<? super E> newSetAccessor(ComponentConfiguration cc, TypeLiteral<E> fieldType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public VarHandle newVarHandle() {
        field.setAccessible(true);
        try {
            return lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Could not create a VarHandle", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public AnnotatedFieldHook<T> optimize() {
        throw new UnsupportedOperationException();
    }
}
