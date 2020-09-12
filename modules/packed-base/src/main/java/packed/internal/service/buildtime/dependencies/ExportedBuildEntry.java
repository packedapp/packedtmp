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
package packed.internal.service.buildtime.dependencies;

import static java.util.Objects.requireNonNull;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.service.ServiceExtension;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.InjectionManager;
import packed.internal.service.runtime.DelegatingInjectorEntry;
import packed.internal.service.runtime.RuntimeService;

/**
 * A build entry representing an exported service. Entries at runtime has never any reference to how (or if) they where
 * exported.
 */
public final class ExportedBuildEntry<T> extends BuildtimeService<T> {

    /** The actual entry that is exported. Is initially null for keyed exports, until it is resolved. */
    @Nullable
    BuildtimeService<T> exportedEntry;

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
    ExportedBuildEntry(InjectionManager builder, Key<T> key, ConfigSite configSite) {
        super(builder, configSite);
        this.keyToExport = requireNonNull(key);
        this.key = requireNonNull(key);
    }

    /**
     * Exports an existing entry.
     * 
     * @param entryToExport
     *            the entry to export
     * @param configSite
     *            the config site of the export
     * @see ServiceExtension#exportAll()
     */
    ExportedBuildEntry(InjectionManager builder, BuildtimeService<T> entryToExport, ConfigSite configSite) {
        super(builder, configSite);
        this.exportedEntry = entryToExport;
        this.keyToExport = null;
        // Export of export, of export????
        // Hvad hvis en eller anden aendrer en key midt i chainen.
        // Slaar det igennem i hele vejen ned.
        this.key = entryToExport.key();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        return new DelegatingInjectorEntry<>(this, exportedEntry.toRuntimeEntry(context));
    }
}
