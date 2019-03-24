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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import app.packed.bundle.BundleDescriptor.LifecyclePoints;
import app.packed.inject.ServiceContract;

/**
 * The contract of a bundle. Unlike {@link BundleDescriptor} this class does not include any implementation details.
 */

public class BundleContract {

    /** A Services object. */
    private final ServiceContract services = null;

    private final LifecyclePoints startingPoints = null;

    private final LifecyclePoints stoppingPoints = null;

    /**
     * Return a service contract object representing the services the bundle exports. As well as any required or optional
     * services.
     * 
     * @return the service contract for the bundle
     */
    public final ServiceContract services() {
        return services;
    }

    // Er detn bare tom for en injector bundle???? Det er den vel
    public final LifecyclePoints startingPoints() {
        return startingPoints;
    }

    public final LifecyclePoints stoppingPoints() {
        return stoppingPoints;
    }

    /**
     * Returns a descriptor for the specified bundle.
     *
     * @param bundle
     *            the bundle to return a descriptor for
     * @return a descriptor for the specified bundle
     */
    public static BundleContract of(Bundle bundle) {
        requireNonNull(bundle, "bundle is null");
        throw new UnsupportedOperationException();
        // return InternalBundleDescriptor.of(bundle).build();
    }

    // Needed if we want to allow extensions..
    public static class Builder {

    }
}
