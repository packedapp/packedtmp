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
package internal.app.packed.service.old;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.packed.application.BuildException;
import app.packed.framework.Nullable;
import app.packed.service.Key;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceLocator;
import internal.app.packed.application.PackedApplicationDriver;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.ExtensionTreeSetup;
import internal.app.packed.container.WireletWrapper;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.lifetime.pool.LifetimeAccessor;
import internal.app.packed.lifetime.pool.LifetimeAccessor.DynamicAccessor;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationTarget.LifetimePoolAccessTarget;
import internal.app.packed.service.PackedServiceLocator;
import internal.app.packed.service.ProvidedService;

/**
 * A service manager is responsible for managing the services for a single container at build time.
 */
public final class InternalServiceExtension extends ContainerOrExtensionInjectionManager {

    HashMap<BeanSetup, BuildtimeService> beans = new HashMap<>();

    /** */
    public final ContainerSetup container;

    /** All dependants that needs to be resolved. */
    public final ArrayList<DependencyNode> dependecyNodes = new ArrayList<>();

    /**
     * An entry to this list is added every time the user calls {@link ServiceExtension#export(Class)},
     * {@link ServiceExtension#export(Key)}.
     */
    final ArrayList<BuildtimeExportedService> exportedEntries = new ArrayList<>();

    /**
     * Creates an export for the specified configuration.
     * 
     * @param <T>
     *            the type of service
     * @param entryToExport
     *            the entry to export
     * @return stuff
     */
    // I think exporting an entry locks its any providing key it might have...

    /** Deals with everything about exporting services to a parent container. */
    public boolean hasExports;

    /** All explicit added build entries. */
    private final ArrayList<BuildtimeService> localServices = new ArrayList<>();

    /** Any parent this composer might have. */
    @Nullable
    private final InternalServiceExtension parent;

    /**
     * Whether or not the user must explicitly specify all required services. Via {@link ServiceExtension#require(Key...)},
     * {@link ServiceExtension#requireOptionally(Key...)} and add contract.
     * <p>
     * In previous versions we kept this information on a per node basis. However, it does not work properly with "foreign"
     * hook methods that make use of injection. Because they may not be processed until the bitter end, so it was only
     * really services registered via the provide methods that could make use of them.
     */
    // Skal jo erstattet af noget Contract...
    // boolean manualRequirementsManagement;

    final LinkedHashMap<Key<?>, Requirement> requirements = new LinkedHashMap<>();

    /** All resolved exports. Is null until {@link #resolve()} has finished (successfully or just finished?). */
    @Nullable
    public final LinkedHashMap<Key<?>, BuildtimeService> resolvedExports = new LinkedHashMap<>();

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
    void addConsumer(OperationSetup operation, LifetimeAccessor la) {
        DependencyNode dependant = new DependencyNode(operation, la);
        dependecyNodes.add(requireNonNull(dependant));
    }

    /** Handles everything to do with dependencies, for example, explicit requirements. */

    public void export(BeanSetup bean) {
        BuildtimeService bss = beans.get(bean);
        exportedEntries.add(new BuildtimeExportedService(bss));
    }

    public ServiceLocator newServiceLocator(PackedApplicationDriver<?> driver, LifetimeObjectArena region) {
        Map<Key<?>, MethodHandle> runtimeEntries = new LinkedHashMap<>();
        for (Entry<Key<?>, BuildtimeService> export : resolvedExports.entrySet()) {
            runtimeEntries.put(export.getKey(), export.getValue().buildInvoker(region));
        }
        return new PackedServiceLocator(region, Map.copyOf(runtimeEntries));
    }

    /** {@return a service contract for this manager.} */
    public ServiceContract newServiceContract() {
        ServiceContract.Builder builder = ServiceContract.builder();

        container.sm.exports.keySet().forEach(k -> builder.provide(k));

        // Add requirements (mandatory or optional)
        if (requirements != null && requirements != null) {
            for (Requirement r : requirements.values()) {
                if (r.isOptional) {
                    builder.requireOptional(r.key);
                } else {
                    builder.require(r.key);
                }
            }
        }

        return builder.build();
    }

    /**
     * @param provider
     */
    public void provideOld(ProvidedService provider) {
        OperationSetup o = provider.operation;
        BuildtimeService bis ;
        if (o.target instanceof LifetimePoolAccessTarget bia) {
            //addService(o.bean, provider.entry.key);
            
            OperationSetup os = null;
            LifetimeAccessor accessor = null;
            if (o.bean.injectionManager.lifetimePoolAccessor == null) {
                os = o.bean.operations.get(0);
            } else {
                accessor = o.bean.injectionManager.lifetimePoolAccessor;
            }

             bis = new BuildtimeBeanMemberService(provider.entry.key, os, accessor);
            beans.put(o.bean, bis);
        } else {
            boolean isStatic = !o.target.requiresBeanInstance;
            if (!isStatic && o.bean.injectionManager.lifetimePoolAccessor == null) {
                throw new BuildException("Not okay)");
            }
            DynamicAccessor accessor = provider.isConstant ? o.bean.container.lifetime.pool.reserve(Object.class) : null;
             bis = new BuildtimeBeanMemberService(provider.entry.key, o, accessor);

            addConsumer(o, accessor);
        }

        container.safeUseExtensionSetup(ServiceExtension.class, null);
        localServices.add(bis);
    }

    public void resolve() {
        for (var c = container.treeFirstChild; c != null; c = c.treeNextSiebling) {
            if (c.assembly == container.assembly) {
                c.injectionManager.resolve();
            }
        }

        for (BuildtimeService entry : localServices) {
            resolvedServices.computeIfAbsent(entry.key(), k -> new ServiceDelegate()).resolve(this, entry);
        }


        // Process exports from any children
        for (var c = container.treeFirstChild; c != null; c = c.treeNextSiebling) {
            InternalServiceExtension child = c.injectionManager;

            WireletWrapper wirelets = c.wirelets;
            if (wirelets != null) {
                // PackedWireletSelection.consumeEach(wirelets, Service1stPassWirelet.class, w -> w.process(child));
            }

            if (child != null) {
                for (BuildtimeService a : child.resolvedExports.values()) {
                    resolvedServices.computeIfAbsent(a.key(), k -> new ServiceDelegate()).resolve(this, a);
                }
            }
        }

        // We now know every resolved service within the container
        // Either a local one or one exported by a child

        // Process own exports
        if (hasExports) {
            for (BuildtimeExportedService entry : exportedEntries) {
                // try and find a matching service entry for key'ed exports via
                // exportedEntry != null for entries added via InjectionExtension#export(ProvidedComponentConfiguration)
                BuildtimeService entryToExport = entry.serviceToExport;
                if (entryToExport == null) {
                    ServiceDelegate wrapper = resolvedServices.get(null);
                    entryToExport = wrapper == null ? null : wrapper.getSingle();
                    entry.serviceToExport = entryToExport;
                }

                if (entry.serviceToExport != null) {
                    resolvedExports.putIfAbsent(entry.key, entry);
                }
            }

            if (container.sm.exportAll) {
                for (ServiceDelegate w : resolvedServices.values()) {
                    BuildtimeService e = w.getSingle();
                    if (!resolvedExports.containsKey(e.key())) {
                        resolvedExports.put(e.key(), new BuildtimeExportedService(e));
                    }
                }
            }
        }
        // Add error messages if any nodes with the same key have been added multiple times

        // Process child requirements to children

        for (DependencyNode i : dependecyNodes) {
            resolve1(i);
        }
    }

    private void resolve1(DependencyNode node) {
        List<InternalDependency> dependencies = InternalDependency.fromOperationType(node.operation.type);// null;//factory.dependencies();
        for (int i = 0; i < dependencies.size(); i++) {
            InternalDependency sd = dependencies.get(i);

            Object e;

            if (node.operation.bean.realm instanceof ExtensionTreeSetup ers) {
                Key<?> requiredKey = sd.key();
                Key<?> thisKey = Key.of(node.operation.bean.beanClass);
                ContainerSetup container = node.operation.bean.container;
                ExtensionSetup es = container.safeUseExtensionSetup(ers.realmType(), null);
                BeanSetup bs = null;
                if (thisKey.equals(requiredKey)) {
                    if (es.treeParent != null) {
                        bs = es.treeParent.injectionManager.lookup(requiredKey);
                    }
                } else {
                    bs = es.injectionManager.lookup(requiredKey);
                }
                if (bs == null) {
                    throw new RuntimeException("Could not resolve key " + requiredKey + " for " + ers.realmType());
                }

                e = bs.injectionManager;

            } else {
                ServiceDelegate wrapper = resolvedServices.get(sd.key());
                e = wrapper == null ? null : wrapper.getSingle();
            }

            if (e == null) {
                Requirement r = requirements.computeIfAbsent(sd.key(), Requirement::new);
                r.missingDependency(sd);
            }
        }
    }

    // En wrapper der goer at vi kan delay lidt det at smide exceptiosn for dublicate keys.
    public final class ServiceDelegate {

        private BuildtimeService service;

        public BuildtimeService getSingle() {
            requireNonNull(service);
            return service;
        }

        void resolve(InternalServiceExtension sbm, BuildtimeService b) {
            if (service == null) {
                this.service = b;
            }
        }
    }

    static class Requirement {

        // Always starts out as optional
        boolean isOptional = true;

        final Key<?> key;

        Requirement(Key<?> key) {
            this.key = key;
        }

        void missingDependency(InternalDependency d) {
            if (!d.isOptional()) {
                isOptional = false;
            }
        }
    }
}
