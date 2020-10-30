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
import app.packed.config.ConfigSite;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceRegistry;
import app.packed.inject.ServiceTransformation;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.inject.service.build.ExportedServiceBuild;
import packed.internal.inject.service.build.ServiceBuild;
import packed.internal.inject.service.runtime.AbstractServiceRegistry;
import packed.internal.inject.service.runtime.PackedServiceTransformer;

/**
 * This class manages everything to do with exporting of service entries.
 *
 * @see ServiceExtension#export(Class)
 * @see ServiceExtension#export(Key)
 * @see ServiceExtension#exportAll()
 */
public final class ServiceExportManager implements Iterable<ServiceBuild> {

    /** The config site, if we export all entries. */
    @Nullable
    private ConfigSite exportAll;

    /**
     * An entry to this list is added every time the user calls {@link ServiceExtension#export(Class)},
     * {@link ServiceExtension#export(Key)}.
     */
    @Nullable
    private ArrayList<ExportedServiceBuild> exportedEntries;

    /** All resolved exports. Is null until {@link #resolve()} has finished (successfully or just finished?). */
    @Nullable
    private final LinkedHashMap<Key<?>, ServiceBuild> resolvedExports = new LinkedHashMap<>();

    /** The extension node this exporter is a part of. */
    private final ServiceBuildManager sm;

    /**
     * Creates a new export manager.
     * 
     * @param sm
     *            the extension node this export manager belongs to
     */
    ServiceExportManager(ServiceBuildManager sm) {
        this.sm = requireNonNull(sm);
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
        return export0(new ExportedServiceBuild(sm, key, configSite));
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

    public <T> ExportedServiceConfiguration<T> export(ServiceBuild entryToExport, ConfigSite configSite) {
        // I'm not sure we need the check after, we have put export() directly on a component configuration..
        // Perviously you could specify any entry, even something from another bundle.
        // if (entryToExport.node != node) {
        // throw new IllegalArgumentException("The specified configuration was created by another injector extension");
        // }
        return export0(new ExportedServiceBuild(entryToExport, configSite));
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
    private <T> PackedExportedServiceConfiguration<T> export0(ExportedServiceBuild entry) {
        // Vi bliver noedt til at vente til vi har resolvet... med finde ud af praecis hvad der skal ske
        // F.eks. hvis en extension publisher en service vi gerne vil exportere
        // Saa sker det maaske foerst naar den completer.
        // dvs efter bundle.configure() returnere
        ArrayList<ExportedServiceBuild> e = exportedEntries;
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
    public Iterator<ServiceBuild> iterator() {
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
            for (ExportedServiceBuild entry : exportedEntries) {
                // try and find a matching service entry for key'ed exports via
                // exportedEntry != null for entries added via InjectionExtension#export(ProvidedComponentConfiguration)
                ServiceBuild entryToExport = entry.exportedEntry;
                if (entryToExport == null) {
                    Wrapper wrapper = sm.resolvedServices.get(entry.exportAsKey);
                    entryToExport = wrapper == null ? null : wrapper.getSingle();
                    entry.exportedEntry = entryToExport;
                    if (entryToExport == null) {
                        sm.errorManager().failingUnresolvedKeyedExports.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>()).add(entry);
                    }
                }

                if (entry.exportedEntry != null) {
                    ServiceBuild existing = resolvedExports.putIfAbsent(entry.key(), entry);
                    if (existing != null) {
                        LinkedHashSet<ServiceBuild> hs = sm.errorManager().failingDuplicateExports.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>());
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

        if (exportAll != null) {
            for (Wrapper w : sm.resolvedServices.values()) {
                ServiceBuild e = w.getSingle();
                if (!resolvedExports.containsKey(e.key())) {
                    resolvedExports.put(e.key(), new ExportedServiceBuild(e, exportAll));
                }
            }
        }
        if (transformer != null) {
            transform(transformer);
        }
        // Finally, make the resolved exports visible.
    }

    public void transform(BiConsumer<? super ServiceTransformation, ? super ServiceContract> transformer) {
        PackedServiceTransformer.transformInplaceAttachment(resolvedExports, transformer, sm.newServiceContract());
    }

    /**
     * Transforms the exported services using the specified transformer.
     * 
     * @param transformer
     *            the transformer to use
     */
    public void transform(Consumer<? super ServiceTransformation> transformer) {
        PackedServiceTransformer.transformInplace(resolvedExports, transformer);
    }

    @Nullable
    Consumer<? super ServiceTransformation> transformer;

    /**
     * @param transformer
     */
    public void addExportTransformer(Consumer<? super ServiceTransformation> transformer) {
        if (this.transformer != null) {
            throw new IllegalStateException("Can only set an export transformer once");
        }
        this.transformer = requireNonNull(transformer, "transformer is null");
    }

    public boolean contains(Key<?> key) {
        return resolvedExports.containsKey(key);
    }
}
