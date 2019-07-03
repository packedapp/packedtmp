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
package packed.internal.componentcache;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerExtension;
import app.packed.container.ContainerExtensionHookProcessor;
import app.packed.container.InstantiationContext;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.MethodDescriptor;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.util.ThrowableUtil;

/**
 * We have a group for a collection of hooks/annotations. A component can have multiple groups.
 */
public final class GroupDescriptor {

    @SuppressWarnings("rawtypes")
    private final BiConsumer build;

    /** The type of extension. */
    private final Class<? extends ContainerExtension<?>> extensionType;

    final List<MethodConsumer<?>> methodConsumers;

    private GroupDescriptor(Builder b) {
        this.extensionType = requireNonNull(b.conf.extensionClass);
        this.build = requireNonNull(b.b.onBuild());
        this.methodConsumers = List.copyOf(b.consumers);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void add(PackedContainerConfiguration container, ComponentConfiguration component) {
        ContainerExtension extension = container.use((Class) extensionType);
        build.accept(component, extension);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static class Builder {

        final ContainerExtensionHookProcessor<?> b;

        /** The component type */
        final Class<?> componentType;

        final HookGroup conf;

        private ArrayList<MethodConsumer<?>> consumers = new ArrayList<>();

        Builder(Class<?> componentType, Class<? extends ContainerExtensionHookProcessor<?>> cc) {
            this.componentType = requireNonNull(componentType);
            this.conf = HookGroup.FOR_CLASS.get(cc);
            this.b = conf.instantiate();
        }

        GroupDescriptor build() {
            return new GroupDescriptor(this);
        }

        void onAnnotatedField(ComponentLookup lookup, Field field, Annotation annotation) {
            conf.invokeHookOnAnnotatedField(b, new PackedAnnotatedFieldHook(lookup.lookup(), field, annotation));
        }

        void onAnnotatedMethod(ComponentLookup lookup, Method method, Annotation annotation) {
            AnnotatedMethodHook hook = new AnnotatedMethodHook() {

                @Override
                public Object annotation() {
                    return annotation;
                }

                @Override
                public Lookup lookup() {
                    return lookup.lookup();// Temporary method
                }

                @Override
                public MethodDescriptor method() {
                    return MethodDescriptor.of(method);
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

                @Override
                public void onMethodReady(Class key, BiConsumer consumer) {
                    requireNonNull(key, "key is null");
                    requireNonNull(consumer, "consumer is null");
                    // This method should definitely not be available. for ever
                    // Should we have a check configurable???
                    consumers.add(new MethodConsumer<>(key, consumer, newMethodHandle()));
                }
            };
            conf.invokeHookOnAnnotatedMethod(annotation.annotationType(), b, hook);
        }
    }
}

class MethodConsumer<S> {
    final BiConsumer<S, Runnable> consumer;
    final Class<S> key;
    final MethodHandle mh;

    /**
     * @param key
     * @param consumer
     */
    public MethodConsumer(Class<S> key, BiConsumer<S, Runnable> consumer, MethodHandle mh) {
        this.key = requireNonNull(key);
        this.consumer = requireNonNull(consumer, "consumer is null");
        this.mh = requireNonNull(mh);

    }

    void prepare(ContainerConfiguration cc, InstantiationContext ic) {
        S s = ic.use(cc, key);
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    mh.invoke();
                } catch (Throwable e) {
                    ThrowableUtil.rethrowErrorOrRuntimeException(e);
                    throw new RuntimeException(e);
                }
            }
        };
        consumer.accept(s, r);
    }
}
