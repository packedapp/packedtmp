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

import java.lang.annotation.Annotation;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Version;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.packed.container.Container;
import app.packed.inject.Injector;
import app.packed.inject.InjectorBundleDescriptor.Services;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;
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
// Abstract Bundle Descriptor

// Description, Tags, runtimeType = {Container/Injector}, BundleFactory.class, Descriptor=InjectorBundleDescriptor
// (maaske faas den fra BundleFactory)
//Bundles do now support selectively deciding which bundles can import other bundles.
//This is supported by modularity.
//For example, that only ZBundle can import CXbundle. This is modules..
/**
 * A bundle descriptor.
 *
 * <p>
 * A bundle descriptor describes a bundle and defines methods to obtain each of its components. The bundle descriptor
 * for a bundle is obtained by invoking the {@link java.lang.Module Module}'s {@link java.lang.Module#getDescriptor
 * getDescriptor} method.
 *
 * <p>
 * In other words a bundle must provide descriptors that are equivalent on each run.
 */
public class BundleDescriptor {

    // I think add @Description as annotation??? IDK

    /** The (optional) description of the bundle. */
    private final @Nullable String bundleDescription;

    /** The type of the bundle. */
    private final Class<? extends Bundle> bundleType;

    /** A Services object. */
    private final Services services;

    private final LifecyclePoints startingPoints = null;

    private final LifecyclePoints stoppingPoints = null;

    /**
     * Creates a new descriptor from the specified builder.
     * 
     * @param builder
     *            a builder object
     */
    protected BundleDescriptor(BundleDescriptor.Builder builder) {
        requireNonNull(builder, "builder is null");
        this.bundleType = builder.bundleType();
        this.bundleDescription = builder.getBundleDescription();
        this.services = new Services(builder);
    }

    /**
     * Returns an optional description of the bundle as set by {@link Bundle#setDescription(String)}.
     * 
     * @return a optional description of the bundle
     * 
     * @see Bundle#setDescription(String)
     */
    public final Optional<String> bundleDescription() {
        return Optional.ofNullable(bundleDescription);
    }

    /**
     * Returns the id of the bundle. If the bundle is in a named module it the name of the module concatenated with
     * {@code "." + bundleType.getSimpleName()}. If this bundle is not in a named module it is just
     * {bundleType.getSimpleName()}
     * 
     * @return the id of the bundle
     */
    public final String bundleId() {
        // Think we are going to drop this....
        if (bundleModule().isNamed()) {
            return bundleModule().getName() + "." + bundleType.getSimpleName();
        }
        return bundleType.getSimpleName();
    }

    /**
     * Returns the module that this bundle is a member of. This is always the module in which the bundle type is located in.
     * <p>
     * If the bundle is in an unnamed module then the {@linkplain ClassLoader#getUnnamedModule() unnamed} {@code Module} of
     * the class loader for the bundle implementation is returned.
     *
     * @return the module that the bundle is a member of
     * @see Class#getModule()
     */
    public final Module bundleModule() {
        return bundleType.getModule();
    }

    /**
     * Returns the type of the bundle.
     *
     * @return the type of the bundle
     */
    public final Class<? extends Bundle> bundleType() {
        return bundleType;
    }

    /**
     * Returns the version of this bundle. The version of a bundle is always identical to the version of the module to which
     * the bundle belongs. If the bundle is in the unnamed module this method returns {@link Optional#empty()}.
     * 
     * @return the version of the bundle
     * @see ModuleDescriptor#version()
     */
    public final Optional<Version> bundleVersion() {
        // Do we want to allow, people to set their own version in the builder????
        // I mean it won't have any effect... maybe let people override it
        return bundleModule().getDescriptor().version();
    }

    public final Hooks hooks() {
        return new Hooks();
    }

    /** Prints this descriptor to {@code system.out}. */
    public final void print() {
        System.out.println(toString());
    }

    /**
     * Returns the runtime type of the bundle. Is currently one of {@link Container} or {@link Injector}.
     * 
     * @return the runtime type of the bundle
     */
    public final Class<?> runtimeType() {
        return Bundle.class.isAssignableFrom(bundleType) ? Container.class : Injector.class;
    }

    /**
     * Return a {@link Services} object representing the services the bundle exposes. As well as any required or optional
     * services.
     * 
     * @return a services object
     */
    public final Services services() {
        return services;
    }

    // Er detn bare tom for en injector bundle???? Det er den vel
    public final LifecyclePoints startingPoints() {
        return startingPoints;
    }

    public final LifecyclePoints stoppingPoints() {
        return stoppingPoints;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("bundle { type: ").append(format(bundleType));
        if (bundleModule().isNamed()) {
            sb.append(", module: ").append(bundleModule().getName());
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
        return InternalBundleDescriptor.of(bundle).build();
    }

    /**
     * Returns a descriptor for the specified type of bundle.
     * 
     * @param bundleType
     *            the type of bundle to return a descriptor from
     * @return a descriptor for the specified type of bundle
     */
    public static BundleDescriptor of(Class<? extends Bundle> bundleType) {
        return of(Bundles.instantiate(bundleType));
    }

    /**
     *
     * @apiNote A {@code Builder} checks the components and invariants as components are added to the builder. The rationale
     *          for this is to detect errors as early as possible and not defer all validation to the {@link #build build}
     *          method.
     */
    public static class Builder {

        /** An optional description of the bundle. */
        @Nullable
        private String bundleDescription;

        /** The bundleType */
        private final Class<? extends Bundle> bundleType;

        public final HashMap<Key<?>, ServiceDescriptor> serviceExports = new HashMap<>();

        public final HashSet<Key<?>> servicesOptional = new HashSet<>();

        public final HashSet<Key<?>> serviceRequired = new HashSet<>();

        public Builder addServiceExport(ServiceConfiguration<?> configuration) {
            requireNonNull(configuration, "configuration is null");
            return addServiceExport(ServiceDescriptor.ofCopy(configuration));
        }

        public Builder addServiceExport(ServiceDescriptor descriptor) {
            requireNonNull(descriptor, "descriptor is null");
            if (serviceExports.putIfAbsent(descriptor.getKey(), descriptor) != null) {
                throw new IllegalStateException("A service descriptor with the same key has already been added, key = " + descriptor.getKey());
            }
            return this;
        }

        public Builder addServiceRequirementsOptionally(Collection<Key<?>> keys) {
            requireNonNull(keys, "keys is null");
            servicesOptional.addAll(keys);
            return this;
        }

        public Builder addServiceRequirement(Key<?> key) {
            requireNonNull(key, "key is null");
            serviceRequired.add(key);
            return this;
        }

        public Builder addServiceRequirements(Collection<Key<?>> keys) {
            requireNonNull(keys, "keys is null");
            serviceRequired.addAll(keys);
            return this;
        }

        public Builder(Class<? extends Bundle> bundleType) {
            this.bundleType = requireNonNull(bundleType, "bundleType is null");
        }

        public BundleDescriptor build() {
            throw new UnsupportedOperationException();
            // return new BundleDescriptor(this);
        }

        /**
         * @return the bundleType
         */
        public final Class<? extends Bundle> bundleType() {
            return bundleType;
        }

        @Nullable
        public final String getBundleDescription() {
            return bundleDescription;
        }

        public Builder setBundleDescription(@Nullable String description) {
            this.bundleDescription = description;
            return this;
        }
    }

    public static final class Hooks {

        // Permissions-> For AOP, For Invocation, for da shizzla

        public Set<Class<? extends Annotation>> annotatedFields() {
            return Set.of();
        }

        public Set<Class<? extends Annotation>> annotatedMethods() {
            return Set.of();
        }

        public Set<Class<? extends Annotation>> annotatedTypes() {
            return Set.of();
        }
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
}
//
/// **
// * Returns any annotations that are present on the bundle. For example, {@link Deprecated}
// *
// * @return any annotations that are present on the bundle
// */
//// Nah lad os ditche dest
// public AnnotatedElement annotations() {
// return bundleType;
// }
