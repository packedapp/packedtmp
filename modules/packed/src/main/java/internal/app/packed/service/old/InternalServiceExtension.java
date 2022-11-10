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

    public final ContainerServiceBinder ios = new ContainerServiceBinder(this);

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
        if (ios.hasExports()) {
            for (BuildtimeService export : ios.exports()) {
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

            if (child != null && child.ios.hasExports()) {
                for (BuildtimeService a : child.ios.exports()) {
                    resolvedServices.computeIfAbsent(a.key, k -> new ServiceDelegate()).resolve(this, a);
                }
            }
        }

        // We now know every resolved service within the container
        // Either a local one or one exported by a child

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
        LinkedHashMap<Key<?>, BuildtimeService> map = new LinkedHashMap<>();

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
           // PackedWireletSelection.consumeEach(wirelets, Service2ndPassWirelet.class, w -> w.process(parent, this, map));
        }

        // If Processere wirelets...

//        ServiceManagerRequirementsSetup srm = ios.requirements();
//        if (srm != null) {
//            for (Requirement r : srm.requirements.values()) {
//                ServiceSetup sa = map.get(r.key);
//                if (sa != null) {
//                    for (FromInjectable i : r.list) {
//                        i.i().setProducer(i.dependencyIndex(), sa);
//                    }
//                }
//            }
//        }
    }

//    public void provideAll(AbstractServiceLocator locator) {
//        // We add this immediately to resolved services, as their keys are immutable.
//
//        ProvideAllFromServiceLocator pi = new ProvideAllFromServiceLocator(this, locator);
//        ArrayList<ProvideAllFromServiceLocator> p = provideAll;
//        if (provideAll == null) {
//            p = provideAll = new ArrayList<>(1);
//        }
//        p.add(pi);
//    }

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

        ios.requirementsOrCreate().checkForMissingDependencies();

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

            sbm.ios.requirementsOrCreate().recordResolvedDependency(node, providerIndex, sd, e, false);
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
