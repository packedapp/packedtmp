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
package packed.internal.inject.util;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.config.ConfigSite;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import packed.internal.inject.build.BuildEntry;

/**
 *
 */
public final class BuildEntryServiceDescriptionWrapper implements ServiceDescriptor {

    /** The configuration we read through to. */
    private final BuildEntry<?> configuration;

    /**
     * Creates a new wrapper
     * 
     * @param configuration
     *            the configuration to wrap
     */
    public BuildEntryServiceDescriptionWrapper(BuildEntry<?> configuration) {
        this.configuration = requireNonNull(configuration, "configuration is null");
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return configuration.configSite();
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