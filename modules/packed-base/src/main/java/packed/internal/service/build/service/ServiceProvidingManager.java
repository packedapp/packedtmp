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
import app.packed.lang.Key;
import app.packed.lang.Nullable;
import app.packed.service.Factory;
import app.packed.service.Injector;
import app.packed.service.InstantiationMode;
import app.packed.service.Provide;
import app.packed.service.ServiceComponentConfiguration;
import app.packed.service.ServiceExtension;
import packed.internal.component.AbstractCoreComponentConfiguration;
import packed.internal.component.FactoryComponentConfiguration;
import packed.internal.component.InstantiatedComponentConfiguration;
import packed.internal.container.FixedWireletList;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.inject.factoryhandle.FactoryHandle;
import packed.internal.inject.util.InjectConfigSiteOperations;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.ErrorMessages;
import packed.internal.service.build.ServiceExtensionNode;
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
    private final IdentityHashMap<ComponentConfiguration<?>, BuildEntry<?>> componentConfigurationCache = new IdentityHashMap<>();

    /** A map of build entries that provide services with the same key. */
    @Nullable
    private LinkedHashMap<Key<?>, LinkedHashSet<BuildEntry<?>>> failingDuplicateProviders;

    /** The extension node. */
    private final ServiceExtensionNode node;

    /** All injectors added via {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
    private ArrayList<ProvideAllFromOtherInjector> provideAll;

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
     * @param hook
     *            a provides group object
     * @param cc
     *            the configuration of the annotated component
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addProvidesHook(AtProvidesHook hook, ComponentConfiguration cc) {
        // The parent node is not added until #provideFactory or #provideInstance
        AbstractComponentBuildEntry parentNode;
        if (cc instanceof InstantiatedComponentConfiguration) {
            Object instance = ((InstantiatedComponentConfiguration) cc).instance;
            parentNode = new ComponentInstanceBuildEntry<>(node, cc.configSite(), cc, instance);
        } else {
            Factory<?> factory = ((FactoryComponentConfiguration) cc).factory;

            MethodHandle mh = ((PackedExtensionContext) node.context()).container().lookup.toMethodHandle(factory.handle());
            parentNode = new ComponentFactoryBuildEntry<>(node, cc, InstantiationMode.SINGLETON, mh, (List) factory.dependencies());
        }

        // If any of the @Provide methods are instance members the parent node needs special treatment.
        // As it needs to be constructed, before the field or method can provide services.
        if (parentNode instanceof ComponentFactoryBuildEntry) {
            ((ComponentFactoryBuildEntry) parentNode).hasInstanceMembers = hook.hasInstanceMembers;
        }

        // Add each @Provide as children of the parent node
        for (AtProvides atProvides : hook.members) {
            ConfigSite configSite = parentNode.configSite().thenAnnotatedMember(InjectConfigSiteOperations.INJECTOR_PROVIDE, atProvides.provides,
                    atProvides.member);
            ComponentFactoryBuildEntry<?> node = new ComponentFactoryBuildEntry<>(configSite, atProvides, atProvides.methodHandle, parentNode);
            node.as((Key) atProvides.key);
            providingEntries.add(node);
        }

        // Set the parent node, so it can be found from provideFactory or provideInstance
        componentConfigurationCache.put(cc, parentNode);
    }

    public void provideAll(AbstractInjector injector, ConfigSite configSite, FixedWireletList wirelets) {
        ArrayList<ProvideAllFromOtherInjector> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(new ProvideAllFromOtherInjector(node, configSite, injector, wirelets));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ServiceComponentConfiguration<T> provideFactory(ComponentConfiguration cc, Factory<T> factory, FactoryHandle<T> function) {
        BuildEntry<?> c = componentConfigurationCache.get(cc);// remove??
        if (c == null) {
            MethodHandle mh = ((PackedExtensionContext) node.context()).container().lookup.toMethodHandle(function);
            c = new ComponentFactoryBuildEntry<>(node, cc, InstantiationMode.SINGLETON, mh, (List) factory.dependencies());
        }
        c.as((Key) factory.key());
        providingEntries.add(c);
        return new PackedServiceComponentConfiguration<>(cc, (ComponentFactoryBuildEntry) c);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ServiceComponentConfiguration<T> provideInstance(ComponentConfiguration cc, T instance) {
        // First see if we have already installed the node. This happens in #set if the component container any members
        // annotated with @Provides
        BuildEntry<?> c = componentConfigurationCache.get(cc);
        if (c == null) {
            // No node found, components has no @Provides method, create a new node
            c = new ComponentInstanceBuildEntry<T>(node, cc.configSite(), cc, instance);
        }

        c.as((Key) Key.of(instance.getClass()));
        providingEntries.add(c);
        return new PackedServiceComponentConfiguration<>((AbstractCoreComponentConfiguration) cc, (BuildEntry) c);
    }

    public HashMap<Key<?>, BuildEntry<?>> resolve() {
        LinkedHashMap<Key<?>, BuildEntry<?>> resolvedServices = new LinkedHashMap<>();

        // First process provided entries, then any entries added via provideAll
        resolve0(resolvedServices, providingEntries);
        if (provideAll != null) {
            // All injectors have already had wirelets transform and filter
            for (ProvideAllFromOtherInjector fromInjector : provideAll) {
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
