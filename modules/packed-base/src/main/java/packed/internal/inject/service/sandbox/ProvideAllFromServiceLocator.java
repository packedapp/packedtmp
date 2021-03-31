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
package packed.internal.inject.service.sandbox;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;

import app.packed.base.Key;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceLocator;
import packed.internal.inject.service.ServiceManagerSetup;
import packed.internal.inject.service.build.RuntimeAdaptorServiceSetup;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.inject.service.runtime.PackedInjector;

/** Represents an injector that used via {@link ServiceExtension#provideAll(ServiceLocator)}. */
public final class ProvideAllFromServiceLocator {

    /** The injector builder from where the service will be provided. */
    public final ServiceManagerSetup node;

    /** All entries that was imported, any wirelets that was specified when importing the injector may modify this map. */
    // Is not ProvideABE because we might transform some of the entries...
    public final LinkedHashMap<Key<?>, ServiceSetup> entries = new LinkedHashMap<>();

    /** The injector that provides the services. */
    final PackedInjector injector;

    /**
     * Creates a new instance.
     * 
     * @param node
     *            A builder for the injector that the injector is being imported into
     * @param injector
     *            the injector that is being imported
     */
    public ProvideAllFromServiceLocator(ServiceManagerSetup node, PackedInjector injector) {
        this.node = requireNonNull(node);
        this.injector = requireNonNull(injector);

        injector.forEachEntry(e -> {
            // ConfigSite cs = configSite.withParent(e.configSite());
            entries.put(e.key(), new RuntimeAdaptorServiceSetup(e));
        });
    }
}
