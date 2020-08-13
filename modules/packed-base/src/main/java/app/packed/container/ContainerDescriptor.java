/*
c
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
package app.packed.container;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Version;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.packed.artifact.ArtifactImage;
import app.packed.base.Contract;
import app.packed.base.ContractSet;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.service.ServiceDescriptor;
import packed.internal.artifact.AssembleOutput;
import packed.internal.container.PackedContainerRole;

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
 * <p>
 * {@code BundleDescriptor} objects are immutable and safe for use by multiple concurrent threads.
 * </p>
 */
// Pretty pringting http://www.lihaoyi.com/post/CompactStreamingPrettyPrintingofHierarchicalData.html
// Abstract Bundle Descriptor

// Description, Tags, runtimeType = {Container/Injector}, BundleFactory.class, Descriptor=InjectorBundleDescriptor
// (maaske faas den fra BundleFactory)
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
// Skal igen bare vaere en wrapper oven paa AnyBundle. Okay her bliver det lidt problematisk med at man kan definere sin
// egen. Boern... altid en hovedpine

// Maaske ender man ikke med at kunne det. Men det er en god ovelse for at separare ting.

// I think add @Description as annotation??? IDK

// ArtifactModel.. Hmm, not sure..
// For example, we do not have a path
// We also take something that is not an artifact
// More Like ContainerDescriptor????
// Because we can also create from a ContainerImage
/// Yes but that image is created from a bundle of some kind.
public class ContainerDescriptor {

    /** The type of the bundle. */
    private final Class<? extends Bundle<?>> bundleType;

    private final ContractSet contracts;

    /** The (optional) description of the bundle. */
    @Nullable
    private final String description;

    private final LinkedHashSet<Class<? extends Extension>> extensions;

    @Nullable
    private String mainEntryPoint;// <--- CanonicalName#MethodName(without args)

    private final String name;

    /**
     * Creates a new descriptor from the specified builder.
     * 
     * @param builder
     *            a builder object
     */
    protected ContainerDescriptor(ContainerDescriptor.Builder builder) {
        requireNonNull(builder, "builder is null");
        this.contracts = ContractSet.of(builder.contracts.values());
        this.bundleType = builder.bundleType();
        this.description = builder.getBundleDescription();
        this.name = builder.name == null ? "?" : builder.name;
        this.extensions = builder.extensions;
    }

    // De er vel named.... Saa Map<String, Descriptor...
    public List<ContainerDescriptor> children() {
        // Saa skal vi vel ogsaa have navne...
        // Maaske kan vi have Container? <- Indicating that it will be created with Container and then some postfix
        throw new UnsupportedOperationException();
    }

    public ContractSet contracts() {
        return contracts;

    }

    // Kan ikke rigtig se hvordan det skulle fungere.... med mindre vi har

    // <T extends AnyBundleDescriptor> List<T> children(Class<T> descriptorType) {
    // Men hvem bestemmer hvilken descriptor type vi laver????
    // Hvis det er en tom skal, der tager en Builder???

    /**
     * Returns any description that has been set for the bundle via {@link ContainerBundle#setDescription(String)}.
     * 
     * @return a optional description of the bundle
     * 
     * @see ContainerBundle#setDescription(String)
     */
    public final Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /**
     * Returns an unmodifiable set of all the extensions the bundle uses.
     * 
     * @return an unmodifiable set of all the extensions the bundle uses
     */
    public final Set<Class<? extends Extension>> extensions() {
        // We return it their order...
        return extensions; // Do we want some kind of order??? Topologically, name sorted? by ID
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
    public final Module module() {
        return bundleType.getModule();
    }

    /**
     * Returns the name of the bundle.
     * 
     * @return the name of the bundle
     */
    public final String name() {
        return name;
    }

    /** Prints this descriptor to {@code system.out}. */
    public final void print() {
        System.out.println(toString());
    }

    /**
     * Returns the type of the bundle.
     *
     * @return the type of the bundle
     */
    public final Class<? extends Bundle<?>> sourceType() {
        return bundleType;
    }

    // has start/stop
    // needsExecutionPhase...
    public boolean isExecutable() {
        return false;
    }

    String toJSON() {
        // Kan maaske have noget funktionality til at lave diffs....
        // Er nok mere vigtig paa contracts...
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("bundle { name: ").append(name);
        sb.append(", type: ").append(format(bundleType));
        if (module().isNamed()) {
            sb.append(", module: ").append(module().getName());
        }

        sb.append(" }");
        return sb.toString();
    }

    /**
     * Returns the version of this bundle. The version of a bundle is always identical to the version of the module to which
     * the bundle belongs. If the bundle is in the unnamed module or on the class path this method returns
     * {@link Optional#empty()}.
     * 
     * @return the version of the bundle, or an empty optional if the bundle does not have a version
     * @see ModuleDescriptor#version()
     */
    public final Optional<Version> version() {
        // To keep things simple we do not currently allow people to override the version.
        // Should we look in Jar, if on the classpath???
        ModuleDescriptor descriptor = module().getDescriptor();
        return descriptor == null ? Optional.empty() : descriptor.version();
    }

    /**
     * Creates a bundle descriptor for the specified bundle.
     *
     * @param bundle
     *            the bundle to create a descriptor for
     * @return a descriptor for the specified bundle
     * 
     * @see ArtifactImage#descriptor()
     */
    public static ContainerDescriptor of(ContainerBundle bundle) {
        requireNonNull(bundle, "bundle is null");
        PackedContainerRole pcc = PackedContainerRole.assemble(AssembleOutput.descriptor(ContainerDescriptor.class), bundle);

        ContainerDescriptor.Builder builder = new ContainerDescriptor.Builder(bundle.getClass());
        pcc.buildDescriptor(builder);
        return builder.build();
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
        private final Class<? extends Bundle<?>> bundleType;

        private IdentityHashMap<Class<? extends Contract>, Contract> contracts = new IdentityHashMap<>();

        public final LinkedHashSet<Class<? extends Extension>> extensions = new LinkedHashSet<>();

        private String name;

        private Map<Key<?>, ServiceDescriptor> services;

        public Builder(Class<? extends Bundle<?>> bundleType) {
            this.bundleType = requireNonNull(bundleType, "bundleType is null");
        }

        public void addContract(Contract contract) {
            requireNonNull(contract, "contract is null");
            if (contracts.putIfAbsent(contract.getClass(), contract) != null) {
                throw new IllegalStateException("A contract of the specified type has already been added, type " + contract.getClass());
            }
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

        public ContainerDescriptor build() {
            return new ContainerDescriptor(this);
        }

        /**
         * @return the bundleType
         */
        public final Class<? extends Bundle<?>> bundleType() {
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

        public Builder setName(String name) {
            this.name = requireNonNull(name);
            return this;
        }
    }
}

//
/// **
// * Returns the runtime type of the bundle. Is currently one of {@link Container} or {@link Injector}.
// *
// * @return the runtime type of the bundle
// */
// public final Class<?> runtimeType() {
// return Bundle.class.isAssignableFrom(bundleType) ? Container.class : Injector.class;
// }
//
/// **
// * Returns the id of the bundle. If the bundle is in a named module it the name of the module concatenated with
// * {@code "." + bundleType.getSimpleName()}. If this bundle is not in a named module it is just
// * {bundleType.getSimpleName()}
// *
// * @return the id of the bundle
// */
// public final String bundleId() {
// // Think we are going to drop this....
// if (bundleModule().isNamed()) {
// return bundleModule().getName() + "." + bundleType.getSimpleName();
// }
// return bundleType.getSimpleName();
// }
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
