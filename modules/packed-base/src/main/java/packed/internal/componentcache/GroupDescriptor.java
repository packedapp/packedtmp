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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.container.AnnotatedMethodHook;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerExtension;
import app.packed.container.ContainerExtensionHookGroup;
import app.packed.container.InstantiationContext;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.MethodDescriptor;
import packed.internal.componentcache.ExtensionHookGroupConfiguration.OnMethod;
import packed.internal.componentcache.ExtensionHookGroupConfiguration.OnMethodDescription;
import packed.internal.componentcache.ExtensionHookGroupConfiguration.OnMethodHandle;
import packed.internal.container.PackedContainerConfiguration;

/**
 * We have a group for a collection of hooks/annotations. A component can have multiple groups.
 */
public final class GroupDescriptor {

    @SuppressWarnings("rawtypes")
    private final BiConsumer build;

    final List<MethodConsumer<?>> consumers;

    /** The type of extension. */
    private final Class<? extends ContainerExtension<?>> extensionType;

    private GroupDescriptor(Builder b) {
        this.extensionType = requireNonNull(b.conf.extensionClass);
        this.build = requireNonNull(b.b.get());
        this.consumers = List.copyOf(b.consumers);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void add(PackedContainerConfiguration container, ComponentConfiguration component) {
        ContainerExtension extension = container.use((Class) extensionType);
        build.accept(component, extension);
    }

    static class Builder {

        final Supplier<BiConsumer<ComponentConfiguration, ?>> b;

        /** The component type */
        final Class<?> componentType;

        final ExtensionHookGroupConfiguration conf;

        private ArrayList<MethodConsumer<?>> consumers = new ArrayList<>();

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Builder(Class<?> componentType, Class<? extends ContainerExtensionHookGroup<?, ?>> cc) {
            this.componentType = requireNonNull(componentType);
            this.conf = ExtensionHookGroupConfiguration.FOR_CLASS.get(cc);
            this.b = (Supplier) conf.egc.newBuilder(componentType);
        }

        GroupDescriptor build() {
            return new GroupDescriptor(this);
        }

        void onAnnotatedField(ComponentLookup lookup, Field field, Annotation annotation) {

        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        void onAnnotatedMethod(ComponentLookup lookup, Method method, Annotation annotation) {
            for (Object o : conf.list) {
                if (o instanceof ExtensionHookGroupConfiguration.OnMethodDescription) {
                    ExtensionHookGroupConfiguration.OnMethodDescription omd = (OnMethodDescription) o;
                    if (omd.annotationType == annotation.annotationType()) {
                        ((BiConsumer) omd.consumer).accept(b, MethodDescriptor.of(method));
                    }
                } else if (o instanceof ExtensionHookGroupConfiguration.OnMethodHandle) {
                    ExtensionHookGroupConfiguration.OnMethodHandle omd = (OnMethodHandle) o;
                    if (omd.annotationType == annotation.annotationType()) {
                        method.setAccessible(true);
                        MethodHandle mh;
                        try {
                            mh = MethodHandles.lookup().unreflect(method);
                        } catch (IllegalAccessException e) {
                            throw new IllegalAccessRuntimeException("stuff", e);
                        }
                        ((BiConsumer) omd.consumer).accept(b, mh);
                    }
                } else if (o instanceof ExtensionHookGroupConfiguration.OnMethod) {
                    ExtensionHookGroupConfiguration.OnMethod omd = (OnMethod) o;
                    if (omd.annotationType == annotation.annotationType()) {
                        ((BiConsumer) omd.consumer).accept(b, new AnnotatedMethodHook() {

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
                            public MethodDescriptor method() {
                                return MethodDescriptor.of(method);
                            }

                            @Override
                            public void onMethodReady(Class key, BiConsumer consumer) {
                                requireNonNull(key, "key is null");
                                requireNonNull(consumer, "consumer is null");
                                consumers.add(new MethodConsumer<>(key, consumer, newMethodHandle()));
                            }
                        });
                    }
                }
            }
            // conf.forAnnotatedMethods();
            // MethodHandle mh = lookup.acquireMethodHandle(componentType, method);
        }
    }

    static class MethodConsumer<S> {
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
                        throw new RuntimeException(e);
                    }
                }
            };
            consumer.accept(s, r);
        }
    }
}
