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
import app.packed.inject.service.ServiceExtension;
import app.packed.inject.service.ServiceLocator;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.bean.BeanSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.PackedWireletSelection;
import packed.internal.container.WireletWrapper;
import packed.internal.inject.bean.DependencyNode;
import packed.internal.inject.manager.ApplicationInjectionManager;
import packed.internal.inject.manager.ParentableInjectionManager;
import packed.internal.inject.service.ServiceManagerRequirementsSetup.Requirement;
import packed.internal.inject.service.ServiceManagerRequirementsSetup.Requirement.FromInjectable;
import packed.internal.inject.service.build.BeanInstanceServiceSetup;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.inject.service.runtime.AbstractServiceLocator;
import packed.internal.inject.service.runtime.PackedInjector;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;
import packed.internal.lifetime.LifetimePool;
import packed.internal.service.sandbox.Injector;
import packed.internal.service.sandbox.ProvideAllFromServiceLocator;

/**
 * A service manager is responsible for managing the services for a single container at build time.
 * <p>
 * A {@link ApplicationInjectionManager} is responsible for managing 1 or more service manager tree that are directly
 * connected and part of the same build.
 */
public final class ContainerInjectionManager extends ParentableInjectionManager {

    /** An error manager that is lazily initialized. */
    @Nullable
    private ServiceManagerFailureSetup em;


    /** All dependants that needs to be resolved. */
    public final ArrayList<DependencyNode> consumers = new ArrayList<>();

    
    /** All explicit added build entries. */
    private final ArrayList<ServiceSetup> localServices = new ArrayList<>();

    /** Any parent this composer might have. */
    @Nullable
    private final ContainerInjectionManager parent;

    //// Taenker ikke de bliver added som beans... men som synthetics provide metoder paa en bean
    /** All locators added via {@link ServiceExtension#provideAll(ServiceLocator)}. */
    private ArrayList<ProvideAllFromServiceLocator> provideAll;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, ServiceDelegate> resolvedServices = new LinkedHashMap<>();

    /** The tree this service manager is a part of. */
    private final ApplicationInjectionManager applicationInjectionManager;

    public final InputOutputServiceManager ios = new InputOutputServiceManager(this);

    public final ContainerSetup container;

    /**
     * Adds the specified injectable to list of injectables that needs to be resolved.
     * 
     * @param dependant
     *            the injectable to add
     */
    public void addConsumer(DependencyNode dependant) {
        consumers.add(requireNonNull(dependant));

    }

    
    /**
     * @param root
     *            the container this service manager is a part of
     */
    public ContainerInjectionManager(ContainerSetup container, @Nullable ContainerInjectionManager parent) {
        this.container = container;
        this.parent = parent;
        this.applicationInjectionManager = parent == null ? new ApplicationInjectionManager() : parent.applicationInjectionManager;
    }

    public void addService(ServiceSetup service) {
        requireNonNull(service);
        localServices.add(service);
    }

    public void checkExportConfigurable() {
        // when processing wirelets
        // We should make sure some stuff is no longer configurable...
    }

    public void close() {
        if (parent == null) {
            applicationInjectionManager.finish(container.lifetime.pool, container);
        }
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

    public ServiceLocator newServiceLocator(PackedApplicationDriver<?> driver, LifetimePool region) {
        Map<Key<?>, RuntimeService> runtimeEntries = new LinkedHashMap<>();
        ServiceInstantiationContext con = new ServiceInstantiationContext(region);
        if (ios.hasExports()) {
            for (ServiceSetup export : ios.exports()) {
                runtimeEntries.put(export.key(), export.toRuntimeEntry(con));
            }

        }

        // make the entries immutable
        runtimeEntries = Map.copyOf(runtimeEntries);

        // A hack to support Injector
        if (Injector.class.isAssignableFrom(driver.applicationRawType())) {
            return new PackedInjector(runtimeEntries);
        } else {
            return new InputOutputServiceManager.ExportedServiceLocator(runtimeEntries);
        }
    }

    public void prepareDependants() {
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
                ContainerInjectionManager child = c.beans.getServiceManager();

                WireletWrapper wirelets = c.wirelets;
                if (wirelets != null) {
                    PackedWireletSelection.consumeEach(wirelets, Service1stPassWirelet.class, w -> w.process(child));
                }

                if (child != null && child.ios.hasExports()) {
                    for (ServiceSetup a : child.ios.exports()) {
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
        if (ios.hasExports()) {
            ios.exports().resolve(this);
        }
        // Add error messages if any nodes with the same key have been added multiple times

        // Process child requirements to children
        if (container.containerChildren != null) {
            for (ContainerSetup c : container.containerChildren) {
                ContainerInjectionManager m = c.beans.getServiceManager();
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
                if (ios.hasExports()) {
                    if (!ios.exports().contains(e.getKey())) {
                        map.put(e.getKey(), e.getValue().getSingle());
                    }
                }
            }
        }

        WireletWrapper wirelets = container.wirelets;
        if (wirelets != null) {
            // For now we just ignore the wirelets
            PackedWireletSelection.consumeEach(wirelets, Service2ndPassWirelet.class, w -> w.process(parent, this, map));
        }

        // If Processere wirelets...

        ServiceManagerRequirementsSetup srm = ios.requirements();
        if (srm != null) {
            for (Requirement r : srm.requirements.values()) {
                ServiceSetup sa = map.get(r.key);
                if (sa != null) {
                    for (FromInjectable i : r.list) {
                        i.i().setProducer(i.dependencyIndex(), sa);
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
        ServiceSetup e = new BeanInstanceServiceSetup(component, key);
        localServices.add(e);
        return e;
    }
}
