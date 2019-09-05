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
 * The contract of a base bundle.
 * <p>
 * The main difference to {@link BundleDescriptor} is that class does not include any implementation details. For
 * example, BundleDescriptor lists all annotated methods.
 * 
 * 
 * Lists all Components also?????
 */
// Hmmmmmmmmmmmmm
/// Ideen er vel lidt at lave det type sikkert....
/// Men hvad hvis den ikke har en given ting
public class BaseBundleContract {

    /** A service contract object. */
    private final InjectorContract services;

    private final LifecycleBundleContractPoints startingPoints = null;

    private final LifecycleBundleContractPoints stoppingPoints = null;

    private BaseBundleContract(BaseBundleContract.Builder builder) {
        this.services = builder.services;
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
     * Returns a contract for the specified bundle.
     *
     * @param bundle
     *            the bundle to return a descriptor for
     * @return a descriptor for the specified bundle
     */
    public static BaseBundleContract of(BaseBundle bundle) {
        return BundleDescriptor.of(bundle).contract();
    }

    public void print() {

    }

    // Needed if we want to allow extensions..
    // Which I think we want after AnyBundle
    public static class Builder {

        /** A service contract builder object. */
        public InjectorContract services;

        /**
         * Builds and returns a new contract
         * 
         * @return the new contract
         */
        public BaseBundleContract build() {
            return new BaseBundleContract(this);
        }
    }

}
