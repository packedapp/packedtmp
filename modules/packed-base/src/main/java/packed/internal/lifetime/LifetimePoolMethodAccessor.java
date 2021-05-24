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
package packed.internal.lifetime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.hooks.accessors.MethodAccessor;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;

/**
 * A implementation of {@link MethodAccessor} that takes a method handle that needs a single {@link LifetimePool} to be
 * invoked.
 */
public record LifetimePoolMethodAccessor<T> (/** The method handle to invoke */
MethodHandle mh, /** The constant pool that stores needed data. */
LifetimePool pool) implements MethodAccessor<T> {

    /**
     * A method handle for creating new RuntimeRegionInvoker instance. We explicitly cast return type from
     * ConstantPoolMethodAccessor->MethodAccessor.
     */
    public static final MethodHandle MH_INVOKER = MethodHandleUtil
            .castReturnType(LookupUtil.lookupConstructor(MethodHandles.lookup(), MethodHandle.class, LifetimePool.class), MethodAccessor.class);

    public LifetimePoolMethodAccessor {
        requireNonNull(mh);
        requireNonNull(pool);
    }

    /** {@inheritDoc} */
    @Override
    public void call() throws Throwable {
        // I think we might need one void() and no arguments...
        // We cannot do invoke exact her
        mh.invoke(pool);
    }

    /** {@inheritDoc} */
    @Override
    public T invoke() throws Throwable {
        return (T) mh.invoke(pool);
    }
}
