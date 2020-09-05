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
package packed.internal.service.buildtime.export;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.service.ServiceExtension;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.runtime.DelegatingInjectorEntry;
import packed.internal.service.runtime.RuntimeEntry;

/**
 * A build entry representing an exported service. Entries at runtime has never any reference to how (or if) they where
 * exported.
 */
public final class ExportedBuildEntry<T> extends BuildEntry<T> {

    /** The actual entry that is exported. Is initially null for keyed exports, until it is resolved. */
    @Nullable
    BuildEntry<T> exportedEntry;

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
    ExportedBuildEntry(ServiceExtensionNode builder, BuildEntry<T> entryToExport, ConfigSite configSite) {
        super(builder, configSite, List.of());
        this.exportedEntry = entryToExport;
        this.keyToExport = null;
        // Export of export, of export????
        // Hvad hvis en eller anden aendrer en key midt i chainen.
        // Slaar det igennem i hele vejen ned.
        this.key = entryToExport.key();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public BuildEntry<?> declaringEntry() {
        return exportedEntry.declaringEntry();
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode instantiationMode() {
        return exportedEntry.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return exportedEntry.requiresPrototypeRequest();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasUnresolvedDependencies() {
        return exportedEntry.hasUnresolvedDependencies();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        return new DelegatingInjectorEntry<>(this, exportedEntry.toRuntimeEntry(context));
    }

    /** {@inheritDoc} */
    @Override
    protected MethodHandle newMH(ServiceExtensionInstantiationContext context) {
        return exportedEntry.toMH(context);
    }
}
