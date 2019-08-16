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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.BiConsumer;

import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.MethodOperation;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.MethodDescriptor;
import packed.internal.hook.ExtensionHookPerComponentGroup.MethodConsumer;

/** The default implementation of {@link AnnotatedMethodHook}. */
final class PackedAnnotatedMethodHook<T extends Annotation> implements AnnotatedMethodHook<T> {

    /** The annotation value */
    private final T annotation;

    /** The annotated method. */
    private final Method method;

    /** A cached method descriptor, is lazily created via {@link #method()}. */
    private volatile MethodDescriptor descriptor;

    /** A lookup object used to create various handlers. */
    private final Lookup lookup;

    private final ArrayList<MethodConsumer<?>> consumers;

    PackedAnnotatedMethodHook(Lookup lookup, Method method, T annotation, ArrayList<MethodConsumer<?>> consumers) {
        this.lookup = requireNonNull(lookup);
        this.method = requireNonNull(method);
        this.annotation = requireNonNull(annotation);
        this.consumers = requireNonNull(consumers);
    }

    /** {@inheritDoc} */
    @Override
    public T annotation() {
        return annotation;
    }

    @Override
    public Lookup lookup() {
        return lookup;// Temporary method
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

    @Override
    public MethodHandle newMethodHandle() {
        method.setAccessible(true);
        try {
            return MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("stuff", e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void onMethodReady(Class key, BiConsumer consumer) {
        requireNonNull(key, "key is null");
        requireNonNull(consumer, "consumer is null");
        // This method should definitely not be available. for ever
        // Should we have a check configurable???
        consumers.add(new MethodConsumer<>(key, consumer, newMethodHandle()));
    }

    /** {@inheritDoc} */
    @Override
    public <E> E accessStatic(MethodOperation<E> accessor) {
        throw new UnsupportedOperationException();
    }
}
