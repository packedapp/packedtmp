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
package internal.app.packed.service.old;

import java.lang.invoke.MethodHandle;

import internal.app.packed.lifetime.pool.LifetimeAccessor;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
class DependencyNode {

    public final LifetimeAccessor la;

    public final OperationSetup operation;

    public DependencyNode(OperationSetup operation, LifetimeAccessor la) {
        this.operation = operation;
        this.la = la;
        if (la != null) {
            operation.bean.container.lifetime.pool.addOrdered(p -> {
                MethodHandle mh = operation.buildInvoker();

                Object instance;
                try {
                    instance = mh.invoke(p);
                } catch (Throwable e) {
                    throw ThrowableUtil.orUndeclared(e);
                }

                if (instance == null) {
                    throw new NullPointerException(this + " returned null");
                }

                la.store(p, instance);

            });
        }
    }
}
