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
import java.util.Map;
import java.util.Set;

import app.packed.inject.BindingMode;
import app.packed.inject.Key;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.ConfigurationSite;
import app.packed.util.Nullable;

/**
 *
 */
public class BundleDescriptorBuilder {

    public Map<Key<?>, ServiceDescriptor> exposedServices = new HashMap<>();

    public Set<Key<?>> optionalServices = new HashSet<>();

    public Set<Key<?>> requiredServices = new HashSet<>();

    public BundleDescriptorBuilder addOptionalServices(Collection<Key<?>> keys) {
        requireNonNull(keys, "keys is null");
        optionalServices.addAll(keys);
        return this;
    }

    public BundleDescriptorBuilder addRequiredServices(Collection<Key<?>> keys) {
        requireNonNull(keys, "keys is null");
        requiredServices.addAll(keys);
        return this;
    }

    public BundleDescriptorBuilder addExposed(ServiceConfiguration<?> configuration) {
        requireNonNull(configuration, "configuration is null");
        return addExposed(new ServiceDescriptorImpl(configuration));
    }

    public BundleDescriptorBuilder addExposed(ServiceDescriptor descriptor) {
        requireNonNull(descriptor);
        if (exposedServices == null) {
            exposedServices = new HashMap<>();
        }
        if (exposedServices.putIfAbsent(descriptor.getKey(), descriptor) != null) {
            throw new IllegalStateException("A service descriptor with the same key has already been added, key = " + descriptor.getKey());
        }
        return this;
    }

    static class ServiceDescriptorImpl implements ServiceDescriptor {
        private final BindingMode bindingMode;
        private final ConfigurationSite configurationSite;
        private final @Nullable String description;
        private final Key<?> key;

        private final Set<String> tags;

        ServiceDescriptorImpl(ServiceConfiguration<?> bne) {
            this.key = bne.getKey();
            this.tags = Set.copyOf(bne.tags());
            this.bindingMode = bne.getBindingMode();
            this.configurationSite = bne.getConfigurationSite();
            this.description = bne.getDescription();
        }

        /** {@inheritDoc} */
        @Override
        public BindingMode getBindingMode() {
            return bindingMode;
        }

        /** {@inheritDoc} */
        @Override
        public ConfigurationSite getConfigurationSite() {
            return configurationSite;
        }

        /** {@inheritDoc} */
        @Override
        public @Nullable String getDescription() {
            return description;
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> getKey() {
            return key;
        }

        /** {@inheritDoc} */
        @Override
        public Set<String> tags() {
            return tags;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[Key = " + getKey().toStringSimple());
            sb.append(", bindingMode = " + getBindingMode());
            if (!tags.isEmpty()) {
                sb.append(", tags = " + tags);
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
