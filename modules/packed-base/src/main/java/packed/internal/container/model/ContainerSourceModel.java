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

import app.packed.container.Bundle;
import app.packed.container.ContainerSource;
import packed.internal.util.LookupValue;

/** A model of a container source, typically a subclass of {@link Bundle}. */
public final class ContainerSourceModel implements ComponentLookup {

    /** A cache of model. */
    private static final ClassValue<ContainerSourceModel> MODEL_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ContainerSourceModel computeValue(Class<?> type) {
            return new ContainerSourceModel((Class<? extends ContainerSource>) type);
        }
    };

    /** A cache of component models that have been accessed without a lookup object. */
    // most likely they will have the same class loader as the container source
    private final ClassValue<ComponentModel> componentsNoLookup = new ClassValue<>() {

        @Override
        protected ComponentModel computeValue(Class<?> type) {
            return new ComponentModel.Builder(ContainerSourceModel.this, type).build();
        }
    };

    /** The default lookup object, when the user has specified no Lookup value */
    ComponentLookup defaultLookup;

    /** The default prefix of the container, used if no name has been specified. */
    private volatile String defaultPrefix;

    /** A cache of lookup values, in 99 % of all cases this will hold no more than 1 value. */
    private final LookupValue<PerLookup> lookups = new LookupValue<>() {

        @Override
        protected PerLookup computeValue(Lookup lookup) {
            return new PerLookup(ContainerSourceModel.this, lookup);
        }
    };

    /** The type of container source. Typically, a subclass of {@link Bundle}. */
    private final Class<? extends ContainerSource> sourceType;

    /**
     * Creates a new container source model.
     * 
     * @param sourceType
     *            the source type
     */
    private ContainerSourceModel(Class<? extends ContainerSource> sourceType) {
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
        // Should we return a private lookup???
        // TODO fix, this method does not work
        return MethodHandles.lookup();
    }

    public ComponentLookup withLookup(Lookup lookup) {
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
    public static ContainerSourceModel of(Class<? extends ContainerSource> sourceType) {
        return MODEL_CACHE.get(sourceType);
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

        final ContainerSourceModel parent;

        public PerLookup(ContainerSourceModel parent, Lookup lookup) {
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
