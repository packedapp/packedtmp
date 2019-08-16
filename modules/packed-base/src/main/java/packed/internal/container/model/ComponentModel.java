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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.stream.Stream;

import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.container.ActivateExtension;
import app.packed.container.Extension;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.hook.ExtensionHookPerComponentGroup;
import packed.internal.hook.ExtensionHookPerComponentGroup.MethodConsumer;

/**
 *
 */
public final class ComponentModel {

    /** The component type. */
    private final Class<?> componentType;

    private final ExtensionHookPerComponentGroup[] extensionGroups;

    /** The simple name of the component type. */
    private volatile String simpleName;

    /**
     * Creates a new descriptor.
     * 
     * @param builder
     *            a builder for this descriptor
     */
    private ComponentModel(ComponentModel.Builder builder) {
        this.componentType = requireNonNull(builder.componentType);
        this.extensionGroups = builder.extensionBuilders.values().stream().map(e -> e.build()).toArray(i -> new ExtensionHookPerComponentGroup[i]);
    }

    /**
     * Returns the default prefix for the container, if no name is explicitly set.
     * 
     * @return the default prefix for the container, if no name is explicitly set
     */
    public String defaultPrefix() {
        String s = simpleName;
        if (s == null) {
            s = simpleName = componentType.getSimpleName();
        }
        return s;
    }

    public ComponentConfiguration initialize(PackedContainerConfiguration containerConfiguration, ComponentConfiguration componentConfiguration) {
        for (ExtensionHookPerComponentGroup c : extensionGroups) {
            c.add(containerConfiguration, componentConfiguration);
        }
        return componentConfiguration;
    }

    @SuppressWarnings("rawtypes")
    public void process(PackedContainerConfiguration cc, ArtifactInstantiationContext ic) {
        for (ExtensionHookPerComponentGroup d : extensionGroups) {
            for (MethodConsumer mc : d.methodConsumers) {
                mc.prepare(cc, ic);
            }
        }
    }

    public void print() {
        System.out.println("ComponentType = " + componentType + ", callbacks = " + Stream.of(extensionGroups).mapToInt(e -> e.getNumberOfCallbacks()).sum());
    }

    /**
     * Returns the type of component.
     * 
     * @return the type of component
     */
    public Class<?> type() {
        return componentType;
    }

    /** A builder object for a component class descriptor. */
    static class Builder {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<Class<? extends Extension>[]> EXTENSION_ACTIVATORS = new ClassValue<>() {

            @Override
            protected Class<? extends Extension>[] computeValue(Class<?> type) {
                ActivateExtension ae = type.getAnnotation(ActivateExtension.class);
                return ae == null ? null : ae.value();
            }
        };

        /** The component type. */
        private final Class<?> componentType;

        /** A map of builders for every activated extension. */
        private final IdentityHashMap<Class<? extends Extension>, ExtensionHookPerComponentGroup.Builder> extensionBuilders = new IdentityHashMap<>();

        /** A lookup object for the component. */
        private final ComponentLookup lookup;

        /**
         * @param lookup
         * @param componentType
         */
        Builder(ComponentLookup lookup, Class<?> componentType) {
            this.lookup = requireNonNull(lookup);
            this.componentType = requireNonNull(componentType);
        }

        /**
         * Builds and returns a new descriptor.
         * 
         * @return a new descriptor
         */
        ComponentModel build() {
            for (Class<?> c = componentType; c != Object.class; c = c.getSuperclass()) {

                for (Field field : c.getDeclaredFields()) {
                    for (Annotation a : field.getAnnotations()) {
                        Class<? extends Extension>[] cc = EXTENSION_ACTIVATORS.get(a.annotationType());
                        if (cc != null) {
                            for (Class<? extends Extension> ccc : cc) {
                                extensionBuilders
                                        .computeIfAbsent(ccc, extensionType -> new ExtensionHookPerComponentGroup.Builder(componentType, extensionType, lookup))
                                        .onAnnotatedField(field, a);
                            }
                        }
                    }
                }
                for (Method method : c.getDeclaredMethods()) {
                    for (Annotation a : method.getAnnotations()) {
                        Class<? extends Extension> cc[] = EXTENSION_ACTIVATORS.get(a.annotationType());
                        if (cc != null) {
                            for (Class<? extends Extension> ccc : cc) {
                                extensionBuilders
                                        .computeIfAbsent(ccc, extensionType -> new ExtensionHookPerComponentGroup.Builder(componentType, extensionType, lookup))
                                        .onAnnotatedMethod(method, a);
                            }
                        }
                    }
                }
                // TODO default methods
            }
            return new ComponentModel(this);
        }
    }
}
