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
import app.packed.inject.ServiceExtension;
import packed.internal.component.RegionAssembly;
import packed.internal.container.ContainerAssembly;
import packed.internal.inject.dependency.Injectable;
import packed.internal.inject.service.ServiceBuildManager;

/**
 * Since the logic for the service extension is quite complex. Especially with cross-container integration. We spread it
 * over multiple classes. With this class being the main one.
 */
public final class InjectionManager {

    /** All injectables that needs to be resolved. */
    final ArrayList<Injectable> injectables = new ArrayList<>();

    /** The container this injection manager belongs to. */
    public final ContainerAssembly container;

    /** An error manager that is lazily initialized. */
    @Nullable
    public InjectionErrorManager em;

    /** A service manager that handles everything to do with services, is lazily initialized. */
    @Nullable
    private ServiceBuildManager sbm;

    /**
     * Creates a new injection manager.
     * 
     * @param container
     *            the container this manager belongs to
     */
    public InjectionManager(ContainerAssembly container) {
        this.container = requireNonNull(container);
    }

    @Nullable
    public InjectionManager parent() {
        return container.parent == null ? null : container.parent.im;
    }

    /**
     * Adds the specified injectable to list of injectables that needs to be resolved.
     * 
     * @param injectable
     *            the injectable to add
     */
    public void addInjectable(Injectable injectable) {
        injectables.add(requireNonNull(injectable));

        // Bliver noedt til at lave noget sidecar preresolve her.
        // I virkeligheden vil vi bare gerne checke at om man
        // har ting der ikke kan resolves via contexts
        if (sbm == null && !injectable.dependencies.isEmpty()) {
            container.useExtension(ServiceExtension.class);
        }
    }

    public void build(RegionAssembly region) {
        InjectionManager parent = container.parent == null ? null : container.parent.im;
        boolean isIslandChild = sbm != null && parent != null && parent.sbm != null;

        // Resolve local services
        // As well as services from child containers
        if (sbm != null) {
            sbm.resolveLocal();
        }

        for (Injectable i : injectables) {
            i.resolve(this);
        }

        // Now we know every dependency that we are missing
        // I think we must plug this in somewhere

        if (sbm != null) {
            sbm.dependencies().checkForMissingDependencies(this);
        }

        // TODO Check any contracts we might as well catch it early

        // If we form for a service island and is root of the island
        // Do checks here
        if (!isIslandChild) {
            ServiceIsland.finish(region, this);
        }
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
    public ServiceBuildManager getServiceManager() {
        return sbm;
    }

    /**
     * Returns the {@link ServiceBuildManager}, creating it lazily if it does not already exist.
     * 
     * @param registerServiceExtension
     *            whether or not we should register the {@link ServiceExtension}. Should always be true, unless the service
     *            manager is installed from the ServiceExtension itself
     * 
     * @return the service exporter for this builder
     */
    public ServiceBuildManager services(boolean registerServiceExtension) {
        ServiceBuildManager e = sbm;
        if (e == null) {
            e = sbm = new ServiceBuildManager(this);
            if (registerServiceExtension) {
                container.useExtension(ServiceExtension.class);
            }
        }
        return e;
    }
}
