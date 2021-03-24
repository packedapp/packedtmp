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
import packed.internal.component.ComponentSetup;
import packed.internal.inject.Dependant;
import packed.internal.inject.service.ServiceManagerSetup;
import packed.internal.inject.service.runtime.ConstantRuntimeService;
import packed.internal.inject.service.runtime.PrototypeRuntimeService;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;

/**
 *
 */
public class SourceMemberBuildtimeService extends BuildtimeService {

    private final Dependant dependant;

    /** If constant, the region index to store it in */
    public final int regionIndex;

    public SourceMemberBuildtimeService(ServiceManagerSetup im, ComponentSetup compConf, Dependant dependant, Key<?> key, boolean isConst) {
        super(key);
        this.dependant = requireNonNull(dependant);
        this.regionIndex = isConst ? compConf.slotTable.reserve() : -1;
    }

    /** {@inheritDoc} */
    @Override
    public Dependant dependant() {
        return dependant;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return dependant.buildMethodHandle();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return regionIndex > -1;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        if (isConstant()) {
            return new ConstantRuntimeService(this, context.region, regionIndex);
        } else {
            return new PrototypeRuntimeService(this, context.region, dependencyAccessor());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "@Provide " + dependant.directMethodHandle;
    }
}
