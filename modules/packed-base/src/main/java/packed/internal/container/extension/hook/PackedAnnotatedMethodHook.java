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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.HookApplicator;
import app.packed.container.extension.MethodOperator;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;
import packed.internal.container.model.ComponentModel;

/** The default implementation of {@link AnnotatedMethodHook}. */
final class PackedAnnotatedMethodHook<T extends Annotation> implements AnnotatedMethodHook<T> {

    /** The annotation value. */
    private final T annotation;

    /** The builder for the component type. */
    private final ComponentModel.Builder builder;

    /** A method descriptor, is lazily created via {@link #method()}. */
    @Nullable
    private MethodDescriptor descriptor;

    /** The annotated method. */
    final Method method;

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
    PackedAnnotatedMethodHook(ComponentModel.Builder builder, Method method, T annotation) {
        this.builder = requireNonNull(builder);
        this.method = requireNonNull(method);
        this.annotation = requireNonNull(annotation);
    }

    /** {@inheritDoc} */
    @Override
    public T annotation() {
        return annotation;
    }

    /** {@inheritDoc} */
    @Override
    public <E> HookApplicator<E> applicator(MethodOperator<E> operator) {
        PackedMethodOperation<E> o = PackedMethodOperation.cast(operator);
        builder.checkActive();
        return new PackedMethodRuntimeAccessor<E>(this, o);
    }

    /** {@inheritDoc} */
    @Override
    public <E> E applyOnStaticMethod(MethodOperator<E> operator) {
        PackedMethodOperation<E> o = PackedMethodOperation.cast(operator);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Cannot invoke this method on a non-static method, method = " + method);
        }
        builder.checkActive();
        return o.applyStaticHook(this);
    }

    @Override
    public Lookup lookup() {
        return builder.lookup().lookup();// Temporary method
    }

    /** {@inheritDoc} */
    @Override
    public MethodDescriptor method() {
        MethodDescriptor d = descriptor;
        if (d == null) {
            descriptor = d = MethodDescriptor.of(method);
        }
        return d;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle methodHandle() {
        MethodHandle mh = methodHandle;
        if (mh == null) {
            methodHandle = mh = builder.lookup().unreflect(method);
        }
        return mh;
    }
}
