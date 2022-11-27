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

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.framework.Nullable;
import internal.app.packed.container.ExtensionTreeSetup;
import internal.app.packed.lifetime.LifetimeAccessor;
import internal.app.packed.lifetime.LifetimeAccessor.DynamicAccessor;
import internal.app.packed.operation.binding.BindingResolutionSetup;
import internal.app.packed.operation.binding.BindingResolutionSetup.ConstantResolution;
import internal.app.packed.operation.binding.BindingResolutionSetup.LifetimePoolResolution;
import internal.app.packed.operation.binding.BindingResolutionSetup.OperationResolution;

/**
 * An injection manager for a bean.
 */
public final class BeanInjectionManager {

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

        // Only create an instance node if we have instances
        if (bean.sourceKind != BeanSourceKind.INSTANCE && bean.beanKind.hasInstances()) {
            bean.container.sm.injectionManager.addConsumer(bean.operations.get(0), lifetimePoolAccessor);
        }
    }

    public BindingResolutionSetup accessBeanX(BeanSetup bean) {
        if (bean.sourceKind == BeanSourceKind.INSTANCE) {
            return new ConstantResolution(bean.source.getClass(), bean.source);
        } else if (bean.beanKind == BeanKind.CONTAINER) {
            return new LifetimePoolResolution((DynamicAccessor) lifetimePoolAccessor);
        } else if (bean.beanKind == BeanKind.MANYTON) {
            return new OperationResolution(bean.operations.get(0));
        }
        throw new Error();
    }
}
