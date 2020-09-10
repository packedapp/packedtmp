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

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.SourceAssembly;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.runtime.IndexedEntry;
import packed.internal.service.runtime.RuntimeEntry;

/**
 *
 */
public class ProvideBuildEntry<T> extends BuildtimeService<T> {

    final SourceAssembly source;

    final AtProvides ap;

    public final Injectable injectable;

    public final int regionIndex;

    /**
     * Creates a new node from an instance.
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ProvideBuildEntry(ConfigSite configSite, ComponentNodeConfiguration component, AtProvides ap) {
        super(component.container.im, configSite);
        this.source = component.source;
        this.ap = ap;
        // if singleton reserve... protype no...
        this.regionIndex = component.region.reserve();
        this.injectable = Injectable.ofDeclaredMember(this, source, ap);
        as((Key) ap.key);
        if (ap.isConstant) {
            // sa.component.region.resolver.sourceInjectables
            // Constants should be stored
        }
        component.container.im.provider().buildEntries.add(this);
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
