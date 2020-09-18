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
package packed.internal.service.buildtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.SourceAssembly;
import packed.internal.inject.Injectable;
import packed.internal.service.InjectionManager;
import packed.internal.service.runtime.ConstantInjectorEntry;
import packed.internal.service.runtime.PrototypeInjectorEntry;
import packed.internal.service.runtime.RuntimeService;
import packed.internal.service.runtime.ServiceInstantiationContext;

/** A build entry wrapping a component source. */
public final class ComponentSourceBuildEntry<T> extends BuildtimeService<T> {

    /** The singleton source we are wrapping */
    private final SourceAssembly source;

    /**
     * Creates a new node from an instance.
     * 
     * @param compConf
     *            the component we provide for
     */
    public ComponentSourceBuildEntry(InjectionManager im, ComponentNodeConfiguration compConf, Key<T> key) {
        super(im, compConf.configSite(), key);
        this.source = requireNonNull(compConf.source);
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
    protected RuntimeService<T> newRuntimeNode(ServiceInstantiationContext context) {
        if (regionIndex() > -1) {
            return new ConstantInjectorEntry<>(this, context.region, source.regionIndex);
        } else {
            return new PrototypeInjectorEntry<>(this, context.region, dependencyAccessor());
        }
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return source.dependencyAccessor();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Singleton " + source;
    }
}
