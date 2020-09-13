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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.service.ExportedServiceConfiguration;
import app.packed.service.Service;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceMap;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.ErrorMessages;
import packed.internal.service.buildtime.SimpleServiceSet;
import packed.internal.service.buildtime.service.ExportedBuildEntry;
import packed.internal.service.buildtime.service.PackedExportedServiceConfiguration;
import packed.internal.util.KeyBuilder;

/**
 * This class manages everything to do with exporting of service entries.
 *
 * @see ServiceExtension#export(Class)
 * @see ServiceExtension#export(Key)
 * @see ServiceExtension#exportAll()
 */
public final class ExportManager implements Iterable<ExportedBuildEntry<?>> {

    /** The config site, if we export all entries. */
    @Nullable
    private ConfigSite exportAll;

    /**
     * An entry to this list is added every time the user calls {@link ServiceExtension#export(Class)},
     * {@link ServiceExtension#export(Key)}.
     */
    @Nullable
    private ArrayList<ExportedBuildEntry<?>> exportedEntries;

    /** A map of multiple exports of the same key. */
    @Nullable
    private LinkedHashMap<Key<?>, LinkedHashSet<ExportedBuildEntry<?>>> failingDuplicateExports;

    /** A map of all keyed exports where an entry matching the key could not be found. */
    @Nullable
    private LinkedHashMap<Key<?>, LinkedHashSet<ExportedBuildEntry<?>>> failingUnresolvedKeyedExports;

    /** The extension node this exporter is a part of. */
    private final InjectionManager im;

    /** All resolved exports. Is null until {@link #resolve()} has finished (successfully or just finished?). */
    @Nullable
    private LinkedHashMap<Key<?>, ExportedBuildEntry<?>> resolvedExports;

    /**
     * Creates a new export manager.
     * 
     * @param im
     *            the extension node this export manager belongs to
     */
    public ExportManager(InjectionManager im) {
        this.im = requireNonNull(im);
    }

    /**
     * Creates an export for the specified configuration.
     * 
     * @param <T>
     *            the type of service
     * @param entryToExport
     *            the entry to export
     * @param configSite
     *            the config site of the export
     * @return stuff
     */
    // I think exporting an entry locks its any providing key it might have...

    public <T> ExportedServiceConfiguration<T> export(BuildtimeService<T> entryToExport, ConfigSite configSite) {
        // I'm not sure we need the check after, we have put export() directly on a component configuration..
        // Perviously you could specify any entry, even something from another bundle.
        // if (entryToExport.node != node) {
        // throw new IllegalArgumentException("The specified configuration was created by another injector extension");
        // }
        return export0(new ExportedBuildEntry<>(im, entryToExport, configSite));
    }

    /**
     * Registers the specified key to be exported.
     * 
     * @param <T>
     *            the type of service
     * @param key
     *            the key of the service to export
     * @param configSite
     *            the config site of the export
     * @return a service configuration that can be returned to the user
     * @see ServiceExtension#export(Class)
     * @see ServiceExtension#export(Key)
     */
    public <T> ExportedServiceConfiguration<T> export(Key<T> key, ConfigSite configSite) {
        return export0(new ExportedBuildEntry<>(im, key, configSite));
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
    private <T> PackedExportedServiceConfiguration<T> export0(ExportedBuildEntry<T> entry) {
        ArrayList<ExportedBuildEntry<?>> e = exportedEntries;
        if (e == null) {
            e = exportedEntries = new ArrayList<>(5);
        }
        e.add(entry);
        return new PackedExportedServiceConfiguration<>(entry);
    }

    /**
     * Registers all entries for export.
     * 
     * @param configSite
     *            the config site of the export
     */
    public void exportAll(ConfigSite configSite) {
        exportAll = configSite;
        // Add exportAll(Predicate); //Maybe some exportAll(Consumer<ExportedConfg>)
        // exportAllAs(Function<?, Key>

        // Export all entries except foo which should be export as Boo
        // exportAll(Predicate) <- takes key or service configuration???
    }

    @Nullable
    public ServiceMap exports() {
        if (resolvedExports == null) {
            return null;
        }
        List<Service> l = new ArrayList<>();
        for (ExportedBuildEntry<?> e : this) {
            l.add(e.toDescriptor());
        }
        return new SimpleServiceSet(l);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<ExportedBuildEntry<?>> iterator() {
        if (resolvedExports == null) {
            List<ExportedBuildEntry<?>> l = List.of();
            return l.iterator();
        }
        return resolvedExports.values().iterator();
    }

    /**
     * This method tries to find matching entries for exports added via {@link ServiceExtension#export(Class)}and
     * {@link ServiceExtension#export(Key)}. We cannot do when they are called, as we allow export statements of entries at
     * any point, even before the
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void resolve() {
        // We could move unresolvedKeyedExports and duplicateExports in here. But keep them as fields
        // to have identical structure to ServiceProvidingManager

        LinkedHashMap<Key<?>, ExportedBuildEntry<?>> resolvedExports = new LinkedHashMap<>();
        // Process every exported build entry
        if (exportedEntries != null) {
            for (ExportedBuildEntry<?> entry : exportedEntries) {
                // try and find a matching service entry for key'ed exports via
                // exportedEntry != null for entries added via InjectionExtension#export(ProvidedComponentConfiguration)
                BuildtimeService<?> entryToExport = entry.exportedEntry;
                boolean export = true;
                if (entryToExport == null) {
                    entryToExport = im.resolvedEntries.get(entry.keyToExport);
                    if (entryToExport == null) {
                        if (failingUnresolvedKeyedExports == null) {
                            failingUnresolvedKeyedExports = new LinkedHashMap<>();
                        }
                        failingUnresolvedKeyedExports.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>()).add(entry);
                        export = false;
                    } else {
                        entry.exportedEntry = (BuildtimeService) entryToExport;
                    }
                }

                if (export) {
                    ExportedBuildEntry<?> existing = resolvedExports.putIfAbsent(entry.key(), entry);
                    if (existing != null) {
                        if (failingDuplicateExports == null) {
                            failingDuplicateExports = new LinkedHashMap<>();
                        }
                        LinkedHashSet<ExportedBuildEntry<?>> hs = failingDuplicateExports.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>());
                        hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                        hs.add(entry);
                    }
                }
            }
        }

        if (failingUnresolvedKeyedExports != null) {
            ErrorMessages.addUnresolvedExports(im, failingUnresolvedKeyedExports);
        }
        if (failingDuplicateExports != null) {
            // TODO add error messages
        }

        if (exportAll != null) {
            for (BuildtimeService<?> e : im.resolvedEntries.values()) {
                if (!e.key().equals(KeyBuilder.INJECTOR_KEY)) {
                    if (!resolvedExports.containsKey(e.key())) {
                        resolvedExports.put(e.key(), new ExportedBuildEntry<>(im, e, exportAll));
                    }
                }
            }
        }
        // Finally, make the resolved exports visible.
        this.resolvedExports = resolvedExports;
    }
}
