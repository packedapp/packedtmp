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
package packed.internal.service.build.export;

import static java.util.Objects.requireNonNull;

import app.packed.config.ConfigSite;
import app.packed.service.ServiceComponentConfiguration;
import app.packed.service.InstantiationMode;
import app.packed.service.ServiceConfiguration;
import app.packed.service.ServiceExtension;
import app.packed.util.Key;
import app.packed.util.Nullable;

/**
 * An instance of {@link ServiceConfiguration} that is returned to the user when he exports a service
 * 
 * @see ServiceExtension#export(Class)
 * @see ServiceExtension#export(Key)
 * @see ServiceExtension#export(ServiceComponentConfiguration)
 */
// Move to ExportManager when we key + check configurable has been finalized
final class ExportedServiceConfiguration<T> implements ServiceConfiguration<T> {

    /** The entry that is exported. */
    private final ExportedBuildEntry<T> entry;

    /**
     * Creates a new service configuration object.
     * 
     * @param entry
     *            the entry to export
     */
    ExportedServiceConfiguration(ExportedBuildEntry<T> entry) {
        this.entry = requireNonNull(entry);
    }

    /** {@inheritDoc} */
    @Override
    public ServiceConfiguration<T> as(@Nullable Key<? super T> key) {
        // TODO, maybe it gets disabled the minute we start analyzing exports???
        // Nah, lige saa snart, vi begynder
        entry.serviceExtension.checkExportConfigurable();
        entry.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return entry.configSite();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getDescription() {
        return entry.description;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Key<?> getKey() {
        return entry.getKey();
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return entry.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public ServiceConfiguration<T> setDescription(String description) {
        requireNonNull(description, "description is null");
        entry.serviceExtension.checkExportConfigurable();
        entry.description = description;
        return this;
    }
}