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

import java.util.List;

import app.packed.config.ConfigSite;
import app.packed.service.InstantiationMode;
import app.packed.service.ServiceComponentConfiguration;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceRequest;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.service.ServiceEntry;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.ServiceExtensionNode;
import packed.internal.service.run.RuntimeEntry;
import packed.internal.service.run.DelegatingRuntimeEntry;

/**
 * A build entry representing an exported service. Entries at runtime has never any reference to how (or if) they where
 * exported.
 */
public final class ExportedBuildEntry<T> extends BuildEntry<T> {

    /** The actual entry that is exported. Is initially null for keyed exports, until it is resolved. */
    @Nullable
    ServiceEntry<T> exportedEntry;

    /** The key under which to export the entry, is null for entry exports. */
    @Nullable
    final Key<?> keyToExport;

    /**
     * Exports an entry via its key.
     * 
     * @param builder
     *            the injector configuration this node is being added to
     * @param configSite
     *            the configuration site of the exposure
     * @see ServiceExtension#export(Class)
     * @see ServiceExtension#export(Key)
     */
    ExportedBuildEntry(ServiceExtensionNode builder, Key<T> key, ConfigSite configSite) {
        super(builder, configSite, List.of());
        this.keyToExport = requireNonNull(key);
        as(key);
    }

    /**
     * Exports an existing entry.
     * 
     * @param entryToExport
     *            the entry to export
     * @param configSite
     *            the config site of the export
     * @see ServiceExtension#export(ServiceComponentConfiguration)
     * @see ServiceExtension#exportAll()
     */
    @SuppressWarnings("unchecked")
    ExportedBuildEntry(ServiceExtensionNode builder, ServiceEntry<T> entryToExport, ConfigSite configSite) {
        super(builder, configSite, List.of());
        this.exportedEntry = entryToExport;
        this.keyToExport = null;
        // Export of export, of export????
        // Hvad hvis en eller anden aendrer en key midt i chainen.
        // Slaar det igennem i hele vejen ned.
        this.key = (Key<T>) entryToExport.key();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public BuildEntry<?> declaringEntry() {
        return (exportedEntry instanceof BuildEntry) ? ((BuildEntry<?>) exportedEntry).declaringEntry() : null;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ServiceRequest request) {
        return exportedEntry.getInstance(request);
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return exportedEntry.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsServiceRequest() {
        return exportedEntry.needsServiceRequest();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasUnresolvedDependencies() {
        return exportedEntry.hasUnresolvedDependencies();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeEntry<T> newRuntimeNode() {
        return new DelegatingRuntimeEntry<>(this, exportedEntry);
    }
}
