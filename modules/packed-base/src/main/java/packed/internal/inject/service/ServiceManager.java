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

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.service.Injector;
import app.packed.service.ServiceExtension;
import packed.internal.component.wirelet.WireletList;
import packed.internal.inject.InjectionManager;
import packed.internal.inject.service.assembly.ProvideAllFromOtherInjector;
import packed.internal.inject.service.assembly.ServiceAssembly;
import packed.internal.inject.service.runtime.AbstractInjector;
import packed.internal.util.LookupUtil;

/**
 *
 */
public class ServiceManager {

    /** A VarHandle that can access ServiceExtension#im. */
    private static final VarHandle VH_SERVICE_EXTENSION_NODE = LookupUtil.vhPrivateOther(MethodHandles.lookup(), ServiceExtension.class, "sm",
            ServiceManager.class);

    /** A service exporter handles everything to do with exports of services. */
    @Nullable
    private ServiceExportManager exporter;

    public final InjectionManager im;

    /** All injectors added via {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
    private ArrayList<ProvideAllFromOtherInjector> provideAll;

    /**
     * @param injectionManager
     */
    public ServiceManager(InjectionManager injectionManager) {
        this.im = requireNonNull(injectionManager);
    }

    public boolean hasExports() {
        return exporter != null;
    }

    public void provideFromInjector(AbstractInjector injector, ConfigSite configSite, WireletList wirelets) {
        ProvideAllFromOtherInjector pi = new ProvideAllFromOtherInjector(im, configSite, injector, wirelets);
        ArrayList<ProvideAllFromOtherInjector> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(pi);
    }

    public LinkedHashMap<Key<?>, ServiceAssembly<?>> resolve(LinkedHashMap<Key<?>, ServiceAssembly<?>> resolvedServices) {

        // First process provided entries, then any entries added via provideAll
        resolve0(im, resolvedServices, im.buildEntries);

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

    private static void resolve0(InjectionManager im, LinkedHashMap<Key<?>, ServiceAssembly<?>> resolvedServices,
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

    public void checkExportConfigurable() {
        // when processing wirelets
        // We should make sure some stuff is no longer configurable...
    }

    public void resolveExports() {
        if (exporter != null) {
            exporter.resolve();
        }
    }
    // En InjectionManager kan have en service manager...

    // Vi smide alt omkring services der...

    // Lazy laver den...

    // Altsaa det er taenkt tll naar vi skal f.eks. slaa Wirelets op...
    // Saa det der med at resolve. Det er ikke services...
    // men injection...

    /**
     * Extracts the service node from a service extension.
     * 
     * @param extension
     *            the extension to extract from
     * @return the service node
     */
    public static ServiceManager fromExtension(ServiceExtension extension) {
        return (ServiceManager) VH_SERVICE_EXTENSION_NODE.get(extension);
    }
}
