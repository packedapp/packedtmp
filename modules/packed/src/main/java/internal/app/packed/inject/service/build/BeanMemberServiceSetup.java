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
package internal.app.packed.inject.service.build;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.inject.DependencyNode;
import internal.app.packed.inject.service.ContainerInjectionManager;
import internal.app.packed.inject.service.runtime.PrototypeRuntimeService;
import internal.app.packed.inject.service.runtime.RuntimeService;
import internal.app.packed.inject.service.runtime.ServiceInstantiationContext;
import internal.app.packed.lifetime.pool.PoolEntryHandle;

/**
 *
 */
public final class BeanMemberServiceSetup extends ServiceSetup {

    private final DependencyNode consumer;

    /** If constant, the region index to store it in */
    @Nullable
    public final PoolEntryHandle accessor;

    public BeanMemberServiceSetup(ContainerInjectionManager im, ComponentSetup compConf, DependencyNode dependant, Key<?> key, boolean isConst) {
        super(key);
        this.consumer = requireNonNull(dependant);
        // TODO fix Object
        this.accessor = isConst ? compConf.lifetime.pool.reserve(Object.class) : null;
    }

    /** {@inheritDoc} */
    @Override
    public DependencyNode dependencyConsumer() {
        return consumer;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return consumer.runtimeMethodHandle();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return accessor != null;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        if (isConstant()) {
            return RuntimeService.constant(key(), accessor.read(context.pool));
        } else {
            return new PrototypeRuntimeService(this, context.pool, dependencyAccessor());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "@Provide " + consumer.originalMethodHandle;
    }
}
