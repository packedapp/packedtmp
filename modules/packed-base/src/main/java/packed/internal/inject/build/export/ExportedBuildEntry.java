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
package packed.internal.inject.build.export;

import java.util.List;

import app.packed.config.ConfigSite;
import app.packed.inject.InjectionExtension;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvideHelper;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.BuildEntry;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.run.RSE;
import packed.internal.inject.run.RSEDelegate;

/** A build entry representing an exported service. */
public final class ExportedBuildEntry<T> extends BuildEntry<T> {

    /** The node that is exported. Is null initially for key'ed exports. */
    @Nullable
    ServiceEntry<T> exportedEntry;

    /**
     * Exports an existing entry in an injector extension.
     * 
     * @param entryToExport
     *            the entry to export
     * @param configSite
     *            the config site of the export
     * @see InjectionExtension#export(ProvidedComponentConfiguration)
     */
    @SuppressWarnings("unchecked")
    ExportedBuildEntry(InjectorBuilder builder, ServiceEntry<T> entryToExport, ConfigSite configSite) {
        super(builder, configSite, List.of());
        this.exportedEntry = entryToExport;

        // Export of export, of export????
        // Hvad hvis en eller anden aendrer en key midt i chainen.
        // Slaar det igennem i hele vejen ned.
        this.key = (Key<T>) entryToExport.key();
    }

    /**
     * Exports an entry via its key. Is typically used via {@link InjectionExtension#export(Class)} or
     * {@link InjectionExtension#export(Key)}.
     * 
     * @param builder
     *            the injector configuration this node is being added to
     * @param configSite
     *            the configuration site of the exposure
     */
    ExportedBuildEntry(InjectorBuilder builder, Key<T> key, ConfigSite configSite) {
        super(builder, configSite, List.of());
        as(key);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public BuildEntry<?> declaringNode() {
        // Skal vi ikke returnere exposureOf?? istedet for .declaringNode
        return (exportedEntry instanceof BuildEntry) ? ((BuildEntry<?>) exportedEntry).declaringNode() : null;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideHelper site) {
        return null;// ???
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return exportedEntry.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResolving() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    protected RSE<T> newRuntimeNode() {
        return new RSEDelegate<>(this, exportedEntry);
    }
}
