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
public final class SingletonBuildEntry<T> extends BuildEntry<T> {

    public final SourceAssembly source;

    /**
     * Creates a new node from an instance.
     * 
     * @param im
     *            the injector builder
     */
    @SuppressWarnings("unchecked")
    public SingletonBuildEntry(InjectionManager im, SourceAssembly source) {
        super(im, source.component.configSite());
        this.source = source;
        as((Key<T>) source.defaultKey());
        im.provider().buildEntries.add(this);
    }

    @Override
    @Nullable
    public Injectable injectable() {
        return source.injectable();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        return new IndexedEntry<>(this, context.region, source.regionIndex);
    }

    @Override
    public MethodHandle toMethodHandle() {
        return source.toMethodHandle();
    }

    @Override
    public String toString() {
        return "Singleton " + source;
    }
}
