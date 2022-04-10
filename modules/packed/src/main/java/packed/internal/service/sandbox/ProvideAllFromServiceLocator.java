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
package packed.internal.service.sandbox;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;

import app.packed.base.Key;
import app.packed.inject.service.ServiceExtension;
import app.packed.inject.service.ServiceLocator;
import packed.internal.inject.factory.service.ContainerInjectionManager;
import packed.internal.inject.factory.service.build.RuntimeAdaptorServiceSetup;
import packed.internal.inject.factory.service.build.ServiceSetup;
import packed.internal.inject.factory.service.runtime.AbstractServiceLocator;
import packed.internal.inject.factory.service.runtime.RuntimeService;

/** Represents an invocation of {@link ServiceExtension#provideAll(ServiceLocator)}. */
public final class ProvideAllFromServiceLocator {

    /** All entries that was imported, any wirelets that was specified when importing the injector may modify this map. */
    // Is not ProvideABE because we might transform some of the entries...
    public final LinkedHashMap<Key<?>, ServiceSetup> entries = new LinkedHashMap<>();

    /** The locator containing every service. */
    final AbstractServiceLocator locator; // currently not used

    /** The injector builder from where the service will be provided. */
    public final ContainerInjectionManager sm;

    /**
     * Creates a new instance.
     * 
     * @param sm
     *            A builder for the injector that the injector is being imported into
     * @param locator
     *            the locator we are importing from
     */
    public ProvideAllFromServiceLocator(ContainerInjectionManager sm, AbstractServiceLocator locator) {
        this.sm = requireNonNull(sm);
        this.locator = requireNonNull(locator);

        for (RuntimeService s : locator.runtimeServices()) {
            entries.put(s.key(), new RuntimeAdaptorServiceSetup(s));
        }
    }
}
