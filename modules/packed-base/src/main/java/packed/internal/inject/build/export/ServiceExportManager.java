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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.config.ConfigSite;
import app.packed.inject.ComponentServiceConfiguration;
import app.packed.inject.InjectionExtension;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceContract;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.BuildEntry;
import packed.internal.inject.build.ErrorMessages;
import packed.internal.inject.build.InjectionExtensionNode;
import packed.internal.inject.build.service.PackedProvidedComponentConfiguration;
import packed.internal.util.StringFormatter;

/**
 * This class manages everything to do with exporting of entries for an {@link InjectionExtension}.
 *
 * @see InjectionExtension#export(Class)
 * @see InjectionExtension#export(Key)
 * @see InjectionExtension#export(ComponentServiceConfiguration)
 * @see InjectionExtension#exportAll()
 */
public final class ServiceExportManager {

    /** The config site, if we export all entries. */
    @Nullable
    private ConfigSite exportAll;

    /**
     * An entry to this list is added every time the user calls {@link InjectionExtension#export(Class)},
     * {@link InjectionExtension#export(Key)} or {@link InjectionExtension#export(ComponentServiceConfiguration)}.
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
    private final InjectionExtensionNode node;

    /**
     * All resolved exports. Is null until {@link #resolve(InjectionExtensionNode, ArtifactBuildContext)} has been invoked.
     */
    @Nullable
    private LinkedHashMap<Key<?>, ExportedBuildEntry<?>> resolvedExports;

    /** */
    @Nullable
    private LinkedHashMap<Key<?>, ServiceEntry<?>> resolvedServiceMap = new LinkedHashMap<>();

    /**
     * Creates a new service export manager.
     * 
     * @param node
     *            the extension node this export manager belongs to
     */
    public ServiceExportManager(InjectionExtensionNode node) {
        this.node = requireNonNull(node);
    }

    public Collection<ExportedBuildEntry<?>> allExports() {
        return resolvedExports.values();
    }

    /**
     * Helps build an {@link ServiceContract}.
     * 
     * @param builder
     *            the contract builder
     */
    public void buildContract(ServiceContract.Builder builder) {
        for (ExportedBuildEntry<?> n : resolvedExports.values()) {
            builder.addProvides(n.key());
        }
    }

    /**
     * Creates an export for the specified configuration.
     * 
     * @param <T>
     *            the type of service
     * @param configuration
     *            the configuration of an existing entry to export
     * @param configSite
     *            the config site of the export
     * @return stuff
     * @see InjectionExtension#export(ComponentServiceConfiguration)
     */
    public <T> ServiceConfiguration<T> export(ComponentServiceConfiguration<T> configuration, ConfigSite configSite) {
        if (!(configuration instanceof PackedProvidedComponentConfiguration)) {
            throw new IllegalArgumentException("Custom implementations of " + StringFormatter.format(ComponentServiceConfiguration.class)
                    + " are not allowed, type = " + StringFormatter.format(configuration.getClass()));
        }
        BuildEntry<T> entryToExport = ((PackedProvidedComponentConfiguration<T>) configuration).buildEntry;
        if (entryToExport.injectorBuilder != node) {
            throw new IllegalArgumentException("The specified configuration was created by another injector extension");
        }
        return export0(new ExportedBuildEntry<>(node, entryToExport, configSite));
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
     * @see InjectionExtension#export(Class)
     * @see InjectionExtension#export(Key)
     */
    public <T> ServiceConfiguration<T> export(Key<T> key, ConfigSite configSite) {
        return export0(new ExportedBuildEntry<>(node, key, configSite));
    }

    private <T> ServiceConfiguration<T> export0(ExportedBuildEntry<T> entry) {
        ArrayList<ExportedBuildEntry<?>> e = exportedEntries;
        if (e == null) {
            e = exportedEntries = new ArrayList<>(5);
        }
        e.add(entry);
        return new ExportedServiceConfiguration<>(entry);
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

    /**
     * This method tries to find matching entries for exports added via {@link InjectionExtension#export(Class)}and
     * {@link InjectionExtension#export(Key)}. We cannot do when they are called, as we allow export statements of entries
     * at any point, even before the
     * 
     * @param resolver
     * @param buildContext
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void resolve(InjectionExtensionNode resolver, ArtifactBuildContext buildContext) {
        // We could move unresolvedKeyedExports and duplicateExports in here. But keep them as fields
        // to have identical structure to ServiceProvidingManager

        LinkedHashMap<Key<?>, ExportedBuildEntry<?>> resolvedExports = new LinkedHashMap<>();
        // Process every exported build entry
        if (exportedEntries != null) {
            for (ExportedBuildEntry<?> entry : exportedEntries) {
                // try and find a matching service entry for key'ed exports via
                // exportedEntry != null for entries added via InjectionExtension#export(ProvidedComponentConfiguration)
                ServiceEntry<?> entryToExport = entry.exportedEntry;
                boolean export = true;
                if (entryToExport == null) {
                    entryToExport = resolver.resolvedEntries.get(entry.keyToExport);
                    if (entryToExport == null) {
                        if (failingUnresolvedKeyedExports == null) {
                            failingUnresolvedKeyedExports = new LinkedHashMap<>();
                        }
                        failingUnresolvedKeyedExports.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>()).add(entry);
                        export = false;
                    } else {
                        entry.exportedEntry = (ServiceEntry) entryToExport;
                    }
                }

                if (export) {
                    ExportedBuildEntry<?> existing = resolvedExports.putIfAbsent(entry.key, entry);
                    if (existing != null) {
                        if (failingDuplicateExports == null) {
                            failingDuplicateExports = new LinkedHashMap<>();
                        }
                        LinkedHashSet<ExportedBuildEntry<?>> hs = failingDuplicateExports.computeIfAbsent(entry.key, m -> new LinkedHashSet<>());
                        hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                        hs.add(entry);
                    }
                }
            }
        }

        if (failingUnresolvedKeyedExports != null) {
            ErrorMessages.addUnresolvedExports(buildContext, failingUnresolvedKeyedExports);
        }
        if (failingDuplicateExports != null) {
            // TODO add error messages
        }

        if (exportAll != null) {
            for (ServiceEntry<?> e : resolver.resolvedEntries.values()) {
                if (!e.isPrivate()) {
                    if (!resolvedExports.containsKey(e.key())) {
                        resolvedExports.put(e.key(), new ExportedBuildEntry<>(node, e, exportAll));
                    }
                }
            }
        }
        // Finally, make the resolved exports visible.
        this.resolvedExports = resolvedExports;
    }

    public LinkedHashMap<Key<?>, ServiceEntry<?>> resolvedServiceMap() {
        LinkedHashMap<Key<?>, ServiceEntry<?>> r = resolvedServiceMap;
        if (r != null) {
            LinkedHashMap<Key<?>, ServiceEntry<?>> m = new LinkedHashMap<>();
            m.putAll(resolvedExports);
            r = resolvedServiceMap = m;
        }
        return r;
    }
}
