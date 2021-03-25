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
package packed.internal.inject.service.build;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.component.ComponentSetup;
import packed.internal.component.source.SourceClassSetup;
import packed.internal.inject.Dependant;
import packed.internal.inject.service.ServiceManagerSetup;
import packed.internal.inject.service.runtime.ConstantRuntimeService;
import packed.internal.inject.service.runtime.PrototypeRuntimeService;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;

/** A build entry wrapping a component source. */
public final class SourceInstanceBuildtimeService extends BuildtimeService {

    /** The singleton source we are wrapping */
    private final SourceClassSetup source;

    /**
     * Creates a new node from an instance.
     * 
     * @param compConf
     *            the component we provide for
     */
    public SourceInstanceBuildtimeService(ServiceManagerSetup im, ComponentSetup compConf, Key<?> key) {
        super(key);
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
        return source.poolIndex > -1;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        if (isConstant()) {
            return new ConstantRuntimeService(this, context.region, source.poolIndex);
        } else {
            return new PrototypeRuntimeService(this, context.region, dependencyAccessor());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Singleton " + source;
    }
}
