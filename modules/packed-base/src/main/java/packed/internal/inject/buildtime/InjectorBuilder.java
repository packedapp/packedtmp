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
package packed.internal.inject.buildtime;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
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
import packed.internal.config.site.InternalConfigSite;
import packed.internal.container.DefaultComponentConfiguration;
import packed.internal.container.FactoryComponentConfiguration;
import packed.internal.container.InstantiatedComponentConfiguration;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.ServiceNode;
import packed.internal.invoke.FunctionHandle;

/** This class records all service related information for a single box. */
public final class InjectorBuilder {

    /** A that is used to store parent nodes */
    private static FeatureKey<BSNDefault<?>> FK = new FeatureKey<>() {};

    /** The configuration of the container to which this builder belongs to. */
    final PackedContainerConfiguration container;

    /**
     * Explicit requirements, typically added via {@link InjectionExtension#require(Key)} or
     * {@link InjectionExtension#requireOptionally(Key)}.
     */
    final ArrayList<ExplicitRequirement> explicitRequirements = new ArrayList<>();

    /**
     * All nodes that have been exported, typically via {@link InjectionExtension#export(Class)},
     * {@link InjectionExtension#export(Key)} or {@link InjectionExtension#export(ProvidedComponentConfiguration)}.
     */
    final ArrayList<BSNExported<?>> exportedNodes = new ArrayList<>();

    /**
     * Whether or not the user must explicitly specify all required services. Via {@link InjectionExtension#require(Key)},
     * {@link InjectionExtension#requireOptionally(Key)} and add contract.
     * <p>
     * In previous versions we kept this information on a per node basis. However, it does not work properly with "foreign"
     * hook methods that make use of injection. Because they may not be processed until the bitter end, so it was only
     * really services registered via the provide methods that could make use of them.
     */
    boolean manualRequirementsManagement;

    /** All provided nodes. */
    final ArrayList<BSN<?>> nodes = new ArrayList<>();

    final InjectorResolver resolver = new InjectorResolver(this);

    public InjectorBuilder(PackedContainerConfiguration container) {
        this.container = requireNonNull(container);
    }

    public void build(ArtifactBuildContext buildContext) {
        resolver.build(buildContext);
    }

    public void buildBundle(BundleDescriptor.Builder builder) {
        // need to have resolved successfully
        for (ServiceNode<?> n : resolver.nodes) {
            if (n instanceof BSN) {
                builder.addServiceDescriptor(((BSN<?>) n).toDescriptor());
            }
        }

        for (BSN<?> n : exportedNodes) {
            if (n instanceof BSNExported) {
                builder.contract().services().addProvides(n.getKey());
            }
        }
        resolver.buildContract(builder.contract().services());
    }

    public <T> ServiceConfiguration<T> export(InternalConfigSite cs, Key<T> key) {
        BSNExported<T> node = new BSNExported<>(this, cs, key);
        exportedNodes.add(node);
        return node.toServiceConfiguration();
    }

    public <T> ServiceConfiguration<T> export(InternalConfigSite cs, ProvidedComponentConfiguration<T> configuration) {
        BSNExported<T> node = new BSNExported<>(this, cs, (PackedProvidedComponentConfiguration<T>) configuration);
        exportedNodes.add(node);
        return node.toServiceConfiguration();
    }

    public void importAll(Injector injector, WireletList wirelets) {
        new ImportedInjector(container, this, injector, wirelets).importAll();
    }

    /** Enables manual requirements management. */
    public void manualRequirementsManagement() {
        manualRequirementsManagement = true;
    }

    public void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
        context.put(container, resolver.publicInjector); // Used by PackedContainer
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onProvidedMembers(ComponentConfiguration cc, AtProvidesGroup apg) {
        // This is a bit complicated, to define
        BSNDefault parentNode;
        if (cc instanceof InstantiatedComponentConfiguration) {
            Object instance = ((InstantiatedComponentConfiguration) cc).getInstance();
            parentNode = new BSNDefault(this, (InternalConfigSite) cc.configSite(), instance);
        } else {
            Factory<?> factory = ((FactoryComponentConfiguration) cc).getFactory();
            parentNode = new BSNDefault<>(this, cc, InstantiationMode.SINGLETON, container.lookup.readable(factory.function()), (List) factory.dependencies());
        }

        // If any of the @Provide methods are instance members the parent node needs special treatment.
        // As it needs to be constructed, before the field or method can provide services.
        parentNode.hasInstanceMembers = apg.hasInstanceMembers;

        // Add each @Provide as children of the parent node
        for (AtProvides provides : apg.members.values()) {
            nodes.add(parentNode.provide(provides));
        }

        // Set the parent node, so it can be found from provideFactory or provideInstance
        cc.features().set(InjectorBuilder.FK, parentNode);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ProvidedComponentConfiguration<T> provideFactory(ComponentConfiguration cc, Factory<T> factory, FunctionHandle<T> function) {
        BSNDefault<?> sc = cc.features().get(FK);
        if (sc == null) {
            sc = new BSNDefault<>(this, cc, InstantiationMode.SINGLETON, container.lookup.readable(function), (List) factory.dependencies());
        }
        sc.as((Key) factory.key());
        nodes.add(sc);
        return new PackedProvidedComponentConfiguration<>((DefaultComponentConfiguration) cc, (BSNDefault) sc);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ProvidedComponentConfiguration<T> provideInstance(ComponentConfiguration cc, T instance) {
        // First see if we have already installed the node. This happens in #set if the component container any members
        // annotated with @Provides
        BSNDefault<?> node = cc.features().get(InjectorBuilder.FK);

        if (node == null) {
            // No node found, components has no @Provides method, create a new node
            node = new BSNDefault<T>(this, (InternalConfigSite) cc.configSite(), instance);
        }

        node.as((Key) Key.of(instance.getClass()));
        nodes.add(node);
        return new PackedProvidedComponentConfiguration<>((DefaultComponentConfiguration) cc, (BSNDefault) node);
    }

    public void requireExplicit(Key<?> key, boolean isOptional) {
        explicitRequirements.add(new ExplicitRequirement(key, isOptional));
    }
}
