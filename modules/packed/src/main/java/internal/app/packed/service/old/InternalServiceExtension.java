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

    /** All dependants that needs to be resolved. */
    public final ArrayList<DependencyNode> dependecyNodes = new ArrayList<>();

    /** */
    public final ContainerSetup container;
    /** The config site, if we export all entries. */
    public boolean exportAll;

    /**
     * An entry to this list is added every time the user calls {@link ServiceExtension#export(Class)},
     * {@link ServiceExtension#export(Key)}.
     */
    final ArrayList<BuildtimeExportedService> exportedEntries = new ArrayList<>();

    /** Deals with everything about exporting services to a parent container. */
    private boolean hasExports;

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

    /** Handles everything to do with dependencies, for example, explicit requirements. */


    public void export(BeanSetup bean, BuildtimeService entryToExport) {
        // I'm not sure we need the check after, we have put export() directly on a component configuration..
        // Perviously you could specify any entry, even something from another assembly.
        // if (entryToExport.node != node) {
        // throw new IllegalArgumentException("The specified configuration was created by another injector extension");
        // }
        BuildtimeService bss = beans.get(bean);
        requireNonNull(bss);
        var entry = new BuildtimeExportedService(bss);
        // Vi bliver noedt til at vente til vi har resolvet... med finde ud af praecis hvad der skal ske
        // F.eks. hvis en extension publisher en service vi gerne vil exportere
        // Saa sker det maaske foerst naar den completer.
        // dvs efter assembly.configure() returnere

        exportsOrCreate();
        exportedEntries.add(entry);
    }

    /**
     * Returns the dependency manager for this builder.
     * 
     * @return the dependency manager for this builder
     */
    public void exportsOrCreate() {
        hasExports = true;
    }

    public boolean hasExports() {
        return hasExports;
    }

    public boolean hasRequirements() {
        return requirements != null;
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
     * Record a dependency that could not be resolved
     * 
     * @param entry
     * @param dependency
     */
    public void recordResolvedDependency(DependencyNode entry, int index, InternalDependency dependency, @Nullable Object resolvedTo, boolean fromParent) {
        requireNonNull(entry);
        requireNonNull(dependency);
        if (resolvedTo != null) {
            return;
        }
        Key<?> key = dependency.key();

        Requirement r = requirements.computeIfAbsent(key, Requirement::new);
        r.missingDependency(entry, index, dependency);
    }

    /**
     * This method tries to find matching entries for exports added via {@link ServiceExtension#export(Class)}and
     * {@link ServiceExtension#export(Key)}. We cannot do when they are called, as we allow export statements of entries at
     * any point, even before the
     */
    public void resolve(InternalServiceExtension sm) {
        // We could move unresolvedKeyedExports and duplicateExports in here. But keep them as fields
        // to have identical structure to ServiceProvidingManager
        // Process every exported build entry
        for (BuildtimeExportedService entry : exportedEntries) {
            // try and find a matching service entry for key'ed exports via
            // exportedEntry != null for entries added via InjectionExtension#export(ProvidedComponentConfiguration)
            BuildtimeService entryToExport = entry.serviceToExport;
            if (entryToExport == null) {
                ServiceDelegate wrapper = sm.resolvedServices.get(entry.exportAsKey);
                entryToExport = wrapper == null ? null : wrapper.getSingle();
                entry.serviceToExport = entryToExport;
            }

            if (entry.serviceToExport != null) {
                BuildtimeService existing = resolvedExports.putIfAbsent(entry.key, entry);
                if (existing != null) {
//                        LinkedHashSet<ServiceSetup> hs = sm.errorManager().failingDuplicateExports.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>());
//                        hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
//                        hs.add(entry);
                }
            }
        }

        if (container.sm.exportAll) {
            for (ServiceDelegate w : sm.resolvedServices.values()) {
                BuildtimeService e = w.getSingle();
                if (!resolvedExports.containsKey(e.key)) {
                    resolvedExports.put(e.key, new BuildtimeExportedService(e));
                }
            }
        }
        // Finally, make the resolved exports visible.
    }

    static class Requirement {

        // Always starts out as optional
        boolean isOptional = true;

        final Key<?> key;

        final ArrayList<FromInjectable> list = new ArrayList<>();

        Requirement(Key<?> key) {
            this.key = key;
        }

        void missingDependency(DependencyNode i, int dependencyIndex, InternalDependency d) {
            if (!d.isOptional()) {
                isOptional = false;
            }
            list.add(new FromInjectable(i, dependencyIndex, d));
        }

        record FromInjectable(DependencyNode i, int dependencyIndex, InternalDependency d) {}
    }

    record ServiceDependencyRequirement(InternalDependency dependency, DependencyNode entry) {}

    /** All explicit added build entries. */
    private final ArrayList<BuildtimeService> localServices = new ArrayList<>();

    /** Any parent this composer might have. */
    @Nullable
    private final InternalServiceExtension parent;


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
        dependecyNodes.add(requireNonNull(dependant));
    }

    HashMap<BeanSetup, BuildtimeService> beans = new HashMap<>();

    public void addService(BeanSetup bean, Key<?> key) {

        OperationSetup os = null;
        LifetimeAccessor accessor = null;
        if (bean.injectionManager.lifetimePoolAccessor == null) {
            os = bean.operations.get(0);
        } else {
            accessor = bean.injectionManager.lifetimePoolAccessor;
        }

        BuildtimeService bis = new BuildtimeBeanMemberService(key, os, accessor);
        beans.put(bean, bis);
        addService(bis);
    }

    public void addService(BuildtimeService service) {
        requireNonNull(service);
        container.safeUseExtensionSetup(ServiceExtension.class, null);
        localServices.add(service);
    }

    public void checkExportConfigurable() {
        // when processing wirelets
        // We should make sure some stuff is no longer configurable...
    }

    public ServiceLocator newNewServiceLocator(PackedApplicationDriver<?> driver, LifetimeObjectArena region) {
        Map<Key<?>, MethodHandle> runtimeEntries = new LinkedHashMap<>();
        if (hasExports()) {
            for (BuildtimeService export : resolvedExports.values()) {
                runtimeEntries.put(export.key, export.newRuntimeNode(region));
            }
        }

        return new PackedServiceLocator(region, Map.copyOf(runtimeEntries));
    }

    public void prepareDependants() {
        // First we take all locally defined services
        for (BuildtimeService entry : localServices) {
            resolvedServices.computeIfAbsent(entry.key, k -> new ServiceDelegate()).resolve(this, entry);
        }

        // Then we take any provideAll() services
//        if (provideAll != null) {
//            // All injectors have already had wirelets transform and filter
//            for (ProvideAllFromServiceLocator fromInjector : provideAll) {
//                for (ServiceSetup entry : fromInjector.entries.values()) {
//                    resolvedServices.computeIfAbsent(entry.key(), k -> new ServiceDelegate()).resolve(this, entry);
//                }
//            }
//        }

        // Process exports from any children
        for (var c = container.treeFirstChild; c != null; c = c.treeNextSiebling) {
            InternalServiceExtension child = c.injectionManager;

            WireletWrapper wirelets = c.wirelets;
            if (wirelets != null) {
             //   PackedWireletSelection.consumeEach(wirelets, Service1stPassWirelet.class, w -> w.process(child));
            }

            if (child != null && child.hasExports()) {
                for (BuildtimeService a : child.resolvedExports.values()) {
                    resolvedServices.computeIfAbsent(a.key, k -> new ServiceDelegate()).resolve(this, a);
                }
            }
        }

        // We now know every resolved service within the container
        // Either a local one or one exported by a child

        // Process own exports
        if (hasExports()) {
            resolve(this);
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
        LinkedHashMap<Key<?>, BuildtimeService> map = new LinkedHashMap<>();

        if (parent != null) {
            for (Entry<Key<?>, ServiceDelegate> e : parent.resolvedServices.entrySet()) {
                // we need to remove all of our exports.
                if (hasExports()) {
                    if (!resolvedExports.containsKey(e.getKey())) {
                        map.put(e.getKey(), e.getValue().getSingle());
                    }
                }
            }
        }

        WireletWrapper wirelets = container.wirelets;
        if (wirelets != null) {
            // For now we just ignore the wirelets
           // PackedWireletSelection.consumeEach(wirelets, Service2ndPassWirelet.class, w -> w.process(parent, this, map));
        }

    }

    public void resolve() {
        for (var c = container.treeFirstChild; c != null; c = c.treeNextSiebling) {
            if (c.assembly == container.assembly) {
                c.injectionManager.resolve();
            }
        }

        // Resolve local services
        prepareDependants();

        for (DependencyNode i : dependecyNodes) {
            resolve(i, this);
        }

        // Now we know every dependency that we are missing
        // I think we must plug this in somewhere

       // ios.requirementsOrCreate().checkForMissingDependencies();

        // TODO Check any contracts we might as well catch it early
    }

    public static void resolve(DependencyNode node, InternalServiceExtension sbm) {
        int providerDelta = node.operation.target.requiresBeanInstance ? 1 : 0;
        List<InternalDependency> dependencies = InternalDependency.fromOperationType(node.operation.type);// null;//factory.dependencies();
        for (int i = 0; i < dependencies.size(); i++) {
            int providerIndex = i + providerDelta;
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
                ServiceDelegate wrapper = sbm.resolvedServices.get(sd.key());
                e = wrapper == null ? null : wrapper.getSingle();
            }

            sbm.recordResolvedDependency(node, providerIndex, sd, e, false);
        }
    }

    /**
     * @param provider
     */
    public void provideOld(ProvidedService provider) {
        OperationSetup o = provider.operation;
        boolean isStatic = !o.target.requiresBeanInstance;
        if (provider.operation.target instanceof LifetimePoolAccessTarget bia) {
            addService(provider.operation.bean, provider.entry.key);
        } else {
            BuildtimeBeanMemberService sa;
            if (provider.entry.key != null) {
                if (!isStatic && provider.operation.bean.injectionManager.lifetimePoolAccessor == null) {
                    throw new BuildException("Not okay)");
                }
                InternalServiceExtension sbm = provider.operation.bean.container.injectionManager;
                DynamicAccessor accessor = provider.isConstant ? provider.operation.bean.container.lifetime.pool.reserve(Object.class) : null;
                sa = new BuildtimeBeanMemberService(provider.entry.key, provider.operation, accessor);
                sbm.addService(sa);
            } else {
                sa = null;
            }

            DependencyNode node = new DependencyNode(provider.operation, sa.accessor);
            provider.operation.bean.container.injectionManager.addConsumer(node);
        }
    }
}
