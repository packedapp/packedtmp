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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.service.Injector;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceRegistry;
import packed.internal.component.ComponentNode;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.RegionAssembly;
import packed.internal.component.RuntimeRegion;
import packed.internal.component.wirelet.WireletList;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.container.ContainerAssembly;
import packed.internal.inject.dependency.Injectable;
import packed.internal.inject.service.DependencyManager;
import packed.internal.inject.service.ServiceManager;
import packed.internal.inject.service.assembly.AtProvideServiceAssembly;
import packed.internal.inject.service.assembly.ComponentSourceServiceAssembly;
import packed.internal.inject.service.assembly.ExportedServiceAssembly;
import packed.internal.inject.service.assembly.ProvideAllFromOtherInjector;
import packed.internal.inject.service.assembly.ServiceAssembly;
import packed.internal.inject.service.runtime.AbstractInjector;
import packed.internal.inject.service.runtime.PackedInjector;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;
import packed.internal.inject.sidecar.AtProvides;
import packed.internal.util.LookupUtil;

/**
 * Since the logic for the service extension is quite complex. Especially with cross-container integration. We spread it
 * over multiple classes. With this class being the main one.
 */
public final class InjectionManager {

    /** A VarHandle that can access ServiceExtension#im. */
    private static final VarHandle VH_SERVICE_EXTENSION_NODE = LookupUtil.vhPrivateOther(MethodHandles.lookup(), ServiceExtension.class, "im",
            InjectionManager.class);

    /** All injectables that needs to be resolved. */
    final ArrayList<Injectable> allInjectables = new ArrayList<>();

    /** All explicit added build entries. */
    public final ArrayList<ServiceAssembly<?>> buildEntries = new ArrayList<>();

    /** The container this injection manager belongs to. */
    public final ContainerAssembly container;

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public DependencyManager dependencies;

    /** An error manager that is lazily initialized. */
    private InjectionErrorManager em;

    /** A service exporter handles everything to do with exports of services. */
    @Nullable
    private ServiceManager services;

    /** All injectors added via {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
    private ArrayList<ProvideAllFromOtherInjector> provideAll;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, ServiceAssembly<?>> resolvedServices = new LinkedHashMap<>();

    /** A list of nodes to use when detecting dependency cycles. */
    public final ArrayList<Injectable> postProcessingInjectables = new ArrayList<>();

    /**
     * Creates a new injection manager.
     * 
     * @param container
     *            the container this manager belongs to
     */
    public InjectionManager(ContainerAssembly container) {
        this.container = requireNonNull(container);
    }

    /**
     * Adds the specified injectable to list of injectables that needs to be resolved.
     * 
     * @param injectable
     *            the injectable to add
     */
    public void addInjectable(Injectable injectable) {
        allInjectables.add(injectable);
    }

    public void buildTree(RegionAssembly resolver) {
        resolve(this, resolvedServices);

        if (em != null) {
            InjectionErrorManagerMessages.addDuplicateNodes(em.failingDuplicateProviders);
        }

        if (services != null) {
            services.resolveExports();
        }

        for (Injectable i : allInjectables) {
            i.resolve();
        }

        dependencies().checkForMissingDependencies(this);
        PostProcesser.dependencyCyclesDetect(resolver, postProcessingInjectables);
    }

    /**
     * Returns the dependency manager for this builder.
     * 
     * @return the dependency manager for this builder
     */
    public DependencyManager dependencies() {
        DependencyManager d = dependencies;
        if (d == null) {
            d = dependencies = new DependencyManager();
        }
        return d;
    }

    /**
     * Returns an error manager.
     * 
     * @return an error manager
     */
    public InjectionErrorManager errorManager() {
        InjectionErrorManager e = em;
        if (e == null) {
            e = em = new InjectionErrorManager();
        }
        return e;
    }

    /**
     * Returns the {@link ServiceManager} for this builder.
     * 
     * @return the service exporter for this builder
     */
    public ServiceManager services() {
        ServiceManager e = services;
        if (e == null) {
            e = services = new ServiceManager(this);
        }
        return e;
    }

    public ServiceContract newServiceContract() {
        return ServiceContract.newContract(c -> {
            if (services().hasExports()) {
                for (ExportedServiceAssembly<?> n : services().exports()) {
                    c.provides(n.key());
                }
            }
            dependencies().buildContract(c);
        });
    }

    public ServiceRegistry newServiceRegistry(ComponentNode comp, RuntimeRegion region, WireletPack wc) {
        LinkedHashMap<Key<?>, RuntimeService<?>> runtimeEntries = new LinkedHashMap<>();
        ServiceInstantiationContext con = new ServiceInstantiationContext(region);
        for (var e : services().exports()) {
            runtimeEntries.put(e.key(), e.toRuntimeEntry(con));
        }
        return new PackedInjector(comp.configSite(), runtimeEntries);
    }

    public void provideFromAtProvides(ComponentNodeConfiguration compConf, AtProvides atProvides) {
        ServiceAssembly<?> e = new AtProvideServiceAssembly<>(this, compConf, atProvides);
        buildEntries.add(e);
        allInjectables.add(e.injectable());
    }

    public void provideFromInjector(AbstractInjector injector, ConfigSite configSite, WireletList wirelets) {
        ProvideAllFromOtherInjector pi = new ProvideAllFromOtherInjector(this, configSite, injector, wirelets);
        ArrayList<ProvideAllFromOtherInjector> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(pi);
    }

    public <T> ServiceAssembly<T> provideFromSource(ComponentNodeConfiguration compConf, Key<T> key) {

        ServiceAssembly<T> e = new ComponentSourceServiceAssembly<>(this, compConf, key);
        buildEntries.add(e);
        return e;
    }

    public LinkedHashMap<Key<?>, ServiceAssembly<?>> resolve(InjectionManager im, LinkedHashMap<Key<?>, ServiceAssembly<?>> resolvedServices) {

        // First process provided entries, then any entries added via provideAll
        resolve0(im, resolvedServices, buildEntries);

        if (provideAll != null) {
            // All injectors have already had wirelets transform and filter
            for (ProvideAllFromOtherInjector fromInjector : provideAll) {
                resolve0(im, resolvedServices, fromInjector.entries.values());
            }
        }

        // Run through all linked containers...
        // Apply any wirelets to exports, and take

        // Add error messages if any nodes with the same key have been added multiple times
        return resolvedServices;
    }

    private void resolve0(InjectionManager im, LinkedHashMap<Key<?>, ServiceAssembly<?>> resolvedServices,
            Collection<? extends ServiceAssembly<?>> buildEntries) {
        for (ServiceAssembly<?> entry : buildEntries) {
            ServiceAssembly<?> existing = resolvedServices.putIfAbsent(entry.key(), entry);
            if (existing != null) {
                LinkedHashSet<ServiceAssembly<?>> hs = im.errorManager().failingDuplicateProviders.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>());
                hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                hs.add(entry);
            }
        }
    }

    /**
     * Extracts the service node from a service extension.
     * 
     * @param extension
     *            the extension to extract from
     * @return the service node
     */
    public static InjectionManager fromExtension(ServiceExtension extension) {
        return (InjectionManager) VH_SERVICE_EXTENSION_NODE.get(extension);
    }
}
