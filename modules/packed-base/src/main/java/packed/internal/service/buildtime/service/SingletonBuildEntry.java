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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.component.SourceAssembly;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.InjectionManager;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.runtime.ConstantInjectorEntry;
import packed.internal.service.runtime.RuntimeService;

/**
 *
 */
public final class SingletonBuildEntry<T> extends BuildtimeService<T> {

    /** The singleton source we are wrapping */
    private final SourceAssembly source;

    /**
     * Creates a new node from an instance.
     * 
     * @param im
     *            the injector builder
     */
    @SuppressWarnings("unchecked")
    public SingletonBuildEntry(InjectionManager im, SourceAssembly source) {
        super(im, source.component.configSite());
        this.source = requireNonNull(source);
        this.source.service = this;
        this.key = (Key<T>) source.defaultKey();
        im.provider().buildEntries.add(this);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Injectable injectable() {
        return source.injectable();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        return new ConstantInjectorEntry<>(this, context.region, source.regionIndex);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle toMethodHandle() {
        return source.toMethodHandle();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Singleton " + source;
    }
}
