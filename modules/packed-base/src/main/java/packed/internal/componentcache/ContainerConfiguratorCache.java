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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import app.packed.artifact.ArtifactSource;
import app.packed.container.ContainerBundle;
import packed.internal.util.LookupValue;

/** A cache for a bundle implementation. */
public final class ContainerConfiguratorCache implements ComponentLookup {

    /** A cache of values. */
    private static final ClassValue<ContainerConfiguratorCache> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected ContainerConfiguratorCache computeValue(Class<?> type) {
            return new ContainerConfiguratorCache(type);
        }
    };

    /** The type of bundle. */
    private final Class<?> configuratorType;

    /** The default prefix of the container. */
    private volatile String defaultPrefix;

    /** A cache of component class descriptors. */
    private final ClassValue<ComponentClassDescriptor> descriptors = new ClassValue<>() {

        @Override
        protected ComponentClassDescriptor computeValue(Class<?> type) {
            return new ComponentClassDescriptor.Builder(ContainerConfiguratorCache.this, type).build();
        }
    };

    /** A cache of lookup values, in 99 % of all cases this will hold no more than 1 value. */
    private final LookupValue<PerLookup> LOOKUP_CACHE = new LookupValue<>() {

        @Override
        protected PerLookup computeValue(Lookup lookup) {
            return new PerLookup(ContainerConfiguratorCache.this, lookup);
        }
    };

    /**
     * Creates a new bundle class cache.
     * 
     * @param configuratorType
     *            the configurator type
     */
    private ContainerConfiguratorCache(Class<?> configuratorType) {
        this.configuratorType = requireNonNull(configuratorType);
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
    public ComponentClassDescriptor componentDescriptorOf(Class<?> componentType) {
        return descriptors.get(componentType);
    }

    /**
     * Returns the default prefix for the container, if no name is explicitly set.
     * 
     * @return the default prefix for the container, if no name is explicitly set
     */
    public String defaultPrefix() {
        String d = defaultPrefix;
        if (d == null) {
            d = defaultPrefix = configuratorType.getSimpleName();
        }
        return d;
    }

    /**
     * @return the configuratorType
     */
    public Class<?> getConfiguratorType() {
        return configuratorType;
    }

    /** {@inheritDoc} */
    @Override
    public Lookup lookup() {
        // TODO fix, this method does not work
        return MethodHandles.lookup();
    }

    public ComponentLookup withLookup(Lookup lookup) {
        return lookup == null ? this : LOOKUP_CACHE.get(lookup);
    }

    public static ContainerConfiguratorCache get(ArtifactSource source) {
        if (source instanceof ContainerBundle) {
            return CACHE.get(source.getClass());
        } else {
            throw new UnsupportedOperationException();
            // return CACHE.get(((InjectorConfiguratorContainerSource) source).getType());
        }
    }

    /**
     * Returns a bundle class cache object for the specified bundle type.
     * 
     * @param configuratorType
     *            the bundle type to return a class cache object for
     * @return a bundle class cache object for the specified bundle type
     */
    public static ContainerConfiguratorCache of(Class<?> configuratorType) {
        return CACHE.get(configuratorType);
    }

    static final class PerLookup implements ComponentLookup {

        /** A cache of component class descriptors. */
        final ClassValue<ComponentClassDescriptor> descriptors = new ClassValue<>() {

            @Override
            protected ComponentClassDescriptor computeValue(Class<?> type) {
                return new ComponentClassDescriptor.Builder(PerLookup.this, type).build();
            }
        };

        /** The actual lookup object we are wrapping. */
        private final Lookup lookup;

        final ContainerConfiguratorCache parent;

        public PerLookup(ContainerConfiguratorCache parent, Lookup lookup) {
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
        public ComponentClassDescriptor componentDescriptorOf(Class<?> componentType) {
            return descriptors.get(componentType);
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
