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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.ServiceComposer;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceRegistry;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.inject.service.build.BuildtimeService;
import packed.internal.inject.service.build.ExportedBuildtimeService;
import packed.internal.inject.service.build.PackedServiceComposer;

/**
 * This class manages everything to do with exporting of service entries.
 *
 * @see ServiceExtension#export(Class)
 * @see ServiceExtension#export(Key)
 * @see ServiceExtension#exportAll()
 */
public final class ServiceExportManager implements Iterable<BuildtimeService> {

    /** The config site, if we export all entries. */
    private boolean exportAll;

    /**
     * An entry to this list is added every time the user calls {@link ServiceExtension#export(Class)},
     * {@link ServiceExtension#export(Key)}.
     */
    @Nullable
    private ArrayList<ExportedBuildtimeService> exportedEntries;

    /** All resolved exports. Is null until {@link #resolve()} has finished (successfully or just finished?). */
    @Nullable
    private final LinkedHashMap<Key<?>, BuildtimeService> resolvedExports = new LinkedHashMap<>();

    /** The extension node this exporter is a part of. */
    private final ServiceManager sm;

    @Nullable
    Consumer<? super ServiceComposer> transformer;

    /**
     * Creates a new export manager.
     * 
     * @param sm
     *            the extension node this export manager belongs to
     */
    ServiceExportManager(ServiceManager sm) {
        this.sm = requireNonNull(sm);
    }

    /**
     * @param transformer
     */
    public void addExportTransformer(Consumer<? super ServiceComposer> transformer) {
        if (this.transformer != null) {
            throw new IllegalStateException("Can only set an export transformer once");
        }
        this.transformer = requireNonNull(transformer, "transformer is null");
    }

    public boolean contains(Key<?> key) {
        return resolvedExports.containsKey(key);
    }

    /**
     * Registers the specified key to be exported.
     * 
     * @param <T>
     *            the type of service
     * @param key
     *            the key of the service to export
     * @return a service configuration that can be returned to the user
     * @see ServiceExtension#export(Class)
     * @see ServiceExtension#export(Key)
     */
    public <T> ExportedServiceConfiguration<T> export(Key<T> key) {
        return export0(new ExportedBuildtimeService(sm, key));
    }

    /**
     * Creates an export for the specified configuration.
     * 
     * @param <T>
     *            the type of service
     * @param entryToExport
     *            the entry to export
     * @return stuff
     */
    // I think exporting an entry locks its any providing key it might have...

    public <T> ExportedServiceConfiguration<T> export(BuildtimeService entryToExport) {
        // I'm not sure we need the check after, we have put export() directly on a component configuration..
        // Perviously you could specify any entry, even something from another bundle.
        // if (entryToExport.node != node) {
        // throw new IllegalArgumentException("The specified configuration was created by another injector extension");
        // }
        return export0(new ExportedBuildtimeService(entryToExport));
    }

    /**
     * Registers the specified exported build entry object.
     * 
     * @param <T>
     *            the type of service to export
     * @param entry
     *            the build entry to export
     * @return a configuration object that can be exposed to the user
     */
    private <T> PackedExportedServiceConfiguration<T> export0(ExportedBuildtimeService entry) {
        // Vi bliver noedt til at vente til vi har resolvet... med finde ud af praecis hvad der skal ske
        // F.eks. hvis en extension publisher en service vi gerne vil exportere
        // Saa sker det maaske foerst naar den completer.
        // dvs efter bundle.configure() returnere
        ArrayList<ExportedBuildtimeService> e = exportedEntries;
        if (e == null) {
            e = exportedEntries = new ArrayList<>(5);
        }
        e.add(entry);
        return new PackedExportedServiceConfiguration<>(entry);
    }

    /**
     * Registers all entries for export.
     * 
     */
    public void exportAll() {
        exportAll = true;
    }

    /**
     * Returns all exported services in a service registry. Or null if there are no exported services.
     * 
     * @return all exported services in a service registry. Or null if there are no exported services
     */
    @Nullable
    public ServiceRegistry exportsAsServiceRegistry() {
        return resolvedExports.isEmpty() ? null : AbstractServiceRegistry.copyOf(resolvedExports);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<BuildtimeService> iterator() {
        return resolvedExports.values().iterator();
    }

    /**
     * This method tries to find matching entries for exports added via {@link ServiceExtension#export(Class)}and
     * {@link ServiceExtension#export(Key)}. We cannot do when they are called, as we allow export statements of entries at
     * any point, even before the
     */
    public void resolve() {
        // We could move unresolvedKeyedExports and duplicateExports in here. But keep them as fields
        // to have identical structure to ServiceProvidingManager
        // Process every exported build entry
        if (exportedEntries != null) {
            for (ExportedBuildtimeService entry : exportedEntries) {
                // try and find a matching service entry for key'ed exports via
                // exportedEntry != null for entries added via InjectionExtension#export(ProvidedComponentConfiguration)
                BuildtimeService entryToExport = entry.exportedEntry;
                if (entryToExport == null) {
                    Wrapper wrapper = sm.resolvedServices.get(entry.exportAsKey);
                    entryToExport = wrapper == null ? null : wrapper.getSingle();
                    entry.exportedEntry = entryToExport;
                    if (entryToExport == null) {
                        sm.errorManager().failingUnresolvedKeyedExports.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>()).add(entry);
                    }
                }

                if (entry.exportedEntry != null) {
                    BuildtimeService existing = resolvedExports.putIfAbsent(entry.key(), entry);
                    if (existing != null) {
                        LinkedHashSet<BuildtimeService> hs = sm.errorManager().failingDuplicateExports.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>());
                        hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                        hs.add(entry);
                    }
                }
            }
        }

        if (sm.errorManager().failingUnresolvedKeyedExports != null) {
            InjectionErrorManagerMessages.addUnresolvedExports(sm, sm.errorManager().failingUnresolvedKeyedExports);
        }
        if (sm.errorManager().failingDuplicateExports != null) {
            // TODO add error messages
        }

        if (exportAll) {
            for (Wrapper w : sm.resolvedServices.values()) {
                BuildtimeService e = w.getSingle();
                if (!resolvedExports.containsKey(e.key())) {
                    resolvedExports.put(e.key(), new ExportedBuildtimeService(e));
                }
            }
        }
        if (transformer != null) {
            transform(transformer);
        }
        // Finally, make the resolved exports visible.
    }

    public void transform(BiConsumer<? super ServiceComposer, ? super ServiceContract> transformer) {
        PackedServiceComposer.transformInplaceAttachment(resolvedExports, transformer, sm.newServiceContract());
    }

    /**
     * Transforms the exported services using the specified transformer.
     * 
     * @param transformer
     *            the transformer to use
     */
    public void transform(Consumer<? super ServiceComposer> transformer) {
        PackedServiceComposer.transformInplace(resolvedExports, transformer);
    }
}
