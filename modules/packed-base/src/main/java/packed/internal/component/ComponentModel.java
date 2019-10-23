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
import java.lang.reflect.UndeclaredThrowableException;
import java.util.IdentityHashMap;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.container.UseExtension;
import packed.internal.container.ComponentLookup;
import packed.internal.container.ContainerSourceModel;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.hook.ComponentModelHookGroup;
import packed.internal.hook.model.HookProcessor;
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
    private final ComponentModelHookGroup[] hookGroups;

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
        this.hookGroups = builder.extensionBuilders.values().stream().map(e -> e.build()).toArray(i -> new ComponentModelHookGroup[i]);
    }

    public <T> ComponentConfiguration<T> addExtensionsToContainer(PackedContainerConfiguration containerConfiguration,
            ComponentConfiguration<T> componentConfiguration) {
        // There should probably be some order we call extensions in....
        /// Other first, packed lasts?
        /// Think they need an order id....
        // Preferable deterministic
        try {
            for (ComponentModelHookGroup group : hookGroups) {
                // First make sure the extension is activated
                group.addTo(containerConfiguration, componentConfiguration);
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
        private static final ClassValue<Class<? extends Extension>[]> EXTENSION_ACTIVATORS = new ClassValue<>() {

            @Override
            protected Class<? extends Extension>[] computeValue(Class<?> type) {
                UseExtension ae = type.getAnnotation(UseExtension.class);
                return ae == null ? null : ae.value();
            }
        };

        /** The type of component we are building a model for. */
        private final Class<?> componentType;

        /** A map of builders for every activated extension. */
        private final IdentityHashMap<Class<? extends Extension>, ComponentModelHookGroup.Builder> extensionBuilders = new IdentityHashMap<>();

        private final ClassProcessor cp;

        public final HookProcessor hookController;

        /**
         * Creates a new component model builder
         * 
         * @param lookup
         *            the component lookup
         * @param componentType
         *            the type of component
         */
        public Builder(ComponentLookup lookup, Class<?> componentType) {
            this.cp = lookup.newClassProcessor(componentType, true);
            this.componentType = requireNonNull(componentType);
            this.hookController = new HookProcessor(cp, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
        }

        /**
         * Builds and returns a new model.
         * 
         * @return a new model
         */
        public ComponentModel build() {
            // Look for type annotations
            for (Annotation a : componentType.getAnnotations()) {
                Class<? extends Extension>[] extensionTypes = EXTENSION_ACTIVATORS.get(a.annotationType());
                if (extensionTypes != null) {
                    for (Class<? extends Extension> eType : extensionTypes) {
                        extensionBuilders.computeIfAbsent(eType, etype -> new ComponentModelHookGroup.Builder(this, etype)).onAnnotatedType(componentType, a);
                    }
                }
            }

            cp.findMethodsAndFields(method -> {
                for (Annotation a : method.getAnnotations()) {
                    Class<? extends Extension>[] extensionTypes = EXTENSION_ACTIVATORS.get(a.annotationType());
                    // See if the component method has any annotations that activates extensions
                    if (extensionTypes != null) {
                        for (Class<? extends Extension> eType : extensionTypes) {
                            extensionBuilders.computeIfAbsent(eType, etype -> new ComponentModelHookGroup.Builder(this, etype)).onAnnotatedMethod(method, a);
                        }
                    }
                }
            }, field -> {
                for (Annotation a : field.getAnnotations()) {
                    Class<? extends Extension>[] extensionTypes = EXTENSION_ACTIVATORS.get(a.annotationType());
                    // See if the component method has any annotations that activates extensions
                    if (extensionTypes != null) {
                        for (Class<? extends Extension> eType : extensionTypes) {
                            extensionBuilders.computeIfAbsent(eType, etype -> new ComponentModelHookGroup.Builder(this, etype)).onAnnotatedField(field, a);
                        }
                    }
                }
            });
            ComponentModel cm = new ComponentModel(this);
            hookController.close();
            return cm;
        }
    }
}
