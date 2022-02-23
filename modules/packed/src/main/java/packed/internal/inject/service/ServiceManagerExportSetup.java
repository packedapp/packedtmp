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
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import app.packed.inject.service.ServiceConfiguration;
import app.packed.inject.service.ServiceContract;
import app.packed.inject.service.ServiceExtension;
import app.packed.inject.service.ServiceRegistry;
import app.packed.inject.service.ServiceTransformer;
import packed.internal.inject.service.build.ExportedServiceSetup;
import packed.internal.inject.service.build.PackedServiceComposer;
import packed.internal.inject.service.build.ServiceSetup;

/**
 * This class manages everything to do with exporting of services.
 *
 * @see ServiceExtension#exportAll()
 */
public final class ServiceManagerExportSetup implements Iterable<ServiceSetup> {

    /** The config site, if we export all entries. */
    private boolean exportAll;

    /**
     * An entry to this list is added every time the user calls {@link ServiceExtension#export(Class)},
     * {@link ServiceExtension#export(Key)}.
     */
    @Nullable
    private ArrayList<ExportedServiceSetup> exportedEntries;

    /** All resolved exports. Is null until {@link #resolve()} has finished (successfully or just finished?). */
    @Nullable
    private final LinkedHashMap<Key<?>, ServiceSetup> resolvedExports = new LinkedHashMap<>();

    /** The extension node this exporter is a part of. */
    private final ContainerInjectionManager sm;

    @Nullable
    Consumer<? super ServiceTransformer> transformer;

    /**
     * Creates a new export manager.
     * 
     * @param sm
     *            the extension node this export manager belongs to
     */
    ServiceManagerExportSetup(ContainerInjectionManager sm) {
        this.sm = requireNonNull(sm);
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
     */
    public <T> ExportedServiceConfiguration<T> export(Key<T> key) {
        return export0(new ExportedServiceSetup(key));
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

    public <T> ExportedServiceConfiguration<T> export(ServiceSetup entryToExport) {
        // I'm not sure we need the check after, we have put export() directly on a component configuration..
        // Perviously you could specify any entry, even something from another assembly.
        // if (entryToExport.node != node) {
        // throw new IllegalArgumentException("The specified configuration was created by another injector extension");
        // }
        return export0(new ExportedServiceSetup(entryToExport));
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
    private <T> ExportedServiceConfigurationSetup<T> export0(ExportedServiceSetup entry) {
        // Vi bliver noedt til at vente til vi har resolvet... med finde ud af praecis hvad der skal ske
        // F.eks. hvis en extension publisher en service vi gerne vil exportere
        // Saa sker det maaske foerst naar den completer.
        // dvs efter assembly.configure() returnere
        ArrayList<ExportedServiceSetup> e = exportedEntries;
        if (e == null) {
            e = exportedEntries = new ArrayList<>(5);
        }
        e.add(entry);
        return new ExportedServiceConfigurationSetup<>(entry);
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

    public boolean hasExports() {
        return !resolvedExports.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<ServiceSetup> iterator() {
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
            for (ExportedServiceSetup entry : exportedEntries) {
                // try and find a matching service entry for key'ed exports via
                // exportedEntry != null for entries added via InjectionExtension#export(ProvidedComponentConfiguration)
                ServiceSetup entryToExport = entry.serviceToExport;
                if (entryToExport == null) {
                    ServiceDelegate wrapper = sm.resolvedServices.get(entry.exportAsKey);
                    entryToExport = wrapper == null ? null : wrapper.getSingle();
                    entry.serviceToExport = entryToExport;
                    if (entryToExport == null) {
                        sm.errorManager().failingUnresolvedKeyedExports.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>()).add(entry);
                    }
                }

                if (entry.serviceToExport != null) {
                    ServiceSetup existing = resolvedExports.putIfAbsent(entry.key(), entry);
                    if (existing != null) {
                        LinkedHashSet<ServiceSetup> hs = sm.errorManager().failingDuplicateExports.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>());
                        hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                        hs.add(entry);
                    }
                }
            }
        }

        if (sm.errorManager().failingUnresolvedKeyedExports != null) {
            ServiceManagerFailureSetup.addUnresolvedExports(sm, sm.errorManager().failingUnresolvedKeyedExports);
        }
        if (sm.errorManager().failingDuplicateExports != null) {
            // TODO add error messages
        }

        if (exportAll) {
            for (ServiceDelegate w : sm.resolvedServices.values()) {
                ServiceSetup e = w.getSingle();
                if (!resolvedExports.containsKey(e.key())) {
                    resolvedExports.put(e.key(), new ExportedServiceSetup(e));
                }
            }
        }
        if (transformer != null) {
            transform(transformer);
        }
        // Finally, make the resolved exports visible.
    }

    /**
     * @param transformer
     */
    public void setExportTransformer(Consumer<? super ServiceTransformer> transformer) {
        if (this.transformer != null) {
            throw new IllegalStateException("This method can only be called once");
        }
        this.transformer = requireNonNull(transformer, "transformer is null");
    }

    public void transform(BiConsumer<? super ServiceTransformer, ? super ServiceContract> transformer) {
        PackedServiceComposer.transformInplaceAttachment(resolvedExports, transformer, sm.newServiceContract());
    }

    /**
     * Transforms the exported services using the specified transformer.
     * 
     * @param transformer
     *            the transformer to use
     */
    public void transform(Consumer<? super ServiceTransformer> transformer) {
        PackedServiceComposer.transformInplace(resolvedExports, transformer);
    }

    /**
     * An instance of {@link ExportedServiceConfiguration} that is returned to the user when they export a service
     * 
     * @see ServiceConfiguration#export()
     * @see ServiceExtension#export(Class)
     * @see ServiceExtension#export(Key)
     */
    record ExportedServiceConfigurationSetup<T> (ExportedServiceSetup service) implements ExportedServiceConfiguration<T> {

        /** {@inheritDoc} */
        @Override
        public ExportedServiceConfiguration<T> as(@Nullable Key<? super T> key) {
            // TODO, maybe it gets disabled the minute we start analyzing exports???
            // Nah, lige saa snart, vi begynder
//            entry.sm.checkExportConfigurable();
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> key() {
            return service.key();
        }
    }
}
