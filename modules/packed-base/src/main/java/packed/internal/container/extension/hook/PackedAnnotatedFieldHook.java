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
package packed.internal.container.extension.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.HookApplicator;
import app.packed.reflect.FieldDescriptor;
import app.packed.reflect.FieldOperator;
import app.packed.util.Nullable;
import packed.internal.container.model.ComponentModel;
import packed.internal.reflect.PackedFieldOperator;
import packed.internal.util.StringFormatter;

/** The default implementation of {@link AnnotatedFieldHook}. */
final class PackedAnnotatedFieldHook<T extends Annotation> implements AnnotatedFieldHook<T> {

    /** The annotation value. */
    private final T annotation;

    /** The builder for the component type. */
    private final ComponentModel.Builder builder;

    /** A field descriptor, is lazily created via {@link #field()}. */
    @Nullable
    private FieldDescriptor descriptor;

    /** The annotated field. */
    final Field field;

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
    PackedAnnotatedFieldHook(ComponentModel.Builder builder, Field field, T annotation) {
        this.builder = requireNonNull(builder);
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
        PackedFieldOperator<E> o = PackedFieldOperator.cast(operator);
        builder.checkActive(); // we do not want people to invoke this method, after the aggregate has been built
        return new PackedFieldHookApplicator<E>(this, o);
    }

    /** {@inheritDoc} */
    @Override
    public <E> E applyStatic(FieldOperator<E> operator) {
        PackedFieldOperator<E> o = PackedFieldOperator.cast(operator);
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException("Cannot invoke this method on a non-static field " + field);
        }
        builder.checkActive(); // we do not want people to invoke this method, after the aggregate has been built
        return o.applyStaticHook(this);
    }

    public AnnotatedFieldHook<T> checkAssignableTo(Class<?> type) {
        throw new UnsupportedOperationException();
    }

    public AnnotatedFieldHook<T> checkExactType(Class<?> type) {
        throw new UnsupportedOperationException();
    }

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

    public AnnotatedFieldHook<T> checkNotStatic() {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new IllegalStateException(
                    "Fields annotated with @" + annotation.annotationType().getSimpleName() + " must not be static, field = " + StringFormatter.format(field));
        }
        // throw new InvalidDeclarationException(
        // ErrorMessageBuilder.of(field).cannot("be static when using the @" + annotationType.getSimpleName() + " annotation")
        // .toResolve("remove @" + annotationType.getSimpleName() + " or make the field non-static"));
        // //
        // // throw new InvalidDeclarationException("Cannot use @" + annotationType.getSimpleName() + " on static field: " +
        // field
        // // + ", to resolve remove @"
        // // + annotationType.getSimpleName() + " or make the field non-static");
        return this;
    }

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
            getter = g = builder.lookup().unreflectGetter(field);
        }
        return g;
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public VarHandle varHandle() {
        VarHandle vh = varHandle;
        if (vh == null) {
            varHandle = vh = builder.lookup().unreflectVarhandle(field);
        }
        return vh;
    }
}
