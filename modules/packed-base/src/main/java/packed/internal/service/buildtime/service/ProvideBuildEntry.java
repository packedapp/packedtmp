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

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import packed.internal.component.SourceAssembly;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.InjectionManager;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.runtime.IndexedEntry;
import packed.internal.service.runtime.RuntimeEntry;

/**
 *
 */
public class ProvideBuildEntry<T> extends BuildEntry<T> {

    final SourceAssembly source;

    final AtProvides ap;

    public final Injectable injectable;

    public final int regionIndex;

    /**
     * Creates a new node from an instance.
     * 
     * @param services
     *            the injector builder
     */
    public ProvideBuildEntry(ConfigSite configSite, InjectionManager services, SourceAssembly sa, AtProvides ap) {
        super(services, configSite);
        this.source = sa;
        this.ap = ap;
        // if singleton reserve... protype no...
        this.regionIndex = sa.component.region.reserve();
        this.injectable = Injectable.ofDeclaredMember(sa, ap);
        if (ap.isConstant) {
            // sa.component.region.resolver.sourceInjectables
            // Constants should be stored
        }
    }

    @Override
    public MethodHandle toMethodHandle() {
        return injectable.buildMethodHandle();
    }

    @Override
    public @Nullable Injectable injectable() {
        return injectable;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        if (ap.isConstant) {
            return new IndexedEntry<>(this, context.region, regionIndex);
        } else {
            throw new UnsupportedOperationException();
            // return new PrototypeInjectorEntry<>(this, context);
        }

    }

    @Override
    public String toString() {
        return "Singleton " + source;
    }
}
