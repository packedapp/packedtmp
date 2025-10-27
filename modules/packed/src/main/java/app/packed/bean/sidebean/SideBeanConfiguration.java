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
package app.packed.bean.sidebean;

import static java.util.Objects.requireNonNull;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanLifetime;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.bean.scanning.BeanIntrospector.OnVariable;
import app.packed.operation.OperationHandle;
import internal.app.packed.bean.PackedSideBeanUsage;
import internal.app.packed.bean.SideBeanHandle;

/**
 * A side bean configuration object.
 */
public final class SideBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    private final SideBeanHandle sideBeanHandle;

    public SideBeanConfiguration(BeanHandle<?> handle) {
        this.sideBeanHandle = (SideBeanHandle) requireNonNull(handle);
        super(handle);
    }

    public SideBeanConfiguration<T> operationInvoker(Class<?> invoker) {
        throw new UnsupportedOperationException();
    }


    // Hmm, fx for CurrentTime... Vil vi vil bare tilfoeje en til beanen
    // Kunne man bruge den samme til flere beans?
    public SideBeanUseSite addToVariable(OnVariable handle) {
        throw new UnsupportedOperationException();
    }

    public SideBeanUseSite addToOperation(OperationHandle<?> handle) {
        PackedSideBeanUsage usage = new PackedSideBeanUsage.OfOperation(sideBeanHandle, handle);
        sideBeanHandle.usage.add(usage);
        usage.bean.sideBeans.add(usage);
        if (usage.bean.beanKind == BeanLifetime.SINGLETON) {
            usage.lifetimeStoreIndex = usage.bean.container.lifetime.store.add(usage);
        }
        return usage;
    }

    // Kunne man taenke at tilfoeje den flere gange til den samme bean????
    public SideBeanUseSite addToBean(BeanHandle<?> handle) {
        PackedSideBeanUsage usage = new PackedSideBeanUsage.OfBean(sideBeanHandle, handle);
        sideBeanHandle.usage.add(usage);
        usage.bean.sideBeans.add(usage);
        return usage;
    }
}
