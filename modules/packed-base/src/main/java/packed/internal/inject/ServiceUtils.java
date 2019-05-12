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

import java.util.Optional;

import app.packed.config.ConfigSite;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;

/**
 *
 */
public class ServiceUtils {

    public static ServiceDescriptor copyOf(ServiceConfiguration<?> configuration) {
        // Move to internal
        return new CopyOfConfiguration(configuration);
    }

    /**
     * Returns an unmodifiable view of the specified service configuration. Operations on the returned descriptor "read
     * through" to the specified configuration.
     * 
     * @param configuration
     *            the service configuration to create an adaptor for
     * @return the read through descriptor
     */
    public static ServiceDescriptor wrapperOf(ServiceConfiguration<?> configuration) {
        // Move to internal
        return new ServiceConfigurationWrapper(configuration);
    }

    static class CopyOfConfiguration implements ServiceDescriptor {

        private final ConfigSite configurationSite;

        private final @Nullable String description;

        private final Key<?> key;

        // private final Set<String> tags;

        CopyOfConfiguration(ServiceConfiguration<?> bne) {
            this.key = bne.getKey();
            // this.tags = Set.copyOf(bne.tags());
            this.configurationSite = bne.configurationSite();
            this.description = bne.getDescription();
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite configurationSite() {
            return configurationSite;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> description() {
            return Optional.ofNullable(description);
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> key() {
            return key;
        }
        //
        // /** {@inheritDoc} */
        // @Override
        // public Set<String> tags() {
        // return tags;
        // }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[Key = " + key().toStringSimple());
            // if (!tags.isEmpty()) {
            // sb.append(", tags = " + tags);
            // }
            sb.append("]");
            return sb.toString();
        }
    }

    /** A descriptor that wraps a service configuration. */
    static class ServiceConfigurationWrapper implements ServiceDescriptor {

        /** The configuration we read through to. */
        private final ServiceConfiguration<?> configuration;

        /**
         * Creates a new wrapper
         * 
         * @param configuration
         *            the configuration to wrap
         */
        ServiceConfigurationWrapper(ServiceConfiguration<?> configuration) {
            this.configuration = requireNonNull(configuration, "configuration is null");
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite configurationSite() {
            return configuration.configurationSite();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> description() {
            return Optional.ofNullable(configuration.getDescription());
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> key() {
            return configuration.getKey();
        }
        //
        // /** {@inheritDoc} */
        // @Override
        // public Set<String> tags() {
        // return Collections.unmodifiableSet(configuration.tags());
        // }
    }
}
