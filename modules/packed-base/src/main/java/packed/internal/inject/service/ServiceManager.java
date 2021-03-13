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
import java.util.Map.Entry;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.Service;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceLocator;
import packed.internal.component.BuildtimeRegion;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedArtifactDriver;
import packed.internal.component.PackedComponent;
import packed.internal.component.RuntimeRegion;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.container.ContainerSetup;
import packed.internal.inject.service.Requirement.FromInjectable;
import packed.internal.inject.service.build.BuildtimeService;
import packed.internal.inject.service.build.SourceInstanceBuildtimeService;
import packed.internal.inject.service.runtime.AbstractServiceLocator;
import packed.internal.inject.service.runtime.PackedInjector;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;
import packed.internal.inject.service.sandbox.Injector;
import packed.internal.inject.service.sandbox.ProvideAllFromServiceLocator;

/**
 * A service composer is responsible for managing the services for a single bundle at build time. A
 * {@link ServiceComposerTree} is responsible for managing 1 or more service composers that are directly connected and
 * part of the same build.
 */
public final class ServiceManager {

    /** The bundle this service manager is a part of. */
    private final ContainerSetup container;

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public ServiceRequirementsManager dependencies;

    /** An error manager that is lazily initialized. */
    @Nullable
    private InjectionErrorManager em;

    /** A service exporter handles everything to do with exports of services. */
    private final ServiceExportManager exports = new ServiceExportManager(this);

    /** All explicit added build entries. */
    private final ArrayList<BuildtimeService> localServices = new ArrayList<>();

    /** All injectors added via {@link ServiceExtension#provideAll(ServiceLocator)}. */
    private ArrayList<ProvideAllFromServiceLocator> provideAll;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, Wrapper> resolvedServices = new LinkedHashMap<>();

    /** Any parent this composer might have. */
    @Nullable
    private final ServiceManager parent;

    /** The composer tree this composer is a part of. */
    private final ServiceComposerTree tree;


    @Nullable
    public Predicate<? super Service> anchorFilter;

    
    /**
     * @param container
     *            the container this service manager is a part of
     */
    public ServiceManager(ContainerSetup container, @Nullable ServiceManager parent) {
        this.container = requireNonNull(container);
        this.parent = parent;
        this.tree = parent == null ? new ServiceComposerTree() : parent.tree;
    }

    public void close(BuildtimeRegion region) {
        if (parent == null) {
            tree.finish(region, container);
        }
    }

    public void addAssembly(BuildtimeService a) {
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
        return exports;
    }

    /**
     * Creates a service contract for this manager.
     * 
     * @return a service contract for this manager
     */
    public ServiceContract newServiceContract() {
        ServiceContract.Builder builder = ServiceContract.builder();

        // Any exports
        if (exports != null) {
            for (BuildtimeService n : exports) {
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
        for (BuildtimeService e : exports) {
            runtimeEntries.put(e.key(), e.toRuntimeEntry(con));
        }

        // make the entries immutable
        runtimeEntries = Map.copyOf(runtimeEntries);

        // A hack to support Injector
        PackedArtifactDriver<?> psd = (PackedArtifactDriver<?>) container.compConf.build().artifactDriver();
        if (Injector.class.isAssignableFrom(psd.artifactRawType())) {
            return new PackedInjector(runtimeEntries);
        } else {
            return new ExportedServiceLocator(comp, runtimeEntries);
        }
    }

    public void provideAll(PackedInjector injector /*, ConfigSite configSite */) {
        // We add this immediately to resolved services, as their keys are immutable.

        ProvideAllFromServiceLocator pi = new ProvideAllFromServiceLocator(this, injector);
        ArrayList<ProvideAllFromServiceLocator> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(pi);
    }

    public <T> BuildtimeService provideSource(ComponentSetup compConf, Key<T> key) {
        BuildtimeService e = new SourceInstanceBuildtimeService(this, compConf, key);
        localServices.add(e);
        return e;
    }

    // Altsaa det er taenkt tll naar vi skal f.eks. slaa Wirelets op...
    // Saa det der med at resolve. Det er ikke services...
    // men injection...

    // Vi smide alt omkring services der...

    // Lazy laver den...

    public void prepareDependants() {
        // First we take all locally defined services
        for (BuildtimeService entry : localServices) {
            resolvedServices.computeIfAbsent(entry.key(), k -> new Wrapper()).resolve(this, entry);
        }

        // Then we take any provideAll() services
        if (provideAll != null) {
            // All injectors have already had wirelets transform and filter
            for (ProvideAllFromServiceLocator fromInjector : provideAll) {
                for (BuildtimeService entry : fromInjector.entries.values()) {
                    resolvedServices.computeIfAbsent(entry.key(), k -> new Wrapper()).resolve(this, entry);
                }
            }
        }

        // Process exports from any children
        if (container.children != null) {
            for (ContainerSetup c : container.children) {
                ServiceManager child = c.getServiceManager();

                WireletPack wp = c.compConf.wirelets;
                List<Service1stPassWirelet> wirelets = wp == null ? null : wp.receiveAll(Service1stPassWirelet.class);
                if (wirelets != null) {
                    for (Service1stPassWirelet f : wirelets) {
                        f.process(child);
                    }
                }

                if (child.exports != null) {
                    for (BuildtimeService a : child.exports) {
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
        if (exports != null) {
            exports.resolve();
        }
        // Add error messages if any nodes with the same key have been added multiple times

        // Process child requirements to children
        if (container.children != null) {
            for (ContainerSetup c : container.children) {
                ServiceManager m = c.getServiceManager();
                if (m != null) {
                    m.processIncomingPipelines(this);
                }
            }
        }
    }

    private void processIncomingPipelines(@Nullable ServiceManager parent) {

        LinkedHashMap<Key<?>, BuildtimeService> map = new LinkedHashMap<>();

        if (parent != null) {
            for (Entry<Key<?>, Wrapper> e : parent.resolvedServices.entrySet()) {
                // we need to remove all of our exports.
                if (!exports().contains(e.getKey())) {
                    map.put(e.getKey(), e.getValue().getSingle());
                }
            }
        }

        System.out.println("HMMM " + map);
        WireletPack wp = container.compConf.wirelets;
        List<Service2ndPassWirelet> wirelets = wp == null ? null : wp.receiveAll(Service2ndPassWirelet.class);
        System.out.println("WWW" + wp);

        // Process wirelets
        if (wirelets != null) {

            for (Service2ndPassWirelet f : wirelets) {
                f.process(parent, this, map);
            }
        }

        // If Processere wirelets...

        ServiceRequirementsManager srm = dependencies;
        if (srm != null) {
            for (Requirement r : srm.requirements.values()) {
                BuildtimeService sa = map.get(r.key);
                if (sa != null) {
                    for (FromInjectable i : r.list) {
                        i.i.setDependencyProvider(i.dependencyIndex, sa);
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
        private final Map<Key<?>, ? extends Service> services;

        private ExportedServiceLocator(PackedComponent component, Map<Key<?>, ? extends Service> services) {
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
            return (Map) services;
        }
    }

}
