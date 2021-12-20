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
import java.util.Map;
import java.util.Map.Entry;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.service.Service;
import app.packed.inject.service.ServiceContract;
import app.packed.inject.service.ServiceExtension;
import app.packed.inject.service.ServiceLocator;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.component.bean.BeanSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.PackedWireletSelection;
import packed.internal.container.WireletWrapper;
import packed.internal.inject.dependency.ApplicationInjectorSetup;
import packed.internal.inject.service.ServiceManagerRequirementsSetup.Requirement;
import packed.internal.inject.service.ServiceManagerRequirementsSetup.Requirement.FromInjectable;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.inject.service.build.SourceInstanceServiceSetup;
import packed.internal.inject.service.runtime.AbstractServiceLocator;
import packed.internal.inject.service.runtime.PackedInjector;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;
import packed.internal.inject.service.sandbox.Injector;
import packed.internal.inject.service.sandbox.ProvideAllFromServiceLocator;
import packed.internal.lifetime.LifetimePool;
import packed.internal.lifetime.LifetimePoolSetup;

/**
 * A service manager is responsible for managing the services for a single container at build time.
 * <p>
 * A {@link ApplicationInjectorSetup} is responsible for managing 1 or more service manager tree that are directly
 * connected and part of the same build.
 */
// ServiceExtensionSetup
public final class ServiceManagerSetup {

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    private ServiceManagerRequirementsSetup dependencies;

    /** An error manager that is lazily initialized. */
    @Nullable
    private ServiceManagerFailureSetup em;

    /** Deals with everything about exporting services to a parent container. */
    private final ServiceManagerExportSetup exports = new ServiceManagerExportSetup(this);

    /** All explicit added build entries. */
    private final ArrayList<ServiceSetup> localServices = new ArrayList<>();

    /** Any parent this composer might have. */
    @Nullable
    private final ServiceManagerSetup parent;

    /** All injectors added via {@link ServiceExtension#provideAll(ServiceLocator)}. */
    private ArrayList<ProvideAllFromServiceLocator> provideAll;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, ServiceDelegate> resolvedServices = new LinkedHashMap<>();

    /** The tree this service manager is a part of. */
    private final ApplicationInjectorSetup tree;

    /**
     * @param root
     *            the container this service manager is a part of
     */
    public ServiceManagerSetup(@Nullable ServiceManagerSetup parent) {
        this.parent = parent;
        this.tree = parent == null ? new ApplicationInjectorSetup() : parent.tree;
    }

    public void addAssembly(ServiceSetup a) {
        requireNonNull(a);
        localServices.add(a);
    }

    public void checkExportConfigurable() {
        // when processing wirelets
        // We should make sure some stuff is no longer configurable...
    }

    public void close(ContainerSetup container, LifetimePoolSetup pool) {
        if (parent == null) {
            tree.finish(pool, container);
        }
    }

    /**
     * Returns the dependency manager for this builder.
     * 
     * @return the dependency manager for this builder
     */
    public ServiceManagerRequirementsSetup dependencies() {
        ServiceManagerRequirementsSetup d = dependencies;
        if (d == null) {
            d = dependencies = new ServiceManagerRequirementsSetup();
        }
        return d;
    }

    /**
     * Returns an error manager.
     * 
     * @return an error manager
     */
    public ServiceManagerFailureSetup errorManager() {
        ServiceManagerFailureSetup e = em;
        if (e == null) {
            e = em = new ServiceManagerFailureSetup();
        }
        return e;
    }

    /**
     * Returns the {@link ServiceManagerExportSetup} for this builder.
     * 
     * @return the service exporter for this builder
     */
    public ServiceManagerExportSetup exports() {
        return exports;
    }

    /** {@return a service contract for this manager.} */
    public ServiceContract newServiceContract() {
        ServiceContract.Builder builder = ServiceContract.builder();

        // Add exports
        if (exports != null) {
            for (ServiceSetup n : exports) {
                builder.provides(n.key());
            }
        }

        // Add requirements (mandatory or optional)
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

    public ServiceLocator newServiceLocator(PackedApplicationDriver<?> driver, LifetimePool region) {
        Map<Key<?>, RuntimeService> runtimeEntries = new LinkedHashMap<>();
        ServiceInstantiationContext con = new ServiceInstantiationContext(region);
        for (ServiceSetup export : exports) {
            runtimeEntries.put(export.key(), export.toRuntimeEntry(con));
        }

        // make the entries immutable
        runtimeEntries = Map.copyOf(runtimeEntries);

        // A hack to support Injector
        if (Injector.class.isAssignableFrom(driver.applicationRawType())) {
            return new PackedInjector(runtimeEntries);
        } else {
            return new ExportedServiceLocator(runtimeEntries);
        }
    }

    public void prepareDependants(ContainerSetup container) {
        // First we take all locally defined services
        for (ServiceSetup entry : localServices) {
            resolvedServices.computeIfAbsent(entry.key(), k -> new ServiceDelegate()).resolve(this, entry);
        }

        // Then we take any provideAll() services
        if (provideAll != null) {
            // All injectors have already had wirelets transform and filter
            for (ProvideAllFromServiceLocator fromInjector : provideAll) {
                for (ServiceSetup entry : fromInjector.entries.values()) {
                    resolvedServices.computeIfAbsent(entry.key(), k -> new ServiceDelegate()).resolve(this, entry);
                }
            }
        }

        // Process exports from any children
        if (container.containerChildren != null) {
            for (ContainerSetup c : container.containerChildren) {
                ServiceManagerSetup child = c.injection.getServiceManager();

                WireletWrapper wirelets = c.wirelets;
                if (wirelets != null) {
                    PackedWireletSelection.consumeEach(wirelets, Service1stPassWirelet.class, w -> w.process(child));
                }

                if (child != null && child.exports != null) {
                    for (ServiceSetup a : child.exports) {
                        resolvedServices.computeIfAbsent(a.key(), k -> new ServiceDelegate()).resolve(this, a);
                    }
                }
            }
        }

        // We now know every resolved service within the container
        // Either a local one or one exported by a child

        if (em != null) {
            ServiceManagerFailureSetup.addDuplicateNodes(em.failingDuplicateProviders);
        }

        // Process own exports
        if (exports != null) {
            exports.resolve();
        }
        // Add error messages if any nodes with the same key have been added multiple times

        // Process child requirements to children
        if (container.containerChildren != null) {
            for (ContainerSetup c : container.containerChildren) {
                ServiceManagerSetup m = c.injection.getServiceManager();
                if (m != null) {
                    m.processWirelets(container);
                }
            }
        }
    }

    private void processWirelets(ContainerSetup container) {
        LinkedHashMap<Key<?>, ServiceSetup> map = new LinkedHashMap<>();

        if (parent != null) {
            for (Entry<Key<?>, ServiceDelegate> e : parent.resolvedServices.entrySet()) {
                // we need to remove all of our exports.
                if (!exports().contains(e.getKey())) {
                    map.put(e.getKey(), e.getValue().getSingle());
                }
            }
        }

        WireletWrapper wirelets = container.wirelets;
        if (wirelets != null) {
            // For now we just ignore the wirelets
            PackedWireletSelection.consumeEach(wirelets, Service2ndPassWirelet.class, w -> w.process(parent, this, map));
        }

        // If Processere wirelets...

        ServiceManagerRequirementsSetup srm = dependencies;
        if (srm != null) {
            for (Requirement r : srm.requirements.values()) {
                ServiceSetup sa = map.get(r.key);
                if (sa != null) {
                    for (FromInjectable i : r.list) {
                        i.i().setDependencyProvider(i.dependencyIndex(), sa);
                    }
                }
            }
        }
    }

    public void provideAll(AbstractServiceLocator locator) {
        // We add this immediately to resolved services, as their keys are immutable.

        ProvideAllFromServiceLocator pi = new ProvideAllFromServiceLocator(this, locator);
        ArrayList<ProvideAllFromServiceLocator> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(pi);
    }

    public <T> ServiceSetup provideSource(BeanSetup component, Key<T> key) {
        ServiceSetup e = new SourceInstanceServiceSetup(this, component, key);
        localServices.add(e);
        return e;
    }

    /** A service locator wrapping all exported services. */
    private static final class ExportedServiceLocator extends AbstractServiceLocator {

        /** All services that this injector provides. */
        private final Map<Key<?>, ? extends Service> services;

        private ExportedServiceLocator(Map<Key<?>, ? extends Service> services) {
            this.services = requireNonNull(services);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public Map<Key<?>, Service> asMap() {
            // as() + addAttribute on all services is disabled before we start the
            // export process. So ServiceBuild can be considered as effectively final
            return (Map) services;
        }

        @Override
        protected String useFailedMessage(Key<?> key) {
            // /child [ss.BaseMyAssembly] does not export a service with the specified key

            // FooAssembly does not export a service with the key
            // It has an internal service. Maybe you forgot to export it()
            // Is that breaking encapsulation
            // container.realm().realmType();
            return "A service with the specified key, key = " + key;
        }
    }
}
