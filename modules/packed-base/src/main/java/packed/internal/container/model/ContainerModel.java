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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import app.packed.artifact.ArtifactSource;
import app.packed.container.Bundle;
import packed.internal.util.LookupValue;

/** A model of a container. */
public final class ContainerModel implements ComponentLookup {

    /** A cache of values. */
    private static final ClassValue<ContainerModel> CONTAINER_MODEL_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ContainerModel computeValue(Class<?> type) {
            return new ContainerModel((Class<? extends ArtifactSource>) type);
        }
    };

    /** A cache of component models that have been access without a lookup object. */
    private final ClassValue<ComponentModel> componentsNoLookup = new ClassValue<>() {

        @Override
        protected ComponentModel computeValue(Class<?> type) {
            return new ComponentModel.Builder(ContainerModel.this, type).build();
        }
    };

    /** The default prefix of the container, used if no name has been specified. */
    private volatile String defaultPrefix;

    /** A cache of lookup values, in 99 % of all cases this will hold no more than 1 value. */
    private final LookupValue<PerLookup> lookups = new LookupValue<>() {

        @Override
        protected PerLookup computeValue(Lookup lookup) {
            return new PerLookup(ContainerModel.this, lookup);
        }
    };

    /** The type of container source. For example, a subclass of {@link Bundle}. */
    private final Class<? extends ArtifactSource> sourceType;

    /**
     * Creates a new container model.
     * 
     * @param sourceType
     *            the source type
     */
    private ContainerModel(Class<? extends ArtifactSource> sourceType) {
        this.sourceType = requireNonNull(sourceType);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle acquireMethodHandle(Class<?> componentType, Method method) {
        try {
            Lookup lookup = MethodHandles.lookup();
            return lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModel componentModelOf(Class<?> componentType) {
        return componentsNoLookup.get(componentType);
    }

    /**
     * Returns the default prefix for the container, if no name is explicitly set.
     * 
     * @return the default prefix for the container, if no name is explicitly set
     */
    public String defaultPrefix() {
        String d = defaultPrefix;
        if (d == null) {
            d = defaultPrefix = sourceType.getSimpleName();
        }
        return d;
    }

    /** {@inheritDoc} */
    @Override
    public Lookup lookup() {
        // TODO fix, this method does not work
        return MethodHandles.lookup();
    }

    /**
     * @return the configuratorType
     */
    public Class<?> sourceType() {
        return sourceType;
    }

    public ComponentLookup withLookup(Lookup lookup) {
        return lookup == null ? this : lookups.get(lookup);
    }

    /**
     * Returns a bundle class cache object for the specified bundle type.
     * 
     * @param sourceType
     *            the bundle type to return a class cache object for
     * @return a bundle class cache object for the specified bundle type
     */
    public static ContainerModel of(Class<? extends ArtifactSource> sourceType) {
        return CONTAINER_MODEL_CACHE.get(sourceType);
    }

    static final class PerLookup implements ComponentLookup {

        /** A cache of component class descriptors. */
        final ClassValue<ComponentModel> components = new ClassValue<>() {

            @Override
            protected ComponentModel computeValue(Class<?> type) {
                return new ComponentModel.Builder(PerLookup.this, type).build();
            }
        };

        /** The actual lookup object we are wrapping. */
        private final Lookup lookup;

        final ContainerModel parent;

        public PerLookup(ContainerModel parent, Lookup lookup) {
            this.parent = requireNonNull(parent);
            this.lookup = requireNonNull(lookup);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle acquireMethodHandle(Class<?> componentType, Method method) {
            try {
                return lookup.unreflect(method);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        /** {@inheritDoc} */
        @Override
        public ComponentModel componentModelOf(Class<?> componentType) {
            return components.get(componentType);
        }

        /** {@inheritDoc} */
        @Override
        public Lookup lookup() {
            return lookup;
        }
    }
}

// Maaske havde Factory.open() istedet for
// Factory.withMethodHandle <- Only for construction of Object????... We are more or less opening it

// Via bundle.lookup(MethodHandle) //Hvordan faar vi installeret @Install Bundle, @ComponentScan.. lookup must be first
// command in a bundle.... Otherwise fail..
// Via MethodHandle on SupportClass
// Via Bundle.class
