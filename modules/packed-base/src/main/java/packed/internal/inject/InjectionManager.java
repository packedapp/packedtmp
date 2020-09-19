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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;

import app.packed.base.Nullable;
import app.packed.service.ServiceExtension;
import packed.internal.component.RegionAssembly;
import packed.internal.container.ContainerAssembly;
import packed.internal.inject.dependency.Injectable;
import packed.internal.inject.service.ServiceManager;

/**
 * Since the logic for the service extension is quite complex. Especially with cross-container integration. We spread it
 * over multiple classes. With this class being the main one.
 */
public final class InjectionManager {

    /** All injectables that needs to be resolved. */
    final ArrayList<Injectable> allInjectables = new ArrayList<>();

    /** The container this injection manager belongs to. */
    public final ContainerAssembly container;

    /** An error manager that is lazily initialized. */
    @Nullable
    private InjectionErrorManager em;

    /** A service manager that handles everything to do with services, is lazily initialized. */
    @Nullable
    private ServiceManager services;

    /**
     * Creates a new injection manager.
     * 
     * @param container
     *            the container this manager belongs to
     */
    public InjectionManager(ContainerAssembly container) {
        this.container = requireNonNull(container);
    }

    /**
     * Adds the specified injectable to list of injectables that needs to be resolved.
     * 
     * @param injectable
     *            the injectable to add
     */
    public void addInjectable(Injectable injectable) {
        allInjectables.add(requireNonNull(injectable));
    }

    public void build(RegionAssembly region) {
        if (services != null) {
            services.resolve();
        }

        if (em != null) {
            InjectionErrorManagerMessages.addDuplicateNodes(em.failingDuplicateProviders);
        }

        if (services != null) {
            services.resolveExports();
        }

        for (Injectable i : allInjectables) {
            i.resolve();
        }

        if (services != null) {
            services.dependencies().checkForMissingDependencies(this);
        }
        PostProcesser.dependencyCyclesDetect(region, this);

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

    @Nullable
    public ServiceManager getServiceManager() {
        return services;
    }

    /**
     * Returns the {@link ServiceManager}, creating it lazily if it does not already exist.
     * 
     * @param registerServiceExtension
     *            whether or not we should register the {@link ServiceExtension}. Should always be true, unless the service
     *            manager is installed from the ServiceExtension itself
     * 
     * @return the service exporter for this builder
     */
    public ServiceManager services(boolean registerServiceExtension) {
        ServiceManager e = services;
        if (e == null) {
            e = services = new ServiceManager(this);
            if (registerServiceExtension) {
                container.useExtension(ServiceExtension.class);
            }
        }
        return e;
    }
}
