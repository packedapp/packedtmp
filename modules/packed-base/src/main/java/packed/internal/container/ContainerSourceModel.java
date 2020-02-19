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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import app.packed.artifact.ArtifactSource;
import app.packed.base.InvalidDeclarationException;
import app.packed.base.Nullable;
import app.packed.container.Bundle;
import app.packed.container.ContainerComposer;
import app.packed.container.Extension;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import packed.internal.component.ComponentModel;
import packed.internal.hook.OnHookModel;
import packed.internal.inject.factory.ExecutableFactoryHandle;
import packed.internal.inject.factory.FactoryHandle;
import packed.internal.reflect.OpenClass;
import packed.internal.util.LookupValue;
import packed.internal.util.UncheckedThrowableFactory;

/** A model of a container source, typically a subclass of {@link Bundle}. */
public final class ContainerSourceModel extends ComponentLookup {

    /** A cache of model. */
    private static final ClassValue<ContainerSourceModel> MODEL_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ContainerSourceModel computeValue(Class<?> type) {
            return new ContainerSourceModel((Class<? extends ArtifactSource>) type);
        }
    };

    @Nullable
    public final LazyExtensionActivationMap activatorMap;

    /** A cache of component models that have been accessed without a lookup object. */
    // most likely they will have the same class loader as the container source
    private final ClassValue<ComponentModel> componentsNoLookup = new ClassValue<>() {

        @Override
        protected ComponentModel computeValue(Class<?> type) {
            return ComponentModel.newInstance(ContainerSourceModel.this, ContainerSourceModel.this.newClassProcessor(type, true));
        }
    };

    /** The default lookup object, when the user has specified no Lookup value */
    private ComponentLookup defaultLookup;

    final List<Class<? extends Extension>> dependenciesTotalOrder;

    /** A cache of lookup values, in 99 % of all cases this will hold no more than 1 value. */
    private final LookupValue<PerLookup> lookups = new LookupValue<>() {

        @Override
        protected PerLookup computeValue(Lookup lookup) {
            return new PerLookup(ContainerSourceModel.this, lookup);
        }
    };

    /** Any methods annotated with {@link OnHook} on the container source. */
    @Nullable
    private final OnHookModel onHookModel;

    /** The type of container source. Typically, a subclass of {@link Bundle}. */
    private final Class<? extends ArtifactSource> sourceType;

    /**
     * Creates a new container source model.
     * 
     * @param sourceType
     *            the source type
     */
    private ContainerSourceModel(Class<? extends ArtifactSource> sourceType) {
        if (Hook.class.isAssignableFrom(sourceType)) {
            throw new InvalidDeclarationException(sourceType + " must not implement/extend " + Hook.class);
        }
        this.sourceType = requireNonNull(sourceType);

        this.onHookModel = OnHookModel.newModel(new OpenClass(MethodHandles.lookup(), sourceType, true), false,
                UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY, ContainerComposer.class);
        this.activatorMap = LazyExtensionActivationMap.of(sourceType);
        this.dependenciesTotalOrder = ExtensionUseModel2.totalOrder(sourceType);
    }

    /** {@inheritDoc} */
    @Override
    ComponentModel componentModelOf(Class<?> componentType) {
        return componentsNoLookup.get(componentType);
    }

    /**
     * If the underlying container source has any methods annotated with {@link OnHook} return the model. Otherwise returns
     * null.
     * 
     * @return any hook model
     */
    @Nullable
    public OnHookModel hooks() {
        return onHookModel;
    }

    /** {@inheritDoc} */
    @Override
    OpenClass newClassProcessor(Class<?> clazz, boolean registerNatives) {
        return new OpenClass(MethodHandles.lookup(), clazz, registerNatives);
    }

    @Override
    <T> FactoryHandle<T> readable(FactoryHandle<T> factory) {
        // TODO needs to cached
        // TODO add field...
        if (factory instanceof ExecutableFactoryHandle) {
            ExecutableFactoryHandle<T> e = (ExecutableFactoryHandle<T>) factory;
            if (!e.hasMethodHandle()) {
                return e.withLookup(MethodHandles.lookup());
            }
        }
        return factory;
    }

    public Class<? extends ArtifactSource> sourceType() {
        return sourceType;
    }

    ComponentLookup withLookup(Lookup lookup) {
        // Use default access (this) if we specify null lookup

        // We need to check this in a separate class. Because from Java 13.
        // There are two classes in a lookup object.
        if (lookup == null) {
            return this;
        } else if (lookup.lookupClass() == sourceType && lookup.lookupModes() == 3) {
            ComponentLookup cl = defaultLookup;
            if (cl != null) {
                return cl;
            }
            return defaultLookup = lookups.get(lookup);
        }
        return lookups.get(lookup);
    }

    /**
     * Returns a container source model for the specified type
     * 
     * @param sourceType
     *            the container source type
     * @return a container source model for the specified type
     */
    public static ContainerSourceModel of(Class<? extends ArtifactSource> sourceType) {
        return MODEL_CACHE.get(sourceType);
    }

    /** A component lookup class wrapping a {@link Lookup} object. */
    private static final class PerLookup extends ComponentLookup {

        /** A cache of component class descriptors. */
        private final ClassValue<ComponentModel> components = new ClassValue<>() {

            @Override
            protected ComponentModel computeValue(Class<?> type) {
                return ComponentModel.newInstance(parent, PerLookup.this.newClassProcessor(type, true));
            }
        };

        /** The actual lookup object we are wrapping. */
        private final Lookup lookup;

        private final ContainerSourceModel parent;

        private PerLookup(ContainerSourceModel parent, Lookup lookup) {
            this.parent = requireNonNull(parent);
            this.lookup = requireNonNull(lookup);
        }

        /** {@inheritDoc} */
        @Override
        ComponentModel componentModelOf(Class<?> componentType) {
            return components.get(componentType);
        }

        /** {@inheritDoc} */
        @Override
        OpenClass newClassProcessor(Class<?> clazz, boolean registerNatives) {
            return new OpenClass(lookup, clazz, registerNatives);
        }

        @Override
        <T> FactoryHandle<T> readable(FactoryHandle<T> factory) {
            // TODO needs to cached
            // TODO add field...
            if (factory instanceof ExecutableFactoryHandle) {
                ExecutableFactoryHandle<T> e = (ExecutableFactoryHandle<T>) factory;
                if (!e.hasMethodHandle()) {
                    return e.withLookup(lookup);
                }
            }
            return factory;
        }
    }
}

// Maaske havde Factory.open() istedet for
// Factory.withMethodHandle <- Only for construction of Object????... We are more or less opening it

// Via bundle.lookup(MethodHandle) //Hvordan faar vi installeret @Install Bundle, @ComponentScan.. lookup must be first
// command in a bundle.... Otherwise fail..
// Via MethodHandle on SupportClass
// Via Bundle.class
