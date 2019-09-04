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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.config.ConfigSite;
import app.packed.inject.InjectionExtension;
import app.packed.inject.InjectorContract;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.BuildEntry;
import packed.internal.inject.build.ErrorMessages;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.build.service.PackedProvidedComponentConfiguration;
import packed.internal.inject.util.ServiceNodeMap;
import packed.internal.util.StringFormatter;

/**
 * This class takes care of everything to do with exporting entries.
 *
 * @see InjectionExtension#export(Class)
 * @see InjectionExtension#export(Key)
 * @see InjectionExtension#export(ProvidedComponentConfiguration)
 * @see InjectionExtension#exportAll()
 */
public final class ServiceExporter {

    /** The injector builder this exporter belongs to. */
    private final InjectorBuilder builder;

    /** A map of multiple exports for the same key. */
    @Nullable
    private LinkedHashMap<Key<?>, LinkedHashSet<ExportedBuildEntry<?>>> duplicateExports;

    private ConfigSite exportAll;

    /**
     * All nodes that have been exported, typically via {@link InjectionExtension#export(Class)},
     * {@link InjectionExtension#export(Key)} or {@link InjectionExtension#export(ProvidedComponentConfiguration)}.
     */
    private final ArrayList<ExportedBuildEntry<?>> exports = new ArrayList<>();

    /** */
    public final ServiceNodeMap resolvedExports = new ServiceNodeMap();

    /** A map of all keyed exports where an entry matching the key could not be found. */
    @Nullable
    private LinkedHashMap<Key<?>, HashSet<ExportedBuildEntry<?>>> unresolvedKeyedExports;

    /**
     * Creates a new service exporter.
     * 
     * @param builder
     *            the builder this exporter belongs to
     */
    public ServiceExporter(InjectorBuilder builder) {
        this.builder = requireNonNull(builder);
    }

    public void buildDescriptor(InjectorContract.Builder builder) {
        for (ServiceEntry<?> n : resolvedExports) {
            builder.addProvides(n.key());
        }
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
        ExportedBuildEntry<T> e = new ExportedBuildEntry<>(builder, key, configSite);
        exports.add(e);
        return new ExportedServiceConfiguration<>(e);
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
     * @see InjectionExtension#export(ProvidedComponentConfiguration)
     */
    public <T> ServiceConfiguration<T> export(ProvidedComponentConfiguration<T> configuration, ConfigSite configSite) {
        if (!(configuration instanceof PackedProvidedComponentConfiguration)) {
            throw new IllegalArgumentException("Custom implementations of " + StringFormatter.format(ProvidedComponentConfiguration.class)
                    + " are not allowed, type = " + StringFormatter.format(configuration.getClass()));
        }
        BuildEntry<T> entryToExport = ((PackedProvidedComponentConfiguration<T>) configuration).buildEntry;
        if (entryToExport.injectorBuilder != builder) {
            throw new IllegalArgumentException("The specified configuration object was created by another injector extension instance");
        }
        ExportedBuildEntry<T> e = new ExportedBuildEntry<>(builder, entryToExport, configSite);
        exports.add(e);
        return new ExportedServiceConfiguration<>(e);
    }

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
    public void resolve(InjectorBuilder resolver, ArtifactBuildContext buildContext) {
        // We could move unresolvedKeyedExports and duplicateExports in here. But keep them as fields
        // to have identical structure to ServiceProvidingManager

        // Process every exported build entry
        for (ExportedBuildEntry<?> entry : exports) {
            // try and find a matching service entry for key'ed exports via
            // exportedEntry != null for entries added via InjectionExtension#export(ProvidedComponentConfiguration)
            ServiceEntry<?> entryToExport = entry.exportedEntry;
            boolean export = true;
            if (entryToExport == null) {
                entryToExport = resolver.resolvedEntries.getRecursive(entry.keyToExport);
                if (entryToExport == null) {
                    if (unresolvedKeyedExports == null) {
                        unresolvedKeyedExports = new LinkedHashMap<>();
                    }
                    unresolvedKeyedExports.computeIfAbsent(entry.key(), m -> new HashSet<>()).add(entry);
                    export = false;
                } else {
                    entry.exportedEntry = (ServiceEntry) entryToExport;
                }
            }

            if (export) {
                ExportedBuildEntry<?> existing = (ExportedBuildEntry<?>) resolvedExports.putIfAbsent(entry);
                if (existing != null) {
                    if (duplicateExports == null) {
                        duplicateExports = new LinkedHashMap<>();
                    }
                    LinkedHashSet<ExportedBuildEntry<?>> hs = duplicateExports.computeIfAbsent(entry.key, m -> new LinkedHashSet<>());
                    hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                    hs.add(entry);
                }
            }
        }

        if (unresolvedKeyedExports != null) {
            ErrorMessages.addUnresolvedExports(buildContext, unresolvedKeyedExports);
        }
        if (duplicateExports != null) {
            // TODO add error messages
        }

        if (exportAll != null) {
            for (ServiceEntry<?> e : resolver.resolvedEntries) {
                if (!e.isPrivate()) {
                    if (!resolvedExports.containsKey(e.key())) {
                        resolvedExports.put(new ExportedBuildEntry<>(builder, e, exportAll));
                    }
                }
            }
        }

    }

}
