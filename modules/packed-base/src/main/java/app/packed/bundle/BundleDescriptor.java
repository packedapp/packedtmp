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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Set;

import app.packed.inject.Key;
import app.packed.inject.ServiceDescriptor;
import packed.internal.bundle.BundleDescriptorBuilder;
import packed.internal.inject.builder.InternalBundleDescriptor;

/**
 * An immutable bundle descriptor.
 * 
 * <p>
 * {@code BundleDescriptor} objects are immutable and safe for use by multiple concurrent threads.
 * </p>
 */
public final class BundleDescriptor {

    /** The type of the bundle. */
    private final Class<? extends Bundle> bundleType;

    /** A Services object. */
    private final Services services;

    private StartingPoints startingPoints = null;

    /**
     * Creates a new descriptor.
     * 
     * @param bundleType
     *            the type of the bundle
     * @param builder
     *            a builder object
     */
    BundleDescriptor(Class<? extends Bundle> bundleType, BundleDescriptorBuilder builder) {
        this.bundleType = bundleType;
        this.services = new Services(builder);
    }

    /**
     * Returns any annotations that are present on the bundle. For example, {@link Deprecated}
     * 
     * @return any annotations that are present on the bundle
     */
    public AnnotatedElement annotations() {
        return bundleType;
    }

    /**
     * Returns the type of the bundle.
     *
     * @return the type of the bundle
     */
    public Class<? extends Bundle> bundleType() {
        return bundleType;
    }

    /**
     * Returns the module that the bundle is a member of. This is normally, the module in which the class extending
     * {@link Bundle} is located.
     * <p>
     * If the bundle is in an unnamed module then the {@linkplain ClassLoader#getUnnamedModule() unnamed} {@code Module} of
     * the class loader for the bundle implementation is returned.
     *
     * @return the module that the bundle is a member of
     * @see Class#getModule()
     */
    public Module getModule() {
        // Gider bi bruge denne metode????? Maaske bare skip den
        return bundleType.getModule();
    }

    /**
     * Return a {@link Services} object representing the services the bundle exposes. As well as any required or optional
     * services.
     * 
     * @return a services object
     */
    public Services services() {
        return services;
    }

    // Er detn bare tom for en injector bundle???? Det er den vel
    public StartingPoints startingPoints() {
        return startingPoints;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("bundle { type: ").append(format(bundleType));
        if (getModule().isNamed()) {
            sb.append(", module: ").append(getModule().getName());
        }

        sb.append(" }");
        return sb.toString();
    }

    /**
     * Returns a descriptor for the specified bundle.
     *
     * @param bundle
     *            the bundle to return a descriptor for
     * @return a descriptor for the specified bundle
     */
    public static BundleDescriptor of(Bundle bundle) {
        requireNonNull(bundle, "bundle is null");
        return new BundleDescriptor(bundle.getClass(), InternalBundleDescriptor.of(bundle));
    }

    public static BundleDescriptor of(Class<? extends Bundle> bundleType) {
        return of(Bundles.instantiate(bundleType));
    }

    public static final class StartingPoints {}

    public static final class StoppingPoints {}

    /** An object representing the services the bundle exposes. As well as any required or optional services. */
    public static final class Services {

        /** An immutable map of all the services the bundle exposes. */
        private final Map<Key<?>, ServiceDescriptor> exposedServices;

        /** A set of all optional service keys. */
        private final Set<Key<?>> optionalServices;

        /** A set of all required service keys. */
        private final Set<Key<?>> requiredServices;

        /**
         * Creates a new Services object
         * 
         * @param builder
         *            the builder object
         */
        Services(BundleDescriptorBuilder builder) {
            this.exposedServices = Map.copyOf(builder.exposedServices);
            this.optionalServices = requireNonNull(builder.optionalServices);
            this.requiredServices = requireNonNull(builder.requiredServices);
        }

        /**
         * Returns an immutable map of all the services the bundle exposes.
         *
         * @return an immutable map of all the services the bundle exposes
         */
        public Map<Key<?>, ServiceDescriptor> exposedServices() {
            return exposedServices;
        }

        /**
         * Returns an immutable set of all service keys that <b>can, but do have to</b> be made available to the entity.
         * 
         * @return an immutable set of all service keys that <b>can, but do have to</b> be made available to the entity
         */
        public Set<Key<?>> optionalServices() {
            return optionalServices;
        }

        /**
         * Returns an immutable set of all service keys that <b>must</b> be made available to the entity.
         * 
         * @return an immutable set of all service keys that <b>must</b> be made available to the entity
         */
        public Set<Key<?>> requiredServices() {
            return requiredServices;
        }
    }

    // Det gode ved at have en SPEC_VERSION, er at man kan specificere man vil bruge.
    // Og dermed kun importere praecis de interfaces den definere...
    // Deploy(someSpec?) ved ikke lige med API'en /
    // FooBarBundle.API$2_2
    // FooBarBundle.API$2_3-SNAPSHOT hmmm, saa forsvinder den jo naar man releaser den???
    // Maaske hellere have den markeret med @Preview :D
    /// Bundlen, kan maaske endda supportere flere versioner??Som i flere versioner??

    // The union of exposedServices, optionalService and requiredService must be empty
}
