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
package packed.internal.inject.build.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.feature.FeatureKey;
import app.packed.inject.Factory;
import app.packed.inject.InjectionExtension;
import app.packed.inject.Injector;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ComponentServiceConfiguration;
import app.packed.util.Key;
import packed.internal.container.CoreComponentConfiguration;
import packed.internal.container.FactoryComponentConfiguration;
import packed.internal.container.InstantiatedComponentConfiguration;
import packed.internal.inject.InjectConfigSiteOperations;
import packed.internal.inject.build.BuildEntry;
import packed.internal.inject.build.ErrorMessages;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.factoryhandle.FactoryHandle;
import packed.internal.inject.run.AbstractInjector;

/**
 *
 */
public final class ServiceProvidingManager {

    /** A that is used to store parent nodes */
    private static FeatureKey<ComponentBuildEntry<?>> FK = new FeatureKey<>() {};

    /** The injector builder. */
    private final InjectorBuilder builder;

    private LinkedHashMap<Key<?>, LinkedHashSet<BuildEntry<?>>> duplicateNodes;

    /** All provided nodes. */
    private final ArrayList<BuildEntry<?>> entries = new ArrayList<>();

    /** All injectors added via {@link InjectionExtension#provideAll(Injector, Wirelet...)} */
    private ArrayList<ProvideAllFromInjector> provideAll;

    /**
     * @param builder
     */
    public ServiceProvidingManager(InjectorBuilder builder) {
        this.builder = requireNonNull(builder);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onProvidesGroup(ComponentConfiguration cc, AtProvidesGroup apg) {
        ComponentBuildEntry parentNode;
        if (cc instanceof InstantiatedComponentConfiguration) {
            Object instance = ((InstantiatedComponentConfiguration) cc).instance;
            parentNode = new ComponentBuildEntry(builder, cc.configSite(), instance);
        } else {
            Factory<?> factory = ((FactoryComponentConfiguration) cc).factory;
            MethodHandle mh = builder.pcc.lookup.toMethodHandle(factory.handle());
            parentNode = new ComponentBuildEntry<>(builder, cc, InstantiationMode.SINGLETON, mh, (List) factory.dependencies());
        }

        // If any of the @Provide methods are instance members the parent node needs special treatment.
        // As it needs to be constructed, before the field or method can provide services.
        parentNode.hasInstanceMembers = apg.hasInstanceMembers;

        // Add each @Provide as children of the parent node
        for (AtProvides atProvides : apg.members) {
            ConfigSite configSite = parentNode.configSite().thenAnnotatedMember(InjectConfigSiteOperations.INJECTOR_PROVIDE, atProvides.provides,
                    atProvides.member);
            ComponentBuildEntry<?> node = new ComponentBuildEntry<>(configSite, atProvides, atProvides.methodHandle, parentNode);
            node.as((Key) atProvides.key);
            entries.add(node);
        }

        // Set the parent node, so it can be found from provideFactory or provideInstance
        cc.features().set(FK, parentNode);
    }

    public void provideAll(AbstractInjector injector, ConfigSite configSite, WireletList wirelets) {
        if (provideAll == null) {
            provideAll = new ArrayList<>(1);
        }
        provideAll.add(new ProvideAllFromInjector(builder, configSite, injector, wirelets));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ComponentServiceConfiguration<T> provideFactory(ComponentConfiguration cc, Factory<T> factory, FactoryHandle<T> function) {
        ComponentBuildEntry<?> c = cc.features().get(FK);
        if (c == null) {
            MethodHandle mh = builder.pcc.lookup.toMethodHandle(function);
            c = new ComponentBuildEntry<>(builder, cc, InstantiationMode.SINGLETON, mh, (List) factory.dependencies());
        }
        c.as((Key) factory.key());
        entries.add(c);
        return new PackedProvidedComponentConfiguration<>(cc, (ComponentBuildEntry) c);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ComponentServiceConfiguration<T> provideInstance(ComponentConfiguration cc, T instance) {
        // First see if we have already installed the node. This happens in #set if the component container any members
        // annotated with @Provides
        ComponentBuildEntry<?> node = cc.features().get(FK);

        if (node == null) {
            // No node found, components has no @Provides method, create a new node
            node = new ComponentBuildEntry<T>(builder, cc.configSite(), instance);
        }

        node.as((Key) Key.of(instance.getClass()));
        entries.add(node);
        return new PackedProvidedComponentConfiguration<>((CoreComponentConfiguration) cc, (ComponentBuildEntry) node);
    }

    public HashMap<Key<?>, BuildEntry<?>> resolveAndCheckForDublicates(ArtifactBuildContext buildContext) {
        HashMap<Key<?>, BuildEntry<?>> resolvedServices = new HashMap<>();

        // First process provided entries, then any entries added via provideAll
        resolveAndCheckForDublicates0(resolvedServices, entries);
        if (provideAll != null) {
            // All injectors have already had wirelets transform and filter
            for (ProvideAllFromInjector fromInjector : provideAll) {
                resolveAndCheckForDublicates0(resolvedServices, fromInjector.entries.values());
            }
        }

        // Run through all linked containers...
        // Apply any wirelets to exports, and take

        // Add error messages if any nodes with the same key have been added multiple times
        if (duplicateNodes != null) {
            ErrorMessages.addDuplicateNodes(builder.context().buildContext(), duplicateNodes);
        }
        return resolvedServices;
    }

    private void resolveAndCheckForDublicates0(HashMap<Key<?>, BuildEntry<?>> resolvedServices, Iterable<? extends BuildEntry<?>> entries) {
        for (BuildEntry<?> entry : entries) {
            Key<?> key = entry.key(); // whats the deal with null keys
            if (key != null) {
                BuildEntry<?> existing = resolvedServices.putIfAbsent(key, entry);
                if (existing != null) {
                    if (duplicateNodes == null) {
                        duplicateNodes = new LinkedHashMap<>();
                    }
                    LinkedHashSet<BuildEntry<?>> hs = duplicateNodes.computeIfAbsent(key, m -> new LinkedHashSet<>());
                    hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                    hs.add(entry);
                }
            }
        }
    }
}
