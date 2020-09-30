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
package packed.internal.sidecar;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Nullable;
import app.packed.sidecar.Invoker;
import packed.internal.component.RuntimeRegion;
import packed.internal.methodhandle.LookupUtil;
import packed.internal.methodhandle.MethodHandleUtil;

/**
 * A implementation of {@link Invoker} that takes a method handle that needs a single {@link RuntimeRegion} to be
 * invoked.
 */
public final class RuntimeRegionInvoker<T> implements Invoker<T> {

    /** A method handle for creating new Invoker instance. We explicitly cast return type from PackedInvoker->Invoker. */
    public static final MethodHandle MH_INVOKER = MethodHandleUtil
            .castReturnType(LookupUtil.lookupConstructor(MethodHandles.lookup(), MethodHandle.class, RuntimeRegion.class), Invoker.class);

    /** The method handle to invoke */
    private final MethodHandle mh;

    /** The region that stores needed data. */
    private final RuntimeRegion region;

    private RuntimeRegionInvoker(MethodHandle mh, RuntimeRegion region) {
        this.mh = requireNonNull(mh);
        this.region = requireNonNull(region);
    }

    /** {@inheritDoc} */
    @Override
    public T invoke() throws Throwable {
        return (T) mh.invoke(region);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public T invokeNullable() throws Throwable {
        return (T) mh.invoke(region);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> returnType() {
        return mh.type().returnType();
    }

    /** {@inheritDoc} */
    @Override
    public void call() throws Throwable {
        mh.invoke(region);
    }
}
