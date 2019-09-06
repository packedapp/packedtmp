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
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.inject.ComponentServiceConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.InjectionExtension;
import app.packed.inject.Injector;
import app.packed.inject.InstantiationMode;
import app.packed.util.Key;
import app.packed.util.Nullable;
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
 * Manages all entities that provide a service.
 */
public final class ServiceProvidingManager {

    /** The injector builder. */
    private final InjectorBuilder builder;

    /** A map of build entries that provide services with the same key. */
    @Nullable
    private LinkedHashMap<Key<?>, LinkedHashSet<BuildEntry<?>>> duplicateProviders;

    /** A map used to connect stuff */
    private final IdentityHashMap<ComponentConfiguration, ComponentBuildEntry<?>> en = new IdentityHashMap<>();

    /** All explicit added build entries. */
    private final ArrayList<BuildEntry<?>> entries = new ArrayList<>();

    /** All injectors added via {@link InjectionExtension#provideAll(Injector, Wirelet...)}. */
    private ArrayList<ProvideAllFromInjector> provideAll;

    /**
     * Creates a new manager.
     * 
     * @param builder
     *            the injector builder
     */
    public ServiceProvidingManager(InjectorBuilder builder) {
        this.builder = requireNonNull(builder);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onProvidesGroup(ComponentConfiguration cc, AtProvidesGroup apg) {
        // The parent node is not added until #provideFactory or #provideInstance
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
        en.put(cc, parentNode);
    }

    public void provideAll(AbstractInjector injector, ConfigSite configSite, WireletList wirelets) {
        if (provideAll == null) {
            provideAll = new ArrayList<>(1);
        }
        provideAll.add(new ProvideAllFromInjector(builder, configSite, injector, wirelets));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ComponentServiceConfiguration<T> provideFactory(ComponentConfiguration cc, Factory<T> factory, FactoryHandle<T> function) {
        ComponentBuildEntry<?> c = en.get(cc);
        // ComponentBuildEntry<?> c = cc.features().get(FK);
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
        // ComponentBuildEntry<?> c = cc.features().get(FK);
        ComponentBuildEntry<?> c = en.get(cc);
        if (c == null) {
            // No node found, components has no @Provides method, create a new node
            c = new ComponentBuildEntry<T>(builder, cc.configSite(), instance);
        }

        c.as((Key) Key.of(instance.getClass()));
        entries.add(c);
        return new PackedProvidedComponentConfiguration<>((CoreComponentConfiguration) cc, (ComponentBuildEntry) c);
    }

    public HashMap<Key<?>, BuildEntry<?>> resolveAndCheckForDublicates(ArtifactBuildContext buildContext) {
        LinkedHashMap<Key<?>, BuildEntry<?>> resolvedServices = new LinkedHashMap<>();

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
        if (duplicateProviders != null) {
            ErrorMessages.addDuplicateNodes(builder.context().buildContext(), duplicateProviders);
        }
        return resolvedServices;
    }

    private void resolveAndCheckForDublicates0(LinkedHashMap<Key<?>, BuildEntry<?>> resolvedServices, Collection<? extends BuildEntry<?>> entries) {
        for (BuildEntry<?> entry : entries) {
            Key<?> key = entry.key(); // whats the deal with null keys
            if (key != null) {
                BuildEntry<?> existing = resolvedServices.putIfAbsent(key, entry);
                if (existing != null) {
                    if (duplicateProviders == null) {
                        duplicateProviders = new LinkedHashMap<>();
                    }
                    LinkedHashSet<BuildEntry<?>> hs = duplicateProviders.computeIfAbsent(key, m -> new LinkedHashSet<>());
                    hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                    hs.add(entry);
                }
            }
        }
    }
}
