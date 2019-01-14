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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Set;

import app.packed.bundle.BundleDescriptor;
import app.packed.util.Key;

/**
 *
 */
public class InjectorBundleDescriptor {

    /** An object representing the services the bundle exposes. As well as any required or optional services. */
    // Prototype vs singleton is actually also part of the API. Because changing a instantiation mode from
    // singleton to prototype can result in a dependent service to fail.
    public static final class Services {

        /** An immutable map of all the services the bundle exposes. */
        private final Map<Key<?>, ServiceDescriptor> exposedServices;

        /** A set of all optional service keys. */
        private final Set<Key<?>> optionalServices;

        /** A set of all required service keys. */
        private final Set<Key<?>> requiredServices;

        /**
         * Creates a new Services object
         * 
         * @param builder
         *            the builder object
         */
        public Services(BundleDescriptor.Builder builder) {
            this.exposedServices = Map.copyOf(builder.serviceExports);
            this.optionalServices = requireNonNull(builder.servicesOptional);
            this.requiredServices = requireNonNull(builder.serviceRequired);
        }

        /**
         * Returns an immutable map of all the services the bundle exposes.
         *
         * @return an immutable map of all the services the bundle exposes
         */
        public Map<Key<?>, ServiceDescriptor> exports() {
            return exposedServices;
        }

        /**
         * if all exposed services in the previous services are also exposed in this services. And if all required services in
         * this are also required services in the previous.
         * 
         * @param previous
         * @return whether or not the specified service are back
         */
        public boolean isBackwardsCompatibleWith(Services previous) {
            requireNonNull(previous, "previous is null");
            if (!previous.requiredServices.containsAll(requiredServices)) {
                return false;
            }
            if (!exposedServices.keySet().containsAll(previous.exposedServices.keySet())) {
                return false;
            }
            return true;
        }

        /**
         * Returns an immutable set of all the keys for which a service that <b>must</b> be made available to the entity.
         * 
         * @return an immutable set of all keys that <b>must</b> be made available to the entity
         */
        // rename to requirements.
        public Set<Key<?>> requires() {
            return requiredServices;
        }

        /**
         * Returns an immutable set of all service keys that <b>can, but do have to</b> be made available to the entity.
         * 
         * @return an immutable set of all service keys that <b>can, but do have to</b> be made available to the entity
         */
        public Set<Key<?>> requiresOptionally() {
            return optionalServices;
        }
    }

    // Det gode ved at have en SPEC_VERSION, er at man kan specificere man vil bruge.
    // Og dermed kun importere praecis de interfaces den definere...
    // Deploy(someSpec?) ved ikke lige med API'en /
    // FooBarBundle.API$2_2
    // FooBarBundle.API$2_3-SNAPSHOT hmmm, saa forsvinder den jo naar man releaser den???
    // Maaske hellere have den markeret med @Preview :D
    /// Bundlen, kan maaske endda supportere flere versioner??Som i flere versioner??

    // The union of exposedServices, optionalService and requiredService must be empty
}
