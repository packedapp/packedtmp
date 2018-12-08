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
package packed.internal.bundle;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import app.packed.bundle.BundleDescriptor;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;

/** A builder object for {@link BundleDescriptor}. */
public class BundleDescriptorBuilder {

    public String description;

    public final Services services = new Services();

    public static class Services {

        public final HashMap<Key<?>, ServiceDescriptor> exposed = new HashMap<>();

        public final HashSet<Key<?>> optional = new HashSet<>();

        public final HashSet<Key<?>> required = new HashSet<>();

        public Services addExposed(ServiceConfiguration<?> configuration) {
            requireNonNull(configuration, "configuration is null");
            return addExposed(ServiceDescriptor.ofCopy(configuration));
        }

        public Services addExposed(ServiceDescriptor descriptor) {
            requireNonNull(descriptor);
            if (exposed.putIfAbsent(descriptor.getKey(), descriptor) != null) {
                throw new IllegalStateException("A service descriptor with the same key has already been added, key = " + descriptor.getKey());
            }
            return this;
        }

        public Services addOptionalServices(Collection<Key<?>> keys) {
            requireNonNull(keys, "keys is null");
            optional.addAll(keys);
            return this;
        }

        public Services addRequiredServices(Collection<Key<?>> keys) {
            requireNonNull(keys, "keys is null");
            required.addAll(keys);
            return this;
        }
    }
}
