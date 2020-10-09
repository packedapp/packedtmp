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
import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.inject.Service;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceLocator;
import app.packed.service.Injector;
import packed.internal.component.ComponentNode;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedShellDriver;
import packed.internal.component.RuntimeRegion;
import packed.internal.component.wirelet.WireletList;
import packed.internal.container.ContainerBuild;
import packed.internal.inject.service.Requirement.FromInjectable;
import packed.internal.inject.service.build.ExportedServiceBuild;
import packed.internal.inject.service.build.ServiceBuild;
import packed.internal.inject.service.build.SourceInstanceServiceBuild;
import packed.internal.inject.service.runtime.AbstractServiceLocator;
import packed.internal.inject.service.runtime.PackedInjector;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;
import packed.internal.inject.service.sandbox.ProvideAllFromOtherInjector;

/**
 *
 */
public final class ServiceBuildManager {

    /** The container this service manager is a part of. */
    private final ContainerBuild container;

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public ServiceRequirementsManager dependencies;

    /** An error manager that is lazily initialized. */
    @Nullable
    private InjectionErrorManager em;

    /** A service exporter handles everything to do with exports of services. */
    @Nullable
    private ServiceExportManager exporter;

    /** All injectors added via {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
    private ArrayList<ProvideAllFromOtherInjector> provideAll;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, Wrapper> resolvedServices = new LinkedHashMap<>();

    /** All explicit added build entries. */
    private final ArrayList<ServiceBuild> unresolvedServices = new ArrayList<>();

    /**
     * @param container
     *            the container this service manager is a part of
     */
    public ServiceBuildManager(ContainerBuild container) {
        this.container = requireNonNull(container);
    }

    public void addAssembly(ServiceBuild a) {
        requireNonNull(a);
        unresolvedServices.add(a);
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
        ServiceContract.Builder b = ServiceContract.newContract();
        if (exporter != null) {
            for (ExportedServiceBuild n : exporter) {
                b.provides(n.key());
            }
        }
        if (dependencies != null && dependencies.requirements != null) {
            for (Requirement r : dependencies.requirements.values()) {
                if (r.isOptional) {
                    b.optional(r.key);
                } else {
                    b.requires(r.key);
                }
            }
        }
        return b.build();
    }

    public ServiceLocator newServiceLocator(ComponentNode comp, RuntimeRegion region) {
        LinkedHashMap<Key<?>, RuntimeService> runtimeEntries = new LinkedHashMap<>();
        ServiceInstantiationContext con = new ServiceInstantiationContext(region);
        for (var e : exports()) {
            runtimeEntries.put(e.key(), e.toRuntimeEntry(con));
        }

        // A hack to support Injector
        PackedShellDriver<?> psd = (PackedShellDriver<?>) container.compConf.assembly().shellDriver();
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

    public <T> ServiceBuild provideSource(ComponentNodeConfiguration compConf, Key<T> key) {
        ServiceBuild e = new SourceInstanceServiceBuild(this, compConf, key);
        unresolvedServices.add(e);
        return e;
    }

    private void resolve0(Collection<? extends ServiceBuild> buildEntries) {
        for (ServiceBuild entry : buildEntries) {
            Wrapper existing = resolvedServices.putIfAbsent(entry.key(), new Wrapper(entry));
            if (existing != null) {
                LinkedHashSet<ServiceBuild> hs = errorManager().failingDuplicateProviders.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>());
                hs.add(existing.build); // might be added multiple times, hence we use a Set, but add existing first
                hs.add(entry);
            }
        }
    }

    // Altsaa det er taenkt tll naar vi skal f.eks. slaa Wirelets op...
    // Saa det der med at resolve. Det er ikke services...
    // men injection...

    // Vi smide alt omkring services der...

    // Lazy laver den...

    public void resolveLocal() {
        // First process provided entries, then any entries added via provideAll
        resolve0(unresolvedServices);

        if (provideAll != null) {
            // All injectors have already had wirelets transform and filter
            for (ProvideAllFromOtherInjector fromInjector : provideAll) {
                resolve0(fromInjector.entries.values());
            }
        }

        if (container.children != null) {
            for (ContainerBuild c : container.children) {
                ServiceBuildManager child = c.getServiceManager();

                // Get Wirelets
                for (ExportedServiceBuild a : child.exports()) {
                    // System.out.println("EXPORT " + a);

                    // Skal vi wrappe den????
                    resolvedServices.put(a.key(), new Wrapper(a));
                }
            }
        }

        if (em != null) {
            InjectionErrorManagerMessages.addDuplicateNodes(em.failingDuplicateProviders);
        }

        if (exporter != null) {
            exporter.resolve();
        }
        // Run through all linked containers...
        // Apply any wirelets to exports, and take

        // Add error messages if any nodes with the same key have been added multiple times

        // also lets try and resolve children
        if (container.children != null) {
            for (ContainerBuild c : container.children) {
                ServiceBuildManager m = c.getServiceManager();
                ServiceRequirementsManager srm = m.dependencies;
                if (srm != null) {
                    for (Requirement r : srm.requirements.values()) {
                        Wrapper sa = resolvedServices.get(r.key);
                        if (sa != null) {
                            for (FromInjectable i : r.list) {
                                i.i.setDependencyProvider(i.dependencyIndex, sa.build);
                            }
                        }
                    }
                }
            }
        }
    }

    private static final class ExportedServiceLocator extends AbstractServiceLocator {

        private final ComponentNode component;

        /** All services that this injector provides. */
        private final Map<Key<?>, RuntimeService> services;

        private ExportedServiceLocator(ComponentNode component, Map<Key<?>, RuntimeService> services) {
            this.services = requireNonNull(services);
            this.component = requireNonNull(component);
        }

        @Override
        protected String failedToUseMessage(Key<?> key) {
            // /child [ss.BaseMyBundle] does not export a service with the specified key

            // FooBundle does not export a service with the key
            // It has an internal service. Maybe you forgot to export it()
            // Is that breaking encapsulation

            return "'" + component.path() + "' does not export a service with the specified key, key = " + key;
        }

        @Override
        @Nullable
        protected RuntimeService getService(Key<?> key) {
            return services.get(key);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        protected Map<Key<?>, Service> services() {
            return (Map) services;
        }
    }

}
