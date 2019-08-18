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
import java.util.HashSet;
import java.util.List;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.container.BundleDescriptor.Builder;
import app.packed.container.WireletList;
import app.packed.feature.FeatureKey;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorContract;
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

    static FeatureKey<BSNDefault<?>> FK = new FeatureKey<>() {};

    public boolean autoRequires = true;

    /** The configuration of the container that is being build. */
    final PackedContainerConfiguration container;

    /** All explicitly exported nodes in order of registration. */
    final ArrayList<BSNExported<?>> exportedNodes = new ArrayList<>();

    ArrayList<BSN<?>> nodes = new ArrayList<>();

    /** A set of all explicitly registered required service keys. */
    final HashSet<Key<?>> required = new HashSet<>();

    /** A set of all explicitly registered optional service keys. */
    final HashSet<Key<?>> requiredOptionally = new HashSet<>();

    final InjectorResolver resolver = new InjectorResolver(this);

    public InjectorBuilder(PackedContainerConfiguration container) {
        this.container = requireNonNull(container);
    }

    /**
     * Adds the specified key to the list of optional services.
     * 
     * @param key
     *            the key to add
     */
    public void addOptional(Key<?> key) {
        // Hvis vi gemmer en required, saa burde vi faktisk have et multiset???
        // Maaske gem den i liste, og saa alt efter om vi skal vide requires eller
        // bare noegler
        // ExplicityRequiredKeys(Key, isOptional, ConfigSite)
        requireNonNull(key, "key is null");
        requiredOptionally.add(key);
    }

    /**
     * Adds the specified key to the list of required services.
     * 
     * @param key
     *            the key to add
     */
    public void addRequired(Key<?> key) {
        requireNonNull(key, "key is null");
        required.add(key);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void build(ArtifactBuildContext buildContext) {
        for (BSN<?> e : nodes) {
            if (!resolver.nodes.putIfAbsent(e)) {
                System.err.println("OOPS " + e.getKey());
            }
        }
        for (BSNExported<?> e : exportedNodes) {
            ServiceNode<?> sn = resolver.nodes.getRecursive(e.getKey());
            if (sn == null) {
                throw new IllegalStateException("Could not find node to export " + e.getKey());
            }
            e.exportOf = (ServiceNode) sn;
            resolver.exports.put(e);
        }
        DependencyGraph dg = new DependencyGraph(container, this);

        if (buildContext.isInstantiating()) {
            dg.instantiate();
        } else {
            dg.analyze();
        }
    }

    public void buildBundle(Builder descriptor) {
        for (ServiceNode<?> n : resolver.nodes) {
            if (n instanceof BSN) {
                descriptor.addServiceDescriptor(((BSN<?>) n).toDescriptor());
            }
        }

        for (BSN<?> n : exportedNodes) {
            if (n instanceof BSNExported) {
                descriptor.contract().services().addProvides(n.getKey());
            }
        }

        buildContract(descriptor.contract().services());
    }

    public void buildContract(InjectorContract.Builder builder) {
        // Why do we need that list
        // for (ServiceBuildNode<?> n : exportedNodes) {
        // if (n instanceof ServiceBuildNodeExposed) {
        // builder.addProvides(n.getKey());
        // }
        // }
        if (requiredOptionally != null) {
            requiredOptionally.forEach(k -> {
                // We remove all optional dependencies that are also mandatory.
                if (required == null || !required.contains(k)) {
                    builder.addOptional(k);
                }
            });
        }
        if (required != null) {
            required.forEach(k -> builder.addRequires(k));
        }
    }

    public <T> ServiceConfiguration<T> exportKey(Key<T> key, InternalConfigSite cs) {
        BSNExported<T> bn = new BSNExported<>(this, cs, key);
        exportedNodes.add(bn);
        return bn.expose();
    }

    public void importAll(Injector injector, WireletList wirelets) {
        new ImportedInjector(container, this, injector, wirelets).importAll();
    }

    public void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
        context.put(container, resolver.publicInjector); // Taken by PackedContainer
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
        // First see if we have installed a node via @Provides annotations.
        BSNDefault<?> sc = cc.features().get(InjectorBuilder.FK);
        if (sc == null) {
            sc = new BSNDefault<T>(this, (InternalConfigSite) cc.configSite(), instance);
        }

        sc.as((Key) Key.of(instance.getClass()));
        nodes.add(sc);
        return new PackedProvidedComponentConfiguration<>((DefaultComponentConfiguration) cc, (BSNDefault) sc);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void set(ComponentConfiguration cc, AtProvidesGroup apg) {
        BSNDefault sc;
        if (cc instanceof InstantiatedComponentConfiguration) {
            Object instance = ((InstantiatedComponentConfiguration) cc).getInstance();
            sc = new BSNDefault(this, (InternalConfigSite) cc.configSite(), instance);
        } else {
            Factory<?> factory = ((FactoryComponentConfiguration) cc).getFactory();
            sc = new BSNDefault<>(this, cc, InstantiationMode.SINGLETON, container.lookup.readable(factory.function()), (List) factory.dependencies());
        }

        sc.hasInstanceMembers = apg.hasInstanceMembers;
        // AtProvidesGroup has already validated that the specified type does not have any members that provide services with
        // the same key, so we can just add them now without any verification
        for (AtProvides member : apg.members.values()) {
            nodes.add(sc.provide(member));// put them directly
        }
        cc.features().set(InjectorBuilder.FK, sc);
    }
}
