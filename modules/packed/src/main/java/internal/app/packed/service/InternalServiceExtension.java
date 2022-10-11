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
package internal.app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceLocator;
import internal.app.packed.application.PackedApplicationDriver;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.PackedWireletSelection;
import internal.app.packed.container.WireletWrapper;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.service.ServiceManagerRequirementsSetup.Requirement;
import internal.app.packed.service.ServiceManagerRequirementsSetup.Requirement.FromInjectable;
import internal.app.packed.service.build.BeanInstanceServiceSetup;
import internal.app.packed.service.build.ProvideAllFromServiceLocator;
import internal.app.packed.service.build.ServiceSetup;
import internal.app.packed.service.inject.ContainerOrExtensionInjectionManager;
import internal.app.packed.service.inject.DependencyNode;
import internal.app.packed.service.runtime.AbstractServiceLocator;
import internal.app.packed.service.runtime.RuntimeService;
import internal.app.packed.service.runtime.ServiceInstantiationContext;

/**
 * A service manager is responsible for managing the services for a single container at build time.
 */
public final class InternalServiceExtension extends ContainerOrExtensionInjectionManager {

    /** All dependants that needs to be resolved. */
    public final ArrayList<DependencyNode> consumers = new ArrayList<>();

    /** */
    private final ContainerSetup container;

    /** An error manager that is lazily initialized. */
    @Nullable
    private ServiceManagerFailureSetup em;

    public final ContainerServiceBinder ios = new ContainerServiceBinder(this);

    /** All explicit added build entries. */
    private final ArrayList<ServiceSetup> localServices = new ArrayList<>();

    /** Any parent this composer might have. */
    @Nullable
    private final InternalServiceExtension parent;

    //// Taenker ikke de bliver added som beans... men som synthetics provide metoder paa en bean
    /** All locators added via {@link ServiceExtension#provideAll(ServiceLocator)}. */
    private ArrayList<ProvideAllFromServiceLocator> provideAll;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, ServiceDelegate> resolvedServices = new LinkedHashMap<>();

    /**
     * @param root
     *            the container this service manager is a part of
     */
    public InternalServiceExtension(ContainerSetup container) {
        this.container = container;
        // TODO Husk at checke om man har en extension realm inde
        this.parent = container.treeParent == null ? null : container.treeParent.injectionManager;

    }

    /**
     * Adds the specified injectable to list of injectables that needs to be resolved.
     * 
     * @param dependant
     *            the injectable to add
     */
    public void addConsumer(DependencyNode dependant) {
        consumers.add(requireNonNull(dependant));
    }

    public void addService(ServiceSetup service) {
        requireNonNull(service);
        localServices.add(service);
    }

    public void checkExportConfigurable() {
        // when processing wirelets
        // We should make sure some stuff is no longer configurable...
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

    public ServiceLocator newNewServiceLocator(PackedApplicationDriver<?> driver, LifetimeObjectArena region) {
        Map<Key<?>, RuntimeService> runtimeEntries = new LinkedHashMap<>();
        ServiceInstantiationContext con = new ServiceInstantiationContext(region);
        if (ios.hasExports()) {
            for (ServiceSetup export : ios.exports()) {
                runtimeEntries.put(export.key(), export.toRuntimeEntry(con));
            }
        }

        return new PackedServiceLocator(Map.copyOf(runtimeEntries));
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
        for (var c = container.treeFirstChild; c != null; c = c.treeNextSiebling) {
            InternalServiceExtension child = c.injectionManager;

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
        for (var c = container.treeFirstChild; c != null; c = c.treeNextSiebling) {
            InternalServiceExtension m = c.injectionManager;
            if (m != null) {
                m.processWirelets(container);
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

    public void resolve() {
        for (var c = container.treeFirstChild; c != null; c = c.treeNextSiebling) {
            if (c.realm == container.realm) {
                c.injectionManager.resolve();
            }
        }

        // Resolve local services
        prepareDependants();

        for (DependencyNode i : consumers) {
            i.resolve(this);
        }

        // Now we know every dependency that we are missing
        // I think we must plug this in somewhere

        ios.requirementsOrCreate().checkForMissingDependencies();

        // TODO Check any contracts we might as well catch it early
    }
}
