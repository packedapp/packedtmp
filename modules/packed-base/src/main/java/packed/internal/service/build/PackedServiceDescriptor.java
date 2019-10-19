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
package packed.internal.service.build;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.config.ConfigSite;
import app.packed.lang.Key;
import app.packed.lang.Nullable;
import app.packed.service.ServiceDescriptor;

/** An implementation of {@link ServiceDescriptor} because {@link BuildEntry} is not immutable. */
final class PackedServiceDescriptor implements ServiceDescriptor {

    /** The configuration site of the service. */
    private final ConfigSite configSite;

    /** An optional description of the service. */
    @Nullable
    private final String description;

    /** The key of the service. */
    private final Key<?> key;

    /**
     * Creates a new descriptor.
     * 
     * @param key
     *            the key of the service
     * @param configSite
     *            the config site of the service
     * @param description
     *            the (optional) description of the service
     */
    PackedServiceDescriptor(Key<?> key, ConfigSite configSite, String description) {
        this.key = requireNonNull(key);
        this.configSite = requireNonNull(configSite);
        this.description = description;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return configSite;
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ServiceDescriptor[key=" + key + ", configSite=" + configSite + ", description=" + description + "]";
    }
}
