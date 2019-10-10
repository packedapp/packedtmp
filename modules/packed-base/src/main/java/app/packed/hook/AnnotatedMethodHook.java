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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.reflect.MethodDescriptor;
import app.packed.reflect.MethodOperator;
import app.packed.reflect.UncheckedIllegalAccessException;
import app.packed.util.Nullable;
import packed.internal.container.model.ComponentModel;
import packed.internal.hook.applicator.PackedMethodHookApplicator;

/** A hook representing a method annotated with a specific type. */
public final class AnnotatedMethodHook<T extends Annotation> implements Hook {

    /** The annotation value. */
    private final T annotation;

    /** The builder for the component type. */
    private final ComponentModel.Builder builder;

    /** A method descriptor, is lazily created via {@link #method()}. */
    @Nullable
    private MethodDescriptor descriptor;

    /** The annotated method. */
    private final Method method;

    /** A method handle setter, is lazily created via {@link #methodHandle()}. */
    @Nullable
    private MethodHandle methodHandle;

    /**
     * Creates a new hook instance.
     * 
     * @param builder
     *            the builder for the component type
     * @param method
     *            the annotated method
     * @param annotation
     *            the annotation value
     */
    AnnotatedMethodHook(ComponentModel.Builder builder, Method method, T annotation) {
        this.builder = requireNonNull(builder);
        this.method = requireNonNull(method);
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

    public <E> HookApplicator<E> applicator(MethodOperator<E> operator) {
        builder.checkActive();
        return new PackedMethodHookApplicator<E>(this, operator, method);
    }

    /**
     * Applies the specified operator to the underlying method.
     * 
     * @param <E>
     *            the type of result from applying the operator
     * @param operator
     *            the operator to apply
     * @return the result from applying the operator to the static method
     * @throws UnsupportedOperationException
     *             if the underlying method is not a static method
     * @throws UncheckedIllegalAccessException
     *             if access checking failed while applying the operator
     */
    public <E> E applyStatic(MethodOperator<E> operator) {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Cannot invoke this method on a non-static method, method = " + method);
        }
        builder.checkActive();
        return operator.applyStaticHook(this);
    }

    /**
     * Returns a descriptor for the underlying method.
     * 
     * @return a descriptor for the underlying method
     */
    public MethodDescriptor method() {
        MethodDescriptor d = descriptor;
        if (d == null) {
            descriptor = d = MethodDescriptor.of(method);
        }
        return d;
    }

    /**
     * Returns a {@link MethodHandle} for the underlying method.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying method is an instance method.
     * 
     * @return a MethodHandle to the underlying method
     * @throws UncheckedIllegalAccessException
     *             if access checking fails
     * @see Lookup#unreflect(java.lang.reflect.Method)
     */
    public MethodHandle methodHandle() {
        MethodHandle mh = methodHandle;
        if (mh == null) {
            builder.checkActive();
            methodHandle = mh = builder.lookup().unreflect(method);
        }
        return mh;
    }
}
