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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import app.packed.base.InvalidDeclarationException;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceLocator;
import app.packed.service.Injector;
import packed.internal.component.ComponentNode;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.RuntimeRegion;
import packed.internal.component.wirelet.WireletList;
import packed.internal.inject.InjectionManager;
import packed.internal.inject.service.assembly.AtProvideServiceAssembly;
import packed.internal.inject.service.assembly.ComponentSourceServiceAssembly;
import packed.internal.inject.service.assembly.ExportedServiceAssembly;
import packed.internal.inject.service.assembly.ProvideAllFromOtherInjector;
import packed.internal.inject.service.assembly.ServiceAssembly;
import packed.internal.inject.service.runtime.AbstractInjector;
import packed.internal.inject.service.runtime.PackedInjector;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;
import packed.internal.inject.sidecar.AtProvides;
import packed.internal.inject.sidecar.AtProvidesHook;
import packed.internal.methodhandle.LookupUtil;

/**
 *
 */
public final class ServiceBuildManager {

    /** A VarHandle that can access ServiceExtension#sm. */
    private static final VarHandle VH_SERVICE_EXTENSION_NODE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), ServiceExtension.class, "sbm",
            ServiceBuildManager.class);

    /** All explicit added build entries. */
    private final ArrayList<ServiceAssembly<?>> assemblies = new ArrayList<>();

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public ServiceDependencyManager dependencies;

    /** A service exporter handles everything to do with exports of services. */
    @Nullable
    private ServiceExportManager exporter;

    /** The injection manager this service manager is a part of. */
    public final InjectionManager im;

    /** All injectors added via {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
    private ArrayList<ProvideAllFromOtherInjector> provideAll;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, ServiceAssembly<?>> resolvedServices = new LinkedHashMap<>();

    /**
     * @param im
     */
    public ServiceBuildManager(InjectionManager im) {
        this.im = requireNonNull(im);
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
    public ServiceDependencyManager dependencies() {
        ServiceDependencyManager d = dependencies;
        if (d == null) {
            d = dependencies = new ServiceDependencyManager();
        }
        return d;
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

    public boolean hasExports() {
        return exporter != null;
    }

    public ServiceContract newServiceContract() {
        return ServiceContract.newContract(c -> {
            if (hasExports()) {
                for (ExportedServiceAssembly<?> n : exports()) {
                    c.provides(n.key());
                }
            }
            if (dependencies != null) {
                if (dependencies.requiredOptionally != null) {
                    dependencies.requiredOptionally.forEach(k -> c.optional(k));
                }
                if (dependencies.required != null) {
                    dependencies.required.forEach(k -> c.requires(k));
                }
            }
        });
    }

    public ServiceLocator newServiceRegistry(ComponentNode comp, RuntimeRegion region) {
        LinkedHashMap<Key<?>, RuntimeService<?>> runtimeEntries = new LinkedHashMap<>();
        ServiceInstantiationContext con = new ServiceInstantiationContext(region);
        for (var e : exports()) {
            runtimeEntries.put(e.key(), e.toRuntimeEntry(con));
        }
        return new PackedInjector(comp.configSite(), runtimeEntries);
    }

    public void provideAtProvides(AtProvidesHook hook, ComponentNodeConfiguration compConf) {
        if (hook.hasInstanceMembers && compConf.source.regionIndex == -1) {
            throw new InvalidDeclarationException("Not okay)");
        }

        // Add each @Provide as children of the parent node
        for (AtProvides atProvides : hook.members) {
            ServiceAssembly<?> e = new AtProvideServiceAssembly<>(this, compConf, atProvides);
            assemblies.add(e);
            im.addInjectable(e.getInjectable());
        }
    }

    public void provideFromInjector(AbstractInjector injector, ConfigSite configSite, WireletList wirelets) {
        ProvideAllFromOtherInjector pi = new ProvideAllFromOtherInjector(this, configSite, injector, wirelets);
        ArrayList<ProvideAllFromOtherInjector> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(pi);
    }

    public <T> ServiceAssembly<T> provideSource(ComponentNodeConfiguration compConf, Key<T> key) {
        ServiceAssembly<T> e = new ComponentSourceServiceAssembly<>(this, compConf, key);
        assemblies.add(e);
        return e;
    }

    public LinkedHashMap<Key<?>, ServiceAssembly<?>> resolve() {

        // First process provided entries, then any entries added via provideAll
        resolve0(im, resolvedServices, assemblies);

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

    private void resolve0(InjectionManager im, LinkedHashMap<Key<?>, ServiceAssembly<?>> resolvedServices,
            Collection<? extends ServiceAssembly<?>> buildEntries) {
        for (ServiceAssembly<?> entry : buildEntries) {
            ServiceAssembly<?> existing = resolvedServices.putIfAbsent(entry.key(), entry);
            if (existing != null) {
                LinkedHashSet<ServiceAssembly<?>> hs = im.errorManager().failingDuplicateProviders.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>());
                hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                hs.add(entry);
            }
        }
    }

    // Vi smide alt omkring services der...

    // Lazy laver den...

    // Altsaa det er taenkt tll naar vi skal f.eks. slaa Wirelets op...
    // Saa det der med at resolve. Det er ikke services...
    // men injection...
    public void resolveExports() {
        if (exporter != null) {
            exporter.resolve();
        }
    }
    // En InjectionManager kan have en service manager...

    /**
     * Extracts the service node from a service extension.
     * 
     * @param extension
     *            the extension to extract from
     * @return the service node
     */
    public static ServiceBuildManager fromExtension(ServiceExtension extension) {
        return (ServiceBuildManager) VH_SERVICE_EXTENSION_NODE.get(extension);
    }
}