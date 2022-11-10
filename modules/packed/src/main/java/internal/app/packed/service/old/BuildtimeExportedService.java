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
package internal.app.packed.service.old;

import java.lang.invoke.MethodHandle;

import app.packed.framework.Nullable;
import app.packed.service.Key;
import app.packed.service.ServiceExtension;
import internal.app.packed.lifetime.LifetimeObjectArena;

/**
 * A build entry representing an exported service. Entries at runtime has never any reference to how (or if) they where
 * exported.
 */
public final class BuildtimeExportedService implements BuildtimeService {

    /** The actual entry that is exported. Is initially null for keyed exports, until it is resolved. */
    @Nullable
    public BuildtimeService serviceToExport;

    public final Key<?> key;

    /**
     * Exports an existing entry.
     * 
     * @param entryToExport
     *            the entry to export
     * @see ServiceExtension#exportAll()
     */
    public BuildtimeExportedService(BuildtimeService entryToExport) {
        this.key = entryToExport.key();
        this.serviceToExport = entryToExport;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle buildInvoker(LifetimeObjectArena context) {
        return serviceToExport.buildInvoker(context);
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return key;
    }
}
