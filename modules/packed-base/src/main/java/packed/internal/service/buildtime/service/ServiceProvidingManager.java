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
package packed.internal.service.buildtime.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.inject.Provide;
import app.packed.service.Injector;
import app.packed.service.ServiceExtension;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.wirelet.WireletList;
import packed.internal.inject.ConfigSiteInjectOperations;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ErrorMessages;
import packed.internal.service.buildtime.InjectionManager;
import packed.internal.service.runtime.AbstractInjector;

/**
 * This class manages everything to do with providing services for an {@link ServiceExtension}.
 *
 * @see ServiceExtension#provideAll(Injector, Wirelet...)
 * @see Provide
 */
public final class ServiceProvidingManager {

    /** A map of build entries that provide services with the same key. */
    @Nullable
    private LinkedHashMap<Key<?>, LinkedHashSet<BuildEntry<?>>> failingDuplicateProviders;

    public final IdentityHashMap<BuildEntry<?>, MethodHandle> handlers = new IdentityHashMap<>();

    /** The injection manager. */
    private final InjectionManager im;

    /** All injectors added via {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
    private ArrayList<ProvideAllFromOtherInjector> provideAll;

    /** All explicit added build entries. */
    public final ArrayList<BuildEntry<?>> buildEntries = new ArrayList<>();

    /**
     * Creates a new manager.
     * 
     * @param node
     *            the extension node
     */
    public ServiceProvidingManager(InjectionManager node) {
        this.im = requireNonNull(node);
    }

    /**
     * Invoked by the runtime when a component has members (fields or methods) that are annotated with {@link Provide}.
     * 
     * @param hook
     *            a provides group object
     * @param component
     *            the configuration of the annotated component
     */
    @SuppressWarnings("rawtypes")
    public void addProvidesHook(AtProvidesHook hook, ComponentNodeConfiguration component) {
        // validate new instance member for @Provide

        // Add each @Provide as children of the parent node
        for (AtProvides atProvides : hook.members) {
            ConfigSite configSite = component.source.component.configSite().thenAnnotatedMember(ConfigSiteInjectOperations.INJECTOR_PROVIDE,
                    atProvides.provides, atProvides.member);
            new ProvideBuildEntry(configSite, component, atProvides); // Adds itself to #buildEntries
        }
    }

    public BuildEntry<?> providePrototype(ComponentNodeConfiguration cc) {
        BuildEntry<?> e = cc.source.providePrototype();
        buildEntries.add(e);
        return e;
    }

    public void provideAll(AbstractInjector injector, ConfigSite configSite, WireletList wirelets) {
        ArrayList<ProvideAllFromOtherInjector> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(new ProvideAllFromOtherInjector(im, configSite, injector, wirelets));
    }

    public HashMap<Key<?>, BuildEntry<?>> resolve() {

        LinkedHashMap<Key<?>, BuildEntry<?>> resolvedServices = new LinkedHashMap<>();

        // First process provided entries, then any entries added via provideAll
        resolve0(resolvedServices, buildEntries);

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
        System.out.println("Found " + resolvedServices);
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
