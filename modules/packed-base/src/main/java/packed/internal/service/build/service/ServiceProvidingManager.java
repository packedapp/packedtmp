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
package packed.internal.service.build.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.Wirelet;
import app.packed.service.ComponentServiceConfiguration;
import app.packed.service.Factory;
import app.packed.service.Injector;
import app.packed.service.InstantiationMode;
import app.packed.service.Provide;
import app.packed.service.ServiceExtension;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.container.CoreComponentConfiguration;
import packed.internal.container.FactoryComponentConfiguration;
import packed.internal.container.InstantiatedComponentConfiguration;
import packed.internal.container.WireletList;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.service.InjectConfigSiteOperations;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.ErrorMessages;
import packed.internal.service.build.ServiceExtensionNode;
import packed.internal.service.factoryhandle.FactoryHandle;
import packed.internal.service.run.AbstractInjector;

/**
 * This class manages everything to do with providing services for an {@link ServiceExtension}.
 *
 * @see ServiceExtension#provide(Class)
 * @see ServiceExtension#provide(Factory)
 * @see ServiceExtension#provideAll(Injector, Wirelet...)
 * @see ServiceExtension#provideInstance(Object)
 * @see Provide
 */
public final class ServiceProvidingManager {

    /** A map used to cache build entries, connect stuff */
    private final IdentityHashMap<ComponentConfiguration<?>, ComponentBuildEntry<?>> componentConfigurationCache = new IdentityHashMap<>();

    /** A map of build entries that provide services with the same key. */
    @Nullable
    private LinkedHashMap<Key<?>, LinkedHashSet<BuildEntry<?>>> failingDuplicateProviders;

    /** The extension node. */
    private final ServiceExtensionNode node;

    /** All injectors added via {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
    private ArrayList<ProvideAllFromInjector> provideAll;

    /** All explicit added build entries. */
    private final ArrayList<BuildEntry<?>> providingEntries = new ArrayList<>();

    /**
     * Creates a new manager.
     * 
     * @param node
     *            the extension node
     */
    public ServiceProvidingManager(ServiceExtensionNode node) {
        this.node = requireNonNull(node);
    }

    /**
     * Invoked by the runtime when a component has members (fields or methods) that are annotated with {@link Provide}.
     * 
     * @param cc
     *            the configuration of the annotated component
     * @param group
     *            a provides group object
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addProvidesGroup(ComponentConfiguration cc, AtProvidesGroup group) {
        // The parent node is not added until #provideFactory or #provideInstance
        ComponentBuildEntry parentNode;
        if (cc instanceof InstantiatedComponentConfiguration) {
            Object instance = ((InstantiatedComponentConfiguration) cc).instance;
            parentNode = new ComponentBuildEntry(node, cc.configSite(), instance);
        } else {
            Factory<?> factory = ((FactoryComponentConfiguration) cc).factory;

            MethodHandle mh = ((PackedExtensionContext) node.context()).pcc.lookup.toMethodHandle(factory.handle());
            parentNode = new ComponentBuildEntry<>(node, cc, InstantiationMode.SINGLETON, mh, (List) factory.dependencies());
        }

        // If any of the @Provide methods are instance members the parent node needs special treatment.
        // As it needs to be constructed, before the field or method can provide services.
        parentNode.hasInstanceMembers = group.hasInstanceMembers;

        // Add each @Provide as children of the parent node
        for (AtProvides atProvides : group.members) {
            ConfigSite configSite = parentNode.configSite().thenAnnotatedMember(InjectConfigSiteOperations.INJECTOR_PROVIDE, atProvides.provides,
                    atProvides.member);
            ComponentBuildEntry<?> node = new ComponentBuildEntry<>(configSite, atProvides, atProvides.methodHandle, parentNode);
            node.as((Key) atProvides.key);
            providingEntries.add(node);
        }

        // Set the parent node, so it can be found from provideFactory or provideInstance
        componentConfigurationCache.put(cc, parentNode);
    }

    public void provideAll(AbstractInjector injector, ConfigSite configSite, WireletList wirelets) {
        ArrayList<ProvideAllFromInjector> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(new ProvideAllFromInjector(node, configSite, injector, wirelets));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ComponentServiceConfiguration<T> provideFactory(ComponentConfiguration cc, Factory<T> factory, FactoryHandle<T> function) {
        ComponentBuildEntry<?> c = componentConfigurationCache.get(cc);// remove??
        if (c == null) {
            MethodHandle mh = ((PackedExtensionContext) node.context()).pcc.lookup.toMethodHandle(function);
            c = new ComponentBuildEntry<>(node, cc, InstantiationMode.SINGLETON, mh, (List) factory.dependencies());
        }
        c.as((Key) factory.key());
        providingEntries.add(c);
        return new PackedProvidedComponentConfiguration<>(cc, (ComponentBuildEntry) c);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ComponentServiceConfiguration<T> provideInstance(ComponentConfiguration cc, T instance) {
        // First see if we have already installed the node. This happens in #set if the component container any members
        // annotated with @Provides
        ComponentBuildEntry<?> c = componentConfigurationCache.get(cc);
        if (c == null) {
            // No node found, components has no @Provides method, create a new node
            c = new ComponentBuildEntry<T>(node, cc.configSite(), instance);
        }

        c.as((Key) Key.of(instance.getClass()));
        providingEntries.add(c);
        return new PackedProvidedComponentConfiguration<>((CoreComponentConfiguration) cc, (ComponentBuildEntry) c);
    }

    public HashMap<Key<?>, BuildEntry<?>> resolve() {
        LinkedHashMap<Key<?>, BuildEntry<?>> resolvedServices = new LinkedHashMap<>();

        // First process provided entries, then any entries added via provideAll
        resolve0(resolvedServices, providingEntries);
        if (provideAll != null) {
            // All injectors have already had wirelets transform and filter
            for (ProvideAllFromInjector fromInjector : provideAll) {
                resolve0(resolvedServices, fromInjector.entries.values());
            }
        }

        // Run through all linked containers...
        // Apply any wirelets to exports, and take

        // Add error messages if any nodes with the same key have been added multiple times
        if (failingDuplicateProviders != null) {
            ErrorMessages.addDuplicateNodes(failingDuplicateProviders);
        }
        return resolvedServices;
    }

    private void resolve0(LinkedHashMap<Key<?>, BuildEntry<?>> resolvedServices, Collection<? extends BuildEntry<?>> entries) {
        for (BuildEntry<?> entry : entries) {
            Key<?> key = entry.key(); // whats the deal with null keys
            if (key != null) {
                BuildEntry<?> existing = resolvedServices.putIfAbsent(key, entry);
                if (existing != null) {
                    if (failingDuplicateProviders == null) {
                        failingDuplicateProviders = new LinkedHashMap<>();
                    }
                    LinkedHashSet<BuildEntry<?>> hs = failingDuplicateProviders.computeIfAbsent(key, m -> new LinkedHashSet<>());
                    hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                    hs.add(entry);
                }
            }
        }
    }
}
