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
package packed.internal.inject.build;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.BundleDescriptor;
import app.packed.container.WireletList;
import app.packed.feature.FeatureKey;
import app.packed.inject.Factory;
import app.packed.inject.InjectionExtension;
import app.packed.inject.Injector;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import packed.internal.container.CoreComponentConfiguration;
import packed.internal.container.FactoryComponentConfiguration;
import packed.internal.container.InstantiatedComponentConfiguration;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.compose.InjectorResolver;
import packed.internal.invoke.FunctionHandle;

/** This class records all service related information for a single box. */
public final class InjectorBuilder {

    /** A that is used to store parent nodes */
    private static FeatureKey<BSEComponent<?>> FK = new FeatureKey<>() {};

    /** The configuration of the container to which this builder belongs to. */
    public final PackedContainerConfiguration containerConfiguration;

    /** All provided nodes. */
    public final ArrayList<BSE<?>> entries = new ArrayList<>();

    /**
     * Explicit requirements, typically added via {@link InjectionExtension#require(Key)} or
     * {@link InjectionExtension#requireOptionally(Key)}.
     */
    final ArrayList<ExplicitRequirement> explicitRequirements = new ArrayList<>();

    /**
     * All nodes that have been exported, typically via {@link InjectionExtension#export(Class)},
     * {@link InjectionExtension#export(Key)} or {@link InjectionExtension#export(ProvidedComponentConfiguration)}.
     */
    public final ArrayList<BSEExported<?>> exportedEntries = new ArrayList<>();

    /**
     * Whether or not the user must explicitly specify all required services. Via {@link InjectionExtension#require(Key)},
     * {@link InjectionExtension#requireOptionally(Key)} and add contract.
     * <p>
     * In previous versions we kept this information on a per node basis. However, it does not work properly with "foreign"
     * hook methods that make use of injection. Because they may not be processed until the bitter end, so it was only
     * really services registered via the provide methods that could make use of them.
     */
    public boolean manualRequirementsManagement;

    final InjectorResolver resolver = new InjectorResolver(this);

    /**
     * Creates a new builder.
     * 
     * @param containerConfiguration
     *            the configuration of the container
     */
    public InjectorBuilder(PackedContainerConfiguration containerConfiguration) {
        this.containerConfiguration = requireNonNull(containerConfiguration);
    }

    public void build(ArtifactBuildContext buildContext) {
        resolver.build(buildContext);
    }

    public void buildBundle(BundleDescriptor.Builder builder) {
        // need to have resolved successfully
        for (ServiceEntry<?> n : resolver.internalNodes) {
            if (n instanceof BSE) {
                builder.addServiceDescriptor(((BSE<?>) n).toDescriptor());
            }
        }

        for (BSE<?> n : exportedEntries) {
            if (n instanceof BSEExported) {
                builder.contract().services().addProvides(n.getKey());
            }
        }
        resolver.buildContract(builder.contract().services());
    }

    public <T> ServiceConfiguration<T> export(Key<T> key, ConfigSite configSite) {
        return export0(new BSEExported<>(this, configSite, key));
    }

    public <T> ServiceConfiguration<T> export(ProvidedComponentConfiguration<T> configuration, ConfigSite configSite) {
        PackedProvidedComponentConfiguration<T> ppcc = (PackedProvidedComponentConfiguration<T>) configuration;
        if (ppcc.buildEntry.injectorBuilder != this) {
            throw new IllegalArgumentException("The specified configuration object was created by another injector extension instance");
        }
        return export0(new BSEExported<>(this, configSite, ppcc.buildEntry));
    }

    /**
     * Converts the internal exported entry to a service configuration object.
     * 
     * @param <T>
     *            the type of service the entry wraps
     * @param entry
     *            the entry to convert
     * @return a service configuration object
     */
    private <T> ServiceConfiguration<T> export0(BSEExported<T> entry) {
        exportedEntries.add(entry);
        return entry.toServiceConfiguration();
    }

    public void importAll(Injector injector, WireletList wirelets) {
        new ImportedInjector(containerConfiguration, this, injector, wirelets).importAll();
    }

    /** Enables manual requirements management. */
    public void manualRequirementsManagement() {
        manualRequirementsManagement = true;
    }

    public void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
        context.put(containerConfiguration, resolver.publicInjector); // Used by PackedContainer
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onProvidedMembers(ComponentConfiguration cc, AtProvidesGroup apg) {
        // This is a bit complicated, to define
        BSEComponent parentNode;
        if (cc instanceof InstantiatedComponentConfiguration) {
            Object instance = ((InstantiatedComponentConfiguration) cc).instance;
            parentNode = new BSEComponent(this, cc.configSite(), instance);
        } else {
            Factory<?> factory = ((FactoryComponentConfiguration) cc).factory;
            parentNode = new BSEComponent<>(this, cc, InstantiationMode.SINGLETON, containerConfiguration.lookup.readable(factory.function()),
                    (List) factory.dependencies());
        }

        // If any of the @Provide methods are instance members the parent node needs special treatment.
        // As it needs to be constructed, before the field or method can provide services.
        parentNode.hasInstanceMembers = apg.hasInstanceMembers;

        // Add each @Provide as children of the parent node
        for (AtProvides provides : apg.members.values()) {
            entries.add(parentNode.provide(provides));
        }

        // Set the parent node, so it can be found from provideFactory or provideInstance
        cc.features().set(InjectorBuilder.FK, parentNode);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ProvidedComponentConfiguration<T> provideFactory(ComponentConfiguration cc, Factory<T> factory, FunctionHandle<T> function) {
        BSEComponent<?> sc = cc.features().get(FK);
        if (sc == null) {
            sc = new BSEComponent<>(this, cc, InstantiationMode.SINGLETON, containerConfiguration.lookup.readable(function), (List) factory.dependencies());
        }
        sc.as((Key) factory.key());
        entries.add(sc);
        return new PackedProvidedComponentConfiguration<>((CoreComponentConfiguration) cc, (BSEComponent) sc);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ProvidedComponentConfiguration<T> provideInstance(ComponentConfiguration cc, T instance) {
        // First see if we have already installed the node. This happens in #set if the component container any members
        // annotated with @Provides
        BSEComponent<?> node = cc.features().get(InjectorBuilder.FK);

        if (node == null) {
            // No node found, components has no @Provides method, create a new node
            node = new BSEComponent<T>(this, cc.configSite(), instance);
        }

        node.as((Key) Key.of(instance.getClass()));
        entries.add(node);
        return new PackedProvidedComponentConfiguration<>((CoreComponentConfiguration) cc, (BSEComponent) node);
    }

    public void requireExplicit(Key<?> key, boolean isOptional) {
        explicitRequirements.add(new ExplicitRequirement(key, isOptional));
    }
}
