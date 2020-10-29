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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.Service;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceLocator;
import app.packed.inject.ServiceRegistry;
import packed.internal.component.ComponentBuild;
import packed.internal.component.PackedComponent;
import packed.internal.component.PackedShellDriver;
import packed.internal.component.RuntimeRegion;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.cube.CubeBuild;
import packed.internal.inject.service.Requirement.FromInjectable;
import packed.internal.inject.service.build.ServiceBuild;
import packed.internal.inject.service.build.SourceInstanceServiceBuild;
import packed.internal.inject.service.runtime.AbstractServiceLocator;
import packed.internal.inject.service.runtime.PackedInjector;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;
import packed.internal.inject.service.sandbox.Injector;
import packed.internal.inject.service.sandbox.ProvideAllFromServiceLocator;

/**
 *
 */
public final class ServiceBuildManager {

    /** The container this service manager is a part of. */
    private final CubeBuild container;

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public ServiceRequirementsManager dependencies;

    /** An error manager that is lazily initialized. */
    @Nullable
    private InjectionErrorManager em;

    /** A service exporter handles everything to do with exports of services. */
    @Nullable
    private ServiceExportManager exporter;

    /** All explicit added build entries. */
    private final ArrayList<ServiceBuild> localServices = new ArrayList<>();

    /** All injectors added via {@link ServiceExtension#provideAll(ServiceLocator)}. */
    private ArrayList<ProvideAllFromServiceLocator> provideAll;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, Wrapper> resolvedServices = new LinkedHashMap<>();

    /**
     * @param container
     *            the container this service manager is a part of
     */
    public ServiceBuildManager(CubeBuild container) {
        this.container = requireNonNull(container);
    }

    public void addAssembly(ServiceBuild a) {
        requireNonNull(a);
        localServices.add(a);
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

    @Nullable
    public ServiceRegistry newExportedServiceRegistry() {
        return exporter == null ? null : exporter.exportsAsServiceRegistry();
    }

    /**
     * Creates a service contract for this manager.
     * 
     * @return a service contract for this manager
     */
    public ServiceContract newServiceContract() {
        ServiceContract.Builder builder = ServiceContract.builder();

        // Any exports
        if (exporter != null) {
            for (ServiceBuild n : exporter) {
                builder.provides(n.key());
            }
        }

        // Any requirements
        if (dependencies != null && dependencies.requirements != null) {
            for (Requirement r : dependencies.requirements.values()) {
                if (r.isOptional) {
                    builder.optional(r.key);
                } else {
                    builder.requires(r.key);
                }
            }
        }

        return builder.build();
    }

    public ServiceLocator newServiceLocator(PackedComponent comp, RuntimeRegion region) {
        Map<Key<?>, RuntimeService> runtimeEntries = new LinkedHashMap<>();
        ServiceInstantiationContext con = new ServiceInstantiationContext(region);
        if (exporter != null) {
            for (ServiceBuild e : exporter) {
                runtimeEntries.put(e.key(), e.toRuntimeEntry(con));
            }
        }

        runtimeEntries = Map.copyOf(runtimeEntries);

        // A hack to support Injector
        PackedShellDriver<?> psd = (PackedShellDriver<?>) container.compConf.build().shellDriver();
        if (Injector.class.isAssignableFrom(psd.shellRawType())) {
            return new PackedInjector(comp.configSite(), runtimeEntries);
        } else {
            return new ExportedServiceLocator(comp, runtimeEntries);
        }
    }

    public void provideFromInjector(PackedInjector injector, ConfigSite configSite) {
        ProvideAllFromServiceLocator pi = new ProvideAllFromServiceLocator(this, configSite, injector);
        ArrayList<ProvideAllFromServiceLocator> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(pi);
    }

    public <T> ServiceBuild provideSource(ComponentBuild compConf, Key<T> key) {
        ServiceBuild e = new SourceInstanceServiceBuild(this, compConf, key);
        localServices.add(e);
        return e;
    }

    // Altsaa det er taenkt tll naar vi skal f.eks. slaa Wirelets op...
    // Saa det der med at resolve. Det er ikke services...
    // men injection...

    // Vi smide alt omkring services der...

    // Lazy laver den...

    public void resolve() {
        // First we take all locally defined services
        for (ServiceBuild entry : localServices) {
            resolvedServices.computeIfAbsent(entry.key(), k -> new Wrapper()).resolve(this, entry);
        }

        // Then we take any provideAll() services
        if (provideAll != null) {
            // All injectors have already had wirelets transform and filter
            for (ProvideAllFromServiceLocator fromInjector : provideAll) {
                for (ServiceBuild entry : fromInjector.entries.values()) {
                    resolvedServices.computeIfAbsent(entry.key(), k -> new Wrapper()).resolve(this, entry);
                }
            }
        }

        // Process exports from any children
        if (container.children != null) {
            for (CubeBuild c : container.children) {
                ServiceBuildManager child = c.getServiceManager();

                WireletPack wp = c.compConf.wirelets;
                List<ServiceWirelet1stPass> wirelets = wp == null ? null : wp.receiveAll(ServiceWirelet1stPass.class);
                if (wirelets != null) {
                    for (ServiceWirelet1stPass f : wirelets) {
                        f.process(child);
                    }
                }

                if (child.exporter != null) {
                    for (ServiceBuild a : child.exporter) {
                        resolvedServices.computeIfAbsent(a.key(), k -> new Wrapper()).resolve(this, a);
                    }
                }

            }
        }

        // We now know every resolved service within the container
        // Either a local one or one exported by a child

        if (em != null) {
            InjectionErrorManagerMessages.addDuplicateNodes(em.failingDuplicateProviders);
        }

        // Process own exports
        if (exporter != null) {
            exporter.resolve();
        }
        // Add error messages if any nodes with the same key have been added multiple times

        // Process imports to children
        if (container.children != null) {
            for (CubeBuild c : container.children) {
                ServiceBuildManager m = c.getServiceManager();

                ServiceRequirementsManager srm = m.dependencies;
                if (srm != null) {
                    for (Requirement r : srm.requirements.values()) {
                        Wrapper sa = resolvedServices.get(r.key);
                        if (sa != null) {
                            for (FromInjectable i : r.list) {
                                i.i.setDependencyProvider(i.dependencyIndex, sa.getSingle());
                            }
                        }
                    }
                }
            }
        }
    }

    /** A service locator wrapping all exported services. */
    private static final class ExportedServiceLocator extends AbstractServiceLocator {

        /** The root component */
        private final PackedComponent component;

        /** All services that this injector provides. */
        private final Map<Key<?>, RuntimeService> services;

        private ExportedServiceLocator(PackedComponent component, Map<Key<?>, RuntimeService> services) {
            this.services = requireNonNull(services);
            this.component = requireNonNull(component);
        }

        @Override
        protected String useFailedMessage(Key<?> key) {
            // /child [ss.BaseMyBundle] does not export a service with the specified key

            // FooBundle does not export a service with the key
            // It has an internal service. Maybe you forgot to export it()
            // Is that breaking encapsulation
            // container.realm().realmType();
            return "'" + component.path() + "' does not export a service with the specified key, key = " + key;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public Map<Key<?>, Service> asMap() {
            // as() + addAttribute on all services is disabled before we start the
            // export process. So ServiceBuild can be considered as effectively final
            return (Map) services; // TODO fix immutability
        }
    }
}
