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
package packed.internal.service.buildtime.service;

import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.service.Injector;
import app.packed.service.ServiceExtension;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.dependencies.InjectionManager;
import packed.internal.service.runtime.DelegatingInjectorEntry;
import packed.internal.service.runtime.RuntimeService;

/** An entry specifically used for {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
public final class RuntimeAdaptorEntry<T> extends BuildtimeService<T> {

    /** The entry from the 'imported' injector. */
    private final RuntimeService<T> entry;

    public RuntimeAdaptorEntry(InjectionManager node, RuntimeService<T> entry) {
        super(node, ConfigSite.UNKNOWN);
        this.entry = entry;
        setKey(entry.key());
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        return new DelegatingInjectorEntry<T>(this, entry);
    }
}
