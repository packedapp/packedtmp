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
package packed.internal.inject.service.build;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.serviceexpose.ServiceExtension;
import packed.internal.inject.DependencyNode;
import packed.internal.inject.service.runtime.DelegatingRuntimeService;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;

/**
 * A build entry representing an exported service. Entries at runtime has never any reference to how (or if) they where
 * exported.
 */
public final class ExportedServiceSetup extends ServiceSetup {

    /** The key under which to export the entry, is null for entry exports. */
    @Nullable
    public final Key<?> exportAsKey;

    /** The actual entry that is exported. Is initially null for keyed exports, until it is resolved. */
    @Nullable
    public ServiceSetup serviceToExport;

    /**
     * Exports an entry via its key.
     * 
     * @param builder
     *            the injector configuration this node is being added to
     * @see ServiceExtension#export(Class)
     * @see ServiceExtension#export(Key)
     */
    public ExportedServiceSetup(Key<?> exportAsKey) {
        super(exportAsKey);
        this.exportAsKey = requireNonNull(exportAsKey);
    }

    /**
     * Exports an existing entry.
     * 
     * @param entryToExport
     *            the entry to export
     * @see ServiceExtension#exportAll()
     */
    public ExportedServiceSetup(ServiceSetup entryToExport) {
        super(entryToExport.key());
        this.serviceToExport = entryToExport;
        this.exportAsKey = null;
        // Export of export, of export????
        // Hvad hvis en eller anden aendrer en key midt i chainen.
        // Slaar det igennem i hele vejen ned.
    }

    @Override
    @Nullable
    public DependencyNode dependencyConsumer() {
        return serviceToExport.dependencyConsumer();
    }

    @Override
    public MethodHandle dependencyAccessor() {
        return serviceToExport.dependencyAccessor();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return serviceToExport.isConstant();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        return new DelegatingRuntimeService(key(), serviceToExport.toRuntimeEntry(context));
    }
}
