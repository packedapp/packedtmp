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
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.SourceAssembly;
import packed.internal.inject.Injectable;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.runtime.ConstantInjectorEntry;
import packed.internal.service.runtime.PrototypeInjectorEntry;
import packed.internal.service.runtime.RuntimeService;

/** A build entry wrapping a component source. */
public final class ComponentSourceBuildEntry<T> extends BuildtimeService<T> {

    /** The singleton source we are wrapping */
    private final SourceAssembly source;

    /**
     * Creates a new node from an instance.
     * 
     * @param component
     *            the component we provide for
     */
    public ComponentSourceBuildEntry(ComponentNodeConfiguration component, Key<T> key) {
        super(component.injectionManager(), component.configSite());
        this.source = requireNonNull(component.source);
        setKey(requireNonNull(key));
        im.provider().buildEntries.add(this);
    }

    @Override
    public int regionIndex() {
        return source.regionIndex;
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
        if (source.isPrototype()) {
            return new PrototypeInjectorEntry<>(this, context.region, toMethodHandle());
        } else {
            return new ConstantInjectorEntry<>(this, context.region, source.regionIndex);
        }
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
