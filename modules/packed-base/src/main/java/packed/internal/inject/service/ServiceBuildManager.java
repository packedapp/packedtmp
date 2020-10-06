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
package packed.internal.inject.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceLocator;
import app.packed.service.Injector;
import packed.internal.component.ComponentNode;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedShellDriver;
import packed.internal.component.RuntimeRegion;
import packed.internal.component.wirelet.WireletList;
import packed.internal.container.ContainerAssembly;
import packed.internal.inject.InjectionErrorManagerMessages;
import packed.internal.inject.InjectionManager;
import packed.internal.inject.service.Requirement.FromInjectable;
import packed.internal.inject.service.assembly.ComponentSourceServiceAssembly;
import packed.internal.inject.service.assembly.ExportedServiceAssembly;
import packed.internal.inject.service.assembly.ProvideAllFromOtherInjector;
import packed.internal.inject.service.assembly.ServiceAssembly;
import packed.internal.inject.service.runtime.ExportedServiceLocator;
import packed.internal.inject.service.runtime.PackedInjector;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;

/**
 *
 */
public final class ServiceBuildManager {

    /** All explicit added build entries. */
    private final ArrayList<ServiceAssembly<?>> assemblies = new ArrayList<>();

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public ServiceRequirementsManager dependencies;

    /** A service exporter handles everything to do with exports of services. */
    @Nullable
    private ServiceExportManager exporter;

    /** The injection manager this service manager is a part of. */
    public final InjectionManager im;

    /** All injectors added via {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
    private ArrayList<ProvideAllFromOtherInjector> provideAll;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, ServiceAssembly<?>> resolvedServices = new LinkedHashMap<>();

    public final ArrayList<ServiceBuildManager> children = new ArrayList<>();

    /**
     * @param im
     */
    public ServiceBuildManager(InjectionManager im) {
        this.im = requireNonNull(im);
    }

    public void checkExportConfigurable() {
        // when processing wirelets
        // We should make sure some stuff is no longer configurable...
    }

    /**
     * Returns the dependency manager for this builder.
     * 
     * @return the dependency manager for this builder
     */
    public ServiceRequirementsManager dependencies() {
        ServiceRequirementsManager d = dependencies;
        if (d == null) {
            d = dependencies = new ServiceRequirementsManager();
        }
        return d;
    }

    /**
     * Returns the {@link ServiceExportManager} for this builder.
     * 
     * @return the service exporter for this builder
     */
    public ServiceExportManager exports() {
        ServiceExportManager e = exporter;
        if (e == null) {
            e = exporter = new ServiceExportManager(this);
        }
        return e;
    }

    public boolean hasExports() {
        return exporter != null;
    }

    public ServiceContract newServiceContract() {
        return ServiceContract.newContract(c -> {
            if (exporter != null) {
                for (ExportedServiceAssembly<?> n : exports()) {
                    c.provides(n.key());
                }
            }
            if (dependencies != null) {
                if (dependencies.requirements != null) {
                    for (Requirement r : dependencies.requirements.values()) {
                        if (r.isOptional) {
                            c.optional(r.key);
                        } else {
                            c.requires(r.key);
                        }
                    }
                }
            }
        });
    }

    public ServiceLocator newServiceLocator(ComponentNode comp, RuntimeRegion region) {
        LinkedHashMap<Key<?>, RuntimeService<?>> runtimeEntries = new LinkedHashMap<>();
        ServiceInstantiationContext con = new ServiceInstantiationContext(region);
        for (var e : exports()) {
            runtimeEntries.put(e.key(), e.toRuntimeEntry(con));
        }

        // A hack to support Injector
        PackedShellDriver<?> psd = (PackedShellDriver<?>) im.container.compConf.assembly().shellDriver();
        if (Injector.class.isAssignableFrom(psd.shellRawType())) {
            return new PackedInjector(comp.configSite(), runtimeEntries);
        } else {
            return new ExportedServiceLocator(comp, runtimeEntries);
        }
    }

    public void provideFromInjector(PackedInjector injector, ConfigSite configSite, WireletList wirelets) {
        ProvideAllFromOtherInjector pi = new ProvideAllFromOtherInjector(this, configSite, injector, wirelets);
        ArrayList<ProvideAllFromOtherInjector> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(pi);
    }

    public void addAssembly(ServiceAssembly<?> a) {
        requireNonNull(a);
        assemblies.add(a);
    }

    public <T> ServiceAssembly<T> provideSource(ComponentNodeConfiguration compConf, Key<T> key) {
        ServiceAssembly<T> e = new ComponentSourceServiceAssembly<>(this, compConf, key);
        assemblies.add(e);
        return e;
    }

    public void resolveLocal() {
        // First process provided entries, then any entries added via provideAll
        resolve0(im, resolvedServices, assemblies);

        if (provideAll != null) {
            // All injectors have already had wirelets transform and filter
            for (ProvideAllFromOtherInjector fromInjector : provideAll) {
                resolve0(im, resolvedServices, fromInjector.entries.values());
            }
        }
        for (ServiceBuildManager m : children) {
            for (ExportedServiceAssembly<?> a : m.exports()) {
                // System.out.println("EXPORT " + a);

                // Skal vi wrappe den????

                resolvedServices.putIfAbsent(a.key(), a);
            }
        }

        if (im.em != null) {
            InjectionErrorManagerMessages.addDuplicateNodes(im.em.failingDuplicateProviders);
        }

        resolveExports();
        // Run through all linked containers...
        // Apply any wirelets to exports, and take

        // Add error messages if any nodes with the same key have been added multiple times

        // also lets try and resolve children
        for (ServiceBuildManager sbm : children) {
            ServiceRequirementsManager srm = sbm.dependencies;
            if (srm != null) {
                for (Requirement r : srm.requirements.values()) {
                    ServiceAssembly<?> sa = resolvedServices.get(r.key);
                    if (resolvedServices.containsKey(r.key)) {
                        for (FromInjectable i : r.list) {
                            i.i.setDependencyProvider(i.dependencyIndex, sa);
                        }
                    }
                }
            }
        }

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

    // Vi smide alt omkring services der...

    // Lazy laver den...

    // Altsaa det er taenkt tll naar vi skal f.eks. slaa Wirelets op...
    // Saa det der med at resolve. Det er ikke services...
    // men injection...
    public void resolveExports() {
        if (exporter != null) {
            exporter.resolve();
            ContainerAssembly parent = im.container.parent;
            if (parent != null) {
                ServiceBuildManager sm = parent.im.getServiceManager();
                if (sm != null) {
                    sm.children.add(this);
                }
            }
        }
    }

}
