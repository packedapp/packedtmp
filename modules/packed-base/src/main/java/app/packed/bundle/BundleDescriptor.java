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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.packed.app.Main;
import app.packed.container.Container;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.Hook;
import app.packed.inject.Injector;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.buildtime.InternalBundleDescriptor;

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
// includes implementation details....
// name, id, type, stuff I think this is in the descriptor???

// Hvis vi extender contracten her... Boer vi vel ogsaa kunne overskrive denne... Hvad vi jo kan...
// Problemet er at Contract er en abstract klasse....
// Maaske en AbstractContract.....
// Vi vil gerne kunne lave descriptors... med hjemmelavet stuff..... Har jeg lidt svaert ved at se hvordan kan fungere
//
// AbstractBundleDescriptor + BundleDescriptor...

// Does not include dependency graf I think...
// Maybe have a BundleDescriptor.Options object and the BundleDescriptor.of(Bundle b, Options... options);
// BundleDescriptor.graph() throws UOE if Options.IncludeDependencyGraph has not been set.

// Could be made into a Visitor instead..... Or in addition to...
public class BundleDescriptor {

    // I think add @Description as annotation??? IDK

    /** The (optional) description of the bundle. */
    private final @Nullable String bundleDescription;

    /** The type of the bundle. */
    private final Class<? extends Bundle> bundleType;

    /** A Services object. */
    private final BundleContract contract;

    @Nullable
    private String mainEntryPoint;// <--- CanonicalName#MethodName(without args)

    /** A descriptor for each registered service in the bundle. */
    // hmm exported vs
    private final Collection<ServiceDescriptor> services;

    /**
     * Creates a new descriptor from the specified builder.
     * 
     * @param builder
     *            a builder object
     */
    protected BundleDescriptor(BundleDescriptor.Builder builder) {
        requireNonNull(builder, "builder is null");
        this.contract = builder.contract().build();
        this.bundleType = builder.bundleType();
        this.bundleDescription = builder.getBundleDescription();
        this.services = builder.services == null ? List.of() : List.copyOf(builder.services.values());
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

    public List<BundleContract> children() {
        throw new UnsupportedOperationException();
    }

    public BundleContract contract() {
        return contract;
    }

    public final Hooks hooks() {
        return new Hooks();
    }

    /**
     * Returns (optional) the main entry point. A bundle defines at maximum one such entry point using the {@link Main}
     * annotation.
     * 
     * @return any main entry point that the bundle defines
     */
    // Maybe just main()?
    public Optional<String> mainEntryPoint() {
        return Optional.ofNullable(mainEntryPoint);
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

    public Collection<ServiceDescriptor> services() {
        return services;
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

    // /**
    // * Returns a descriptor for the specified type of bundle.
    // *
    // * @param bundleType
    // * the type of bundle to return a descriptor from
    // * @return a descriptor for the specified type of bundle
    // */
    // @Deprecated
    // public static BundleDescriptor of(Class<? extends Bundle> bundleType) {
    // return of(Bundles.instantiate(bundleType));
    // }

    // /**
    // * <p>A stream builder has a lifecycle, which starts in a building
    // * phase, during which elements can be added, and then transitions to a built
    // * phase, after which elements may not be added. The built phase begins
    // * when the {@link #build()} method is called, which creates an ordered
    // * {@code Stream} whose elements are the elements that were added to the stream
    // * builder, in the order they were added.
    //
    // /**
    // * Builds the stream, transitioning this builder to the built state.
    // * An {@code IllegalStateException} is thrown if there are further attempts
    // * to operate on the builder after it has entered the built state.
    // *
    // * @return the built stream
    // * @throws IllegalStateException if the builder has already transitioned to
    // * the built state
    // */
    // Stream<T> build();
    //
    // }

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

        private BundleContract.Builder contract = new BundleContract.Builder();

        private Map<Key<?>, ServiceDescriptor> services;

        public Builder(Class<? extends Bundle> bundleType) {
            this.bundleType = requireNonNull(bundleType, "bundleType is null");
        }

        public Builder addServiceDescriptor(ServiceDescriptor descriptor) {
            requireNonNull(descriptor, "descriptor is null");
            Map<Key<?>, ServiceDescriptor> s = services;
            if (s == null) {
                s = services = new HashMap<>();
            }
            s.put(descriptor.key(), descriptor); // Do we want a defensive copy???
            return this;
        }

        public BundleDescriptor build() {
            return new BundleDescriptor(this);
        }

        /**
         * @return the bundleType
         */
        public final Class<? extends Bundle> bundleType() {
            return bundleType;
        }

        public BundleContract.Builder contract() {
            return contract;
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

    public final class Hooks {

        // Permissions-> For AOP, For Invocation, for da shizzla

        public Map<Class<? extends Class<?>>, Collection<AnnotatedFieldHook<?>>> annotatedFieldExports() {
            return Map.of();
        }

        /**
         * Returns a collection of all exported annotated field hooks of the particular type.
         * 
         * @param <T>
         *            the type of field annotation
         * @param annotationType
         *            the type of field hook
         * @return a collection of all exported annotated field hooks of the particular type
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <T extends Annotation> Collection<AnnotatedFieldHook<T>> annotatedFieldExports(Class<T> annotationType) {
            requireNonNull(annotationType, "annotationType is null");
            return (Collection) annotatedFieldExports().getOrDefault(annotationType, List.of());
        }

        /**
         * Returns a collection of all hooks that the bundle exports in no particular order.
         * 
         * @return a collection of all hooks that the bundle exports in no particular order
         */
        public Collection<Hook> exports() {
            return Set.of();
        }

        public final class Builder {}
        // captures
        // exposes

        // expose hooks, capture hooks

        // Another key feature is hooks.
    }
    //

    // Det gode ved at have en SPEC_VERSION, er at man kan specificere man vil bruge.
    // Og dermed kun importere praecis de interfaces den definere...
    // Deploy(someSpec?) ved ikke lige med API'en /
    // FooBarBundle.API$2_2
    // FooBarBundle.API$2_3-SNAPSHOT hmmm, saa forsvinder den jo naar man releaser den???
    // Maaske hellere have den markeret med @Preview :D
    /// Bundlen, kan maaske endda supportere flere versioner??Som i flere versioner??

    // The union of exposedServices, optionalService and requiredService must be empty
    // Hmm, vi gider ikke bygge dobbelt check..., og vi gider ikke lave en descriptor hver gang.
    // Saa koden skal nok ligge andet steds..
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
