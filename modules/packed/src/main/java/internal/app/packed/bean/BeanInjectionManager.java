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
package internal.app.packed.bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.framework.Nullable;
import internal.app.packed.container.ExtensionTreeSetup;
import internal.app.packed.lifetime.LifetimeAccessor;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.operation.OperationSetup;

/**
 * An injection manager for a bean.
 */
public final class BeanInjectionManager {

    /**
     * A dependency node representing a bean instance and its factory method. Or {@code null} for functional beans and other
     * {@code void} beans.
     */
    @Nullable
    private final OperationSetup operation;

    /** A pool accessor if a single instance of this bean is created. null otherwise */
    @Nullable
    public final LifetimeAccessor lifetimePoolAccessor;

    public BeanInjectionManager(BeanSetup bean) {
        // Can only register a single extension bean of a particular type
        if (bean.realm instanceof ExtensionTreeSetup e) {
            if (bean.beanKind == BeanKind.CONTAINER) {
                bean.ownedBy.injectionManager.addBean(bean);
            }
        }
        if (bean.sourceKind == BeanSourceKind.INSTANCE) {
            this.lifetimePoolAccessor = new LifetimeAccessor.ConstantAccessor(bean.source);
        } else if (bean.beanKind == BeanKind.CONTAINER) {
            this.lifetimePoolAccessor = bean.container.lifetime.pool.reserve(bean.beanClass);
        } else if (bean.beanKind == BeanKind.LAZY) {
            throw new UnsupportedOperationException();
        } else {
            this.lifetimePoolAccessor = null;
        }

        OperationSetup os = null;
        // Only create an instance node if we have instances
        if (bean.sourceKind != BeanSourceKind.INSTANCE && bean.beanKind.hasInstances()) {
            os = bean.operations.get(0);
            bean.container.sm.injectionManager.addConsumer(os, lifetimePoolAccessor);
        }
        this.operation = os;
    }

    public MethodHandle accessBean(BeanSetup bean) {
        if (bean.sourceKind == BeanSourceKind.INSTANCE) {
            MethodHandle mh = MethodHandles.constant(bean.source.getClass(), bean.source);
            return MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);
        }
        if (bean.beanKind == BeanKind.CONTAINER) {
            return lifetimePoolAccessor.poolReader(); // MethodHandle(ConstantPool)T
        }
        if (bean.beanKind == BeanKind.MANYTON) {
            return operation.generateMethodHandle(); // MethodHandle(ConstantPool)T
        }
        throw new Error();
    }
}
