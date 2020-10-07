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
package packed.internal.inject.service;

import static java.util.Objects.requireNonNull;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.ServiceExtension;
import app.packed.service.ExportedServiceConfiguration;
import packed.internal.inject.service.assembly.ExportedServiceAssembly;

/**
 * An instance of {@link ExportedServiceConfiguration} that is returned to the user when he exports a service
 * 
 * @see ServiceExtension#export(Class)
 * @see ServiceExtension#export(Key)
 */
// Move to ExportManager when we key + check configurable has been finalized
public final class PackedExportedServiceConfiguration<T> implements ExportedServiceConfiguration<T> {

    /** The entry that is exported. */
    private final ExportedServiceAssembly<T> entry;

    /**
     * Creates a new service configuration object.
     * 
     * @param entry
     *            the entry to export
     */
    public PackedExportedServiceConfiguration(ExportedServiceAssembly<T> entry) {
        this.entry = requireNonNull(entry);
    }

    /** {@inheritDoc} */
    @Override
    public ExportedServiceConfiguration<T> as(@Nullable Key<? super T> key) {
        // TODO, maybe it gets disabled the minute we start analyzing exports???
        // Nah, lige saa snart, vi begynder
        entry.sm.checkExportConfigurable();
        entry.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Key<?> key() {
        return entry.key();
    }
}