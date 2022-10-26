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
package internal.app.packed.oldservice.build;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.oldservice.inject.DependencyNode;
import internal.app.packed.oldservice.runtime.PrototypeRuntimeService;
import internal.app.packed.oldservice.runtime.RuntimeService;
import internal.app.packed.oldservice.runtime.ServiceInstantiationContext;

/** A entry wrapping a component source. */
public final class BeanInstanceServiceSetup extends ServiceSetup {

    /** The singleton source we are wrapping */
    public final BeanSetup bean;

    /**
     * Creates a new node from an instance.
     * 
     * @param bean
     *            the component we provide for
     */
    public BeanInstanceServiceSetup(BeanSetup bean, Key<?> key) {
        super(key);
        this.bean = requireNonNull(bean);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public DependencyNode dependencyConsumer() {
        return bean.injectionManager.dependencyConsumer();
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return bean.injectionManager.dependencyAccessor();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return bean.injectionManager.singletonAccessor != null;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        if (isConstant()) {
            return RuntimeService.constant(key(), bean.injectionManager.singletonAccessor.read(context.pool));
        } else {
            return new PrototypeRuntimeService(this, context.pool, dependencyAccessor());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Singleton " + bean;
    }
}
