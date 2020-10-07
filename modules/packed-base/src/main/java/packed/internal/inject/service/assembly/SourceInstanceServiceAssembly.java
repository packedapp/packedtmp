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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.SourceAssembly;
import packed.internal.inject.Dependant;
import packed.internal.inject.service.ServiceBuildManager;
import packed.internal.inject.service.runtime.ConstantRuntimeService;
import packed.internal.inject.service.runtime.PrototypeInjectorEntry;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;

/** A build entry wrapping a component source. */
public final class SourceInstanceServiceAssembly<T> extends ServiceAssembly<T> {

    /** The singleton source we are wrapping */
    private final SourceAssembly source;

    /**
     * Creates a new node from an instance.
     * 
     * @param compConf
     *            the component we provide for
     */
    public SourceInstanceServiceAssembly(ServiceBuildManager im, ComponentNodeConfiguration compConf, Key<T> key) {
        super(im, compConf.configSite(), key);
        this.source = requireNonNull(compConf.source);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Dependant dependant() {
        return source.dependant();
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return source.dependencyAccessor();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return source.regionIndex > -1;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService<T> newRuntimeNode(ServiceInstantiationContext context) {
        if (isConstant()) {
            return new ConstantRuntimeService<>(this, context.region, source.regionIndex);
        } else {
            return new PrototypeInjectorEntry<>(this, context.region, dependencyAccessor());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Singleton " + source;
    }
}
