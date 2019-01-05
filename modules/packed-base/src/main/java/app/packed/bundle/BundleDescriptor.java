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
import java.util.Optional;
import java.util.Set;

import app.packed.container.Container;
import app.packed.inject.Injector;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.bundle.BundleDescriptorBuilder;
import packed.internal.bundle.Bundles;
import packed.internal.inject.builder.InternalBundleDescriptor;

/**
 * An immutable bundle descriptor.
 * 
 * <p>
 * {@code BundleDescriptor} objects are immutable and safe for use by multiple concurrent threads.
 * </p>
 */
// Pretty pringting http://www.lihaoyi.com/post/CompactStreamingPrettyPrintingofHierarchicalData.html
public final class BundleDescriptor {

    // I think add @Description as annotation??? IDK

    /** The type of the bundle. */
    private final Class<? extends Bundle> bundleType;

    /** The (optional) description of the bundle. */
    // Altsaa taenker den er anderledes en hvad vil container.description lig med.
    // This bundle is xxxx
    private final @Nullable String description;

    /** A Services object. */
    private final Services services;

    private final LifecyclePoints startingPoints = null;

    private final LifecyclePoints stoppingPoints = null;

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
        this.description = builder.description;
        this.services = new Services(builder.services);
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
     * Returns an optional description of the bundle as set by {@link Bundle#setDescription(String)}.
     * 
     * @return a optional description of the bundle
     * 
     * @see Bundle#setDescription(String)
     */
    public Optional<String> bundleDescription() {
        return Optional.ofNullable(description);
    }

    /**
     * Returns the id of the bundle. If the bundle is in a named module it the name of the module concatenated with
     * {@code "." + bundleType.getSimpleName()}. If this bundle is not in a named module it is just
     * {bundleType.getSimpleName()}
     * 
     * @return the id of the bundle
     */
    public String bundleId() {
        if (getModule().isNamed()) {
            return getModule().getName() + "." + bundleType.getSimpleName();
        }
        return bundleType.getSimpleName();
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
    Module getModule() {
        return bundleType.getModule(); // Gider bi bruge denne metode????? Maaske bare skip den
    }

    /**
     * Returns the runtime type of the bundle. Is currently one of {@link Container} or {@link Injector}.
     * 
     * @return the runtime type of the bundle
     */
    public Class<?> runtimeType() {
        return ContainerBundle.class.isAssignableFrom(bundleType) ? Container.class : Injector.class;
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
    public LifecyclePoints startingPoints() {
        return startingPoints;
    }

    public LifecyclePoints stoppingPoints() {
        return stoppingPoints;
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

    public static final class LifecyclePoints {
        // Navn + Description, alternative, Map<String, Optional<String>> name+ description
        public Map<String, Optional<String>> exposed() {
            return Map.of();
        }

        public Set<String> optional() {
            return Set.of();
        }

        public Set<String> required() {
            return Set.of();
        }
    }

    /** An object representing the services the bundle exposes. As well as any required or optional services. */
    // Prototype vs singleton is actually also part of the API. Because changing a instantiation mode from
    // singleton to prototype can result in a dependent service to fail.
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
        public Services(BundleDescriptorBuilder.Services builder) {
            this.exposedServices = Map.copyOf(builder.exposed);
            this.optionalServices = requireNonNull(builder.optional);
            this.requiredServices = requireNonNull(builder.required);
        }

        /**
         * Returns an immutable map of all the services the bundle exposes.
         *
         * @return an immutable map of all the services the bundle exposes
         */
        public Map<Key<?>, ServiceDescriptor> exposed() {
            return exposedServices;
        }

        /**
         * if all exposed services in the previous services are also exposed in this services. And if all required services in
         * this are also required services in the previous.
         * 
         * @param previous
         * @return whether or not the specified service are back
         */
        public boolean isBackwardsCompatibleWith(Services previous) {
            requireNonNull(previous, "previous is null");
            if (!previous.requiredServices.containsAll(requiredServices)) {
                return false;
            }
            if (!exposedServices.keySet().containsAll(previous.exposedServices.keySet())) {
                return false;
            }
            return true;
        }

        /**
         * Returns an immutable set of all the keys for which a service that <b>must</b> be made available to the entity.
         * 
         * @return an immutable set of all keys that <b>must</b> be made available to the entity
         */
        // rename to requirements.
        public Set<Key<?>> requires() {
            return requiredServices;
        }

        /**
         * Returns an immutable set of all service keys that <b>can, but do have to</b> be made available to the entity.
         * 
         * @return an immutable set of all service keys that <b>can, but do have to</b> be made available to the entity
         */
        public Set<Key<?>> requiresOptionally() {
            return optionalServices;
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
