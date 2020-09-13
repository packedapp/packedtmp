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
package packed.internal.service.buildtime.dependencies;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.service.Injector;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceRegistry;
import packed.internal.component.ComponentNode;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.Region;
import packed.internal.component.RegionAssembly;
import packed.internal.component.wirelet.WireletList;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.container.ContainerAssembly;
import packed.internal.inject.Injectable;
import packed.internal.inject.sidecar.AtProvides;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.ErrorMessages;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.service.AtProvideBuildEntry;
import packed.internal.service.buildtime.service.ComponentSourceBuildEntry;
import packed.internal.service.buildtime.service.ExportedBuildEntry;
import packed.internal.service.buildtime.service.ProvideAllFromOtherInjector;
import packed.internal.service.runtime.AbstractInjector;
import packed.internal.service.runtime.PackedInjector;
import packed.internal.service.runtime.RuntimeService;
import packed.internal.util.LookupUtil;

/**
 * Since the logic for the service extension is quite complex. Especially with cross-container integration. We spread it
 * over multiple classes. With this class being the main one.
 */
public final class InjectionManager {

    /** A VarHandle that can access ServiceExtension#node. */
    private static final VarHandle VH_SERVICE_EXTENSION_NODE = LookupUtil.vhPrivateOther(MethodHandles.lookup(), ServiceExtension.class, "im",
            InjectionManager.class);

    /** Any children of the extension. */
    @Nullable
    ArrayList<InjectionManager> children;

    public final ContainerAssembly container;

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public DependencyManager dependencies;

    /** A service exporter handles everything to do with exports. */
    @Nullable
    private ExportManager exporter;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, BuildtimeService<?>> resolvedServices = new LinkedHashMap<>();

    InjectionErrorManager em;

    /**
     * Creates a new injection manager.
     * 
     * @param container
     *            the container this manager belongs to
     */
    public InjectionManager(ContainerAssembly container) {
        this.container = requireNonNull(container);
    }

    InjectionErrorManager errorManager() {
        InjectionErrorManager e = em;
        if (e == null) {
            e = em = new InjectionErrorManager();
        }
        return e;
    }

    public <T> BuildtimeService<T> provideFromSource(ComponentNodeConfiguration compConf, Key<T> key) {
        BuildtimeService<T> e = new ComponentSourceBuildEntry<>(compConf, key);
        buildEntries.add(e);
        return e;
    }

    public void provideFromAtProvides(ComponentNodeConfiguration compConf, AtProvides atProvides) {
        BuildtimeService<?> e = new AtProvideBuildEntry<>(compConf, atProvides);
        buildEntries.add(e);
        compConf.region.allInjectables.add(e.injectable());
    }

    public void buildTree(RegionAssembly resolver) {
        resolve(this, resolvedServices);

        if (em != null) {
            ErrorMessages.addDuplicateNodes(em.failingDuplicateProviders);
        }

        if (exporter != null) {
            exporter.resolve();
        }

        for (Injectable i : resolver.allInjectables) {
            i.resolve();
        }

        dependencies().analyze(resolver, this);
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
    public DependencyManager dependencies() {
        DependencyManager d = dependencies;
        if (d == null) {
            d = dependencies = new DependencyManager();
        }
        return d;
    }

    public boolean hasExports() {
        return exporter != null;
    }

    /**
     * Returns the {@link ExportManager} for this builder.
     * 
     * @return the service exporter for this builder
     */
    public ExportManager exports() {
        ExportManager e = exporter;
        if (e == null) {
            e = exporter = new ExportManager(this);
        }
        return e;
    }

    public ServiceRegistry newServiceRegistry(ComponentNode comp, Region region, WireletPack wc) {
        LinkedHashMap<Key<?>, RuntimeService<?>> runtimeEntries = new LinkedHashMap<>();
        ServiceExtensionInstantiationContext con = new ServiceExtensionInstantiationContext(region);
        for (var e : exports()) {
            runtimeEntries.put(e.key(), e.toRuntimeEntry(con));
        }
        return new PackedInjector(comp.configSite(), runtimeEntries);
    }

    public void link(InjectionManager child) {
        if (children == null) {
            children = new ArrayList<>(5);
        }
        children.add(child);
    }

    public ServiceContract newServiceContract() {
        return ServiceContract.newContract(c -> {
            if (exporter != null) {
                for (ExportedBuildEntry<?> n : exporter) {
                    c.provides(n.key());
                }
            }
            dependencies().buildContract(c);
        });
    }

    /**
     * Extracts the service node from a service extension.
     * 
     * @param extension
     *            the extension to extract from
     * @return the service node
     */
    public static InjectionManager fromExtension(ServiceExtension extension) {
        return (InjectionManager) VH_SERVICE_EXTENSION_NODE.get(extension);
    }

    /** All injectors added via {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
    private ArrayList<ProvideAllFromOtherInjector> provideAll;

    /** All explicit added build entries. */
    public final ArrayList<BuildtimeService<?>> buildEntries = new ArrayList<>();

    public void provideFromInjector(InjectionManager im, AbstractInjector injector, ConfigSite configSite, WireletList wirelets) {
        ArrayList<ProvideAllFromOtherInjector> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(new ProvideAllFromOtherInjector(im, configSite, injector, wirelets));
    }

    public LinkedHashMap<Key<?>, BuildtimeService<?>> resolve(InjectionManager im, LinkedHashMap<Key<?>, BuildtimeService<?>> resolvedServices) {

        // First process provided entries, then any entries added via provideAll
        resolve0(im, resolvedServices, buildEntries);

        if (provideAll != null) {
            // All injectors have already had wirelets transform and filter
            for (ProvideAllFromOtherInjector fromInjector : provideAll) {
                resolve0(im, resolvedServices, fromInjector.entries.values());
            }
        }

        // Run through all linked containers...
        // Apply any wirelets to exports, and take

        // Add error messages if any nodes with the same key have been added multiple times
        return resolvedServices;
    }

    private void resolve0(InjectionManager im, LinkedHashMap<Key<?>, BuildtimeService<?>> resolvedServices,
            Collection<? extends BuildtimeService<?>> buildEntries) {
        for (BuildtimeService<?> entry : buildEntries) {
            BuildtimeService<?> existing = resolvedServices.putIfAbsent(entry.key(), entry);
            if (existing != null) {
                LinkedHashSet<BuildtimeService<?>> hs = im.errorManager().failingDuplicateProviders.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>());
                hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                hs.add(entry);
            }
        }
    }
}
