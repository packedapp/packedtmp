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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.IdentityHashMap;
import java.util.Set;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.container.UseExtension;
import packed.internal.container.ComponentLookup;
import packed.internal.container.ContainerSourceModel;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.extension.ActivatorMap;
import packed.internal.hook.HookProcessor;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;

/**
 * A model of a container, an instance of this class can only be acquired via
 * {@link ContainerSourceModel#componentModelOf(Class)}.
 */

public final class ComponentModel {

    /** The component type. */
    private final Class<?> componentType;

    /** An array of any hook groups defined by the component type. */
    private final ComponentHookRequest[] hookGroups;

    /** The simple name of the component type, typically used for lazy generating a component name. */
    private volatile String simpleName;

    /**
     * Creates a new descriptor.
     * 
     * @param builder
     *            a builder for this descriptor
     */
    private ComponentModel(ComponentModel.Builder builder) {
        this.componentType = requireNonNull(builder.componentType);
        this.hookGroups = builder.extensionBuilders.values().stream().map(e -> e.build()).toArray(i -> new ComponentHookRequest[i]);
    }

    public <T> ComponentConfiguration<T> addExtensionsToContainer(PackedContainerConfiguration containerConfiguration,
            ComponentConfiguration<T> componentConfiguration) {

        // There should probably be some order we call extensions in....
        /// Other first, packed lasts?
        /// Think they need an order id....
        // Boer vaere lowest dependency id first...
        // Preferable deterministic
        try {
            for (ComponentHookRequest group : hookGroups) {
                group.process(containerConfiguration, componentConfiguration);
            }
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new UndeclaredThrowableException(e);
        }
        return componentConfiguration;
    }

    /**
     * Returns the default prefix for the component, if no name is explicitly set by the user.
     * 
     * @return the default prefix for the component, if no name is explicitly set by the user
     */
    public String defaultPrefix() {
        String s = simpleName;
        if (s == null) {
            s = simpleName = componentType.getSimpleName();
        }
        return s;
    }

    /** A builder object for a component model. */
    public static final class Builder {

        /** A cache of any extensions a particular annotation activates. */
        static final ClassValue<Set<Class<? extends Extension>>> EXTENSION_ACTIVATORS = new ClassValue<>() {

            @Override
            protected Set<Class<? extends Extension>> computeValue(Class<?> type) {
                UseExtension ae = type.getAnnotation(UseExtension.class);
                return ae == null ? null : Set.of(ae.value());
            }
        };

        private final ActivatorMap activatorMap;

        /** The type of component we are building a model for. */
        private final Class<?> componentType;

        private final ClassProcessor cp;

        final ContainerSourceModel csm;

        /** A map of builders for every activated extension. */
        private final IdentityHashMap<Class<? extends Extension>, ComponentHookRequest.Builder> extensionBuilders = new IdentityHashMap<>();

        public final HookProcessor hookProcessor;

        /**
         * Creates a new component model builder
         * 
         * @param lookup
         *            the component lookup
         * @param componentType
         *            the type of component
         */
        public Builder(ContainerSourceModel csm, ComponentLookup lookup, Class<?> componentType) {
            this.cp = lookup.newClassProcessor(componentType, true);
            this.componentType = requireNonNull(componentType);
            this.hookProcessor = new HookProcessor(cp, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            this.csm = requireNonNull(csm);
            this.activatorMap = csm.activatorMap;
        }

        /**
         * Builds and returns a new model.
         * 
         * @return a new model
         */
        public ComponentModel build() {
            // Look for type annotations
            try {
                for (Annotation a : componentType.getAnnotations()) {
                    onAnnotatedType(a, EXTENSION_ACTIVATORS.get(a.annotationType()));
                    if (activatorMap != null) {
                        onAnnotatedType(a, activatorMap.onAnnotatedType(a.annotationType()));
                    }
                }

                cp.findMethodsAndFields(method -> {
                    for (Annotation a : method.getAnnotations()) {
                        onAnnotatedMethod(a, method, EXTENSION_ACTIVATORS.get(a.annotationType()));
                        if (activatorMap != null) {
                            onAnnotatedMethod(a, method, activatorMap.onAnnotatedMethod(a.annotationType()));
                        }
                    }
                }, field -> {
                    for (Annotation a : field.getAnnotations()) {
                        onAnnotatedField(a, field, EXTENSION_ACTIVATORS.get(a.annotationType()));
                        if (activatorMap != null) {
                            onAnnotatedField(a, field, activatorMap.onAnnotatedMethod(a.annotationType()));
                        }
                    }
                });
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
            ComponentModel cm = new ComponentModel(this);
            hookProcessor.close();
            return cm;
        }

        private void onAnnotatedType(Annotation a, Set<Class<? extends Extension>> extensionTypes) throws Throwable {
            if (extensionTypes != null) {
                for (Class<? extends Extension> eType : extensionTypes) {
                    extensionBuilders.computeIfAbsent(eType, etype -> new ComponentHookRequest.Builder(hookProcessor, etype)).onAnnotatedType(componentType, a);
                }
            }
        }

        private void onAnnotatedField(Annotation a, Field field, Set<Class<? extends Extension>> extensionTypes) throws Throwable {
            if (extensionTypes != null) {
                for (Class<? extends Extension> eType : extensionTypes) {
                    extensionBuilders.computeIfAbsent(eType, etype -> new ComponentHookRequest.Builder(hookProcessor, etype)).onAnnotatedField(field, a);
                }
            }
        }

        private void onAnnotatedMethod(Annotation a, Method method, Set<Class<? extends Extension>> extensionTypes) throws Throwable {
            if (extensionTypes != null) {
                for (Class<? extends Extension> eType : extensionTypes) {
                    extensionBuilders.computeIfAbsent(eType, etype -> new ComponentHookRequest.Builder(hookProcessor, etype)).onAnnotatedMethod(method, a);
                }
            }
        }
    }
}
