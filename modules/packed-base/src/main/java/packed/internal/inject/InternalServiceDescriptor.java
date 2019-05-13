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
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;

/** The default implementation of {@link ServiceDescriptor}. */
public class InternalServiceDescriptor implements ServiceDescriptor {

    /** The configuration site of the service. */
    private final ConfigSite configurationSite;

    /** An optional description of the service. */
    @Nullable
    private final String description;

    /** The key of the service. */
    private final Key<?> key;

    // private final Set<String> tags;

    /**
     * @param configurationSite
     * @param description
     * @param key
     */
    public InternalServiceDescriptor(Key<?> key, ConfigSite configurationSite, String description /* Set<String> tags */) {
        this.configurationSite = requireNonNull(configurationSite);
        this.description = description;
        this.key = requireNonNull(key);
        // this.tags = requireNonNull(tags);
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
        return "InternalServiceDescriptor [configurationSite=" + configurationSite + ", description=" + description + ", key=" + key /* + ", tags=" + tags */
                + "]";
    }
}
