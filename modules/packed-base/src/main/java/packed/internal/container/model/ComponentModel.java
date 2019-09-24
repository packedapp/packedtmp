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
package packed.internal.container.model;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.IdentityHashMap;

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.ActivateExtension;
import app.packed.container.extension.Extension;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.reflect.MemberProcessor;
import packed.internal.util.ThrowableUtil;

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

    public ComponentConfiguration addExtensionsToContainer(PackedContainerConfiguration containerConfiguration, ComponentConfiguration componentConfiguration) {
        try {
            for (ComponentModelHookGroup group : hookGroups) {
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
                ActivateExtension ae = type.getAnnotation(ActivateExtension.class);
                return ae == null ? null : ae.value();
            }
        };

        /** The type of component we are building a model for. */
        private final Class<?> componentType;

        /** A map of builders for every activated extension. */
        private final IdentityHashMap<Class<? extends Extension>, ComponentModelHookGroup.Builder> extensionBuilders = new IdentityHashMap<>();

        private boolean isFinished;

        /** A lookup object for the component. */
        private final ComponentLookup lookup;

        /**
         * Creates a new component model builder
         * 
         * @param lookup
         *            the component lookup
         * @param componentType
         *            the type of component
         */
        Builder(ComponentLookup lookup, Class<?> componentType) {
            this.lookup = requireNonNull(lookup);
            this.componentType = requireNonNull(componentType);
        }

        /**
         * Builds and returns a new model.
         * 
         * @return a new model
         */
        ComponentModel build() {
            // Runs through every interesting field and method.
            MemberProcessor.processMethodsAndFields(Object.class, componentType, method -> {
                for (Annotation a : method.getAnnotations()) {
                    Class<? extends Extension>[] extensionTypes = EXTENSION_ACTIVATORS.get(a.annotationType());
                    if (extensionTypes != null) {
                        for (Class<? extends Extension> eType : extensionTypes) {
                            extensionBuilders.computeIfAbsent(eType, k -> new ComponentModelHookGroup.Builder(this, k)).onAnnotatedMethod(method, a);
                        }
                    }
                }
            }, field -> {
                for (Annotation a : field.getAnnotations()) {
                    Class<? extends Extension>[] extensionTypes = EXTENSION_ACTIVATORS.get(a.annotationType());
                    if (extensionTypes != null) {
                        for (Class<? extends Extension> eType : extensionTypes) {
                            extensionBuilders.computeIfAbsent(eType, k -> new ComponentModelHookGroup.Builder(this, k)).onAnnotatedField(field, a);
                        }
                    }
                }
            });
            ComponentModel cm = new ComponentModel(this);
            isFinished = true;
            return cm;
        }

        public void checkActive() {
            if (isFinished) {
                throw new IllegalStateException("This method cannot be called after the component type has processed");
            }
        }

        public ComponentLookup lookup() {
            return lookup;
        }
    }
}
