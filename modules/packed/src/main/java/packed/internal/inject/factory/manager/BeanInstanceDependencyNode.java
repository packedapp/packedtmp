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
package packed.internal.inject.factory.manager;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.Nullable;
import packed.internal.bean.BeanSetup;
import packed.internal.inject.factory.bean.DependencyNode;
import packed.internal.inject.factory.bean.DependencyProducer;
import packed.internal.inject.factory.bean.InternalDependency;
import packed.internal.lifetime.PoolEntryHandle;

/**
 *
 */
// Vi laver en hvis vi har bean instanser...
public final class BeanInstanceDependencyNode extends DependencyNode {

    final BeanInjectionManager bim;

    public BeanInstanceDependencyNode(BeanSetup bean, BeanInjectionManager bim, List<InternalDependency> dependencies, MethodHandle mh) {
        super(bean, dependencies, mh, new DependencyProducer[mh.type().parameterCount()]);
        this.bim = bim;
    }

    @Nullable
    protected PoolEntryHandle poolAccessor() {
        return bim.singletonHandle;
    }
}
