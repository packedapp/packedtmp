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
import java.lang.invoke.MethodHandles;

import app.packed.framework.Nullable;
import app.packed.service.Key;
import internal.app.packed.lifetime.LifetimeAccessor;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public final class BuildtimeBeanMemberService implements BuildtimeService {

    /** If constant, the region index to store it in */
    @Nullable
    public final LifetimeAccessor accessor;

    final OperationSetup operation;

    final Key<?> key;

    public BuildtimeBeanMemberService(Key<?> key, OperationSetup operation, LifetimeAccessor accessor) {
        this.key = key;
        this.operation = operation;
        this.accessor = accessor;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle buildInvoker(LifetimeObjectArena context) {
        if (accessor == null) {
            return operation.buildInvoker();
        } else {
            return constant(key, accessor.read(context));
        }
    }

    static MethodHandle constant(Key<?> key, Object constant) {
        MethodHandle mh = MethodHandles.constant(key.rawType(), constant);
        mh = MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);
        return mh;
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return key;
    }
}
