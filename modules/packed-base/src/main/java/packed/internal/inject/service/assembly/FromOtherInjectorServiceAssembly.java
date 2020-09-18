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
package packed.internal.inject.service.assembly;

import java.lang.invoke.MethodHandle;

import app.packed.component.Wirelet;
import app.packed.service.Injector;
import app.packed.service.ServiceExtension;
import packed.internal.inject.service.runtime.DelegatingInjectorEntry;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;

/** An entry specifically used for {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
final class FromOtherInjectorServiceAssembly<T> extends ServiceAssembly<T> {

    /** The entry from the 'imported' injector. */
    private final RuntimeService<T> entry;

    /** A wrapper for the 'imported' injector. */
    final ProvideAllFromOtherInjector fromInjector; // not used currently

    FromOtherInjectorServiceAssembly(ProvideAllFromOtherInjector fromInjector, RuntimeService<T> entry) {
        super(fromInjector.node.services(), fromInjector.configSite.withParent(entry.configSite()), entry.key());
        this.entry = entry;
        this.fromInjector = fromInjector;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService<T> newRuntimeNode(ServiceInstantiationContext context) {
        return new DelegatingInjectorEntry<T>(this, entry);
    }

    @Override
    public MethodHandle dependencyAccessor() {
        throw new UnsupportedOperationException();
    }

}
