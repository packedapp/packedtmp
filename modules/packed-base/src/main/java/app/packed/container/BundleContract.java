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
package app.packed.container;

import app.packed.inject.InjectorContract;
import app.packed.lifecycle.LifecycleBundleContractPoints;

/**
 * The contract of a bundle.
 * <p>
 * The main difference to {@link BundleDescriptor} is that class does not include any implementation details. For
 * example, BundleDescriptor lists all annotated methods.
 * 
 * 
 * Lists all Components also?????
 */

public class BundleContract {

    /** A service contract object. */
    private final InjectorContract services;

    private final LifecycleBundleContractPoints startingPoints = null;

    private final LifecycleBundleContractPoints stoppingPoints = null;

    private BundleContract(BundleContract.Builder builder) {
        InjectorContract.Builder s = builder.services;
        this.services = s == null ? InjectorContract.EMPTY : s.build();
    }

    /**
     * Return a service contract object representing the services the bundle exports. As well as any required or optional
     * services.
     * 
     * @return the service contract for the bundle
     */
    public final InjectorContract services() {
        return services;
    }

    // Er detn bare tom for en injector bundle???? Det er den vel
    public final LifecycleBundleContractPoints startingPoints() {
        return startingPoints;
    }

    public final LifecycleBundleContractPoints stoppingPoints() {
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
        return BundleDescriptor.of(bundle).contract();
    }

    public void print() {

    }

    // Needed if we want to allow extensions..
    // Which I think we want after AnyBundle
    public static class Builder {

        /** A service contract builder object. */
        private InjectorContract.Builder services;

        /**
         * Builds and returns a new contract
         * 
         * @return the new contract
         */
        public BundleContract build() {
            return new BundleContract(this);
        }

        public InjectorContract.Builder services() {
            InjectorContract.Builder s = services;
            return s == null ? services = InjectorContract.builder() : s;
        }
    }

}
