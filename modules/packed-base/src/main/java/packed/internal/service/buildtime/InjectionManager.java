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
package packed.internal.service.buildtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceRegistry;
import app.packed.service.ServiceMap;
import packed.internal.component.Region;
import packed.internal.component.Resolver;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.container.ContainerAssembly;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.service.buildtime.dependencies.DependencyManager;
import packed.internal.service.buildtime.export.ExportManager;
import packed.internal.service.buildtime.export.ExportedBuildEntry;
import packed.internal.service.buildtime.service.ServiceProvidingManager;
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

    private boolean hasFailed;

    /** Any parent of the node. */
    @Nullable
    InjectionManager parent;

    /** A service exporter handles everything to do with exports. */
    @Nullable
    ServiceProvidingManager provider;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, BuildtimeService<?>> resolvedEntries = new LinkedHashMap<>();

    /**
     * Creates a new injection manager.
     * 
     * @param container
     *            the container this manager belongs to
     */
    public InjectionManager(ContainerAssembly container) {
        this.container = requireNonNull(container);
    }

    public void addErrorMessage() {
        hasFailed = true;
    }

    public void buildTree(Resolver resolver) {
        if (parent == null) {
//            TreePrinter.print(this, n -> n.children, "", n -> n.context.containerPath().toString());
        }

        HashMap<Key<?>, BuildtimeService<?>> resolvedServices = provider().resolve();
        resolvedServices.values().forEach(e -> resolvedEntries.put(requireNonNull(e.key()), e));

        if (exporter != null) {
            exporter.resolve();
        }

        if (hasFailed) {
            return;
        }

        for (Injectable i : resolver.allInjectables) {
            i.resolve(resolver);
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

    public ServiceRegistry newServiceRegistry(Region region, WireletPack wc) {
        LinkedHashMap<Key<?>, RuntimeService<?>> runtimeEntries = new LinkedHashMap<>();
        ServiceExtensionInstantiationContext con = new ServiceExtensionInstantiationContext(region);
        for (var e : exports()) {
            runtimeEntries.put(e.key, e.toRuntimeEntry(con));
        }
        return new PackedInjector(container.component.configSite(), runtimeEntries);
    }

    public void link(InjectionManager child) {
        child.parent = this;
        if (children == null) {
            children = new ArrayList<>(5);
        }
        children.add(child);
    }

    @Nullable
    public ServiceMap newExportedServiceSet() {
        return exports().exports();
    }

    public ServiceContract newServiceContract() {
        // Er ikke sikker paa alt er med her...
        // Vi kalder vist ikke ordenligt ind paa DependencyManager.register...
        return ServiceContract.newContract(c -> {
            if (exporter != null) {
                for (ExportedBuildEntry<?> n : exporter) {
                    c.provides(n.key());
                }
            }
            dependencies().buildContract(c);
        });
    }

    public ServiceProvidingManager provider() {
        ServiceProvidingManager p = provider;
        if (p == null) {
            p = provider = new ServiceProvidingManager(this);
        }
        return p;
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
}
