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
package packed.internal.inject.service.runtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.inject.ServiceMode;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.invoke.constantpool.ConstantPool;
import packed.internal.util.ThrowableUtil;

/** A runtime service node for prototypes. */
public final class PrototypeRuntimeService implements RuntimeService {

    /** The key under which the service is available. */
    private final Key<?> key;

    /** The method handle used to create new instances. */
    private final MethodHandle mh; // (ConstantPool)Object

    /** The Constant pool used when creating new service instances. */
    private final ConstantPool pool;

    /**
     * @param service
     */
    public PrototypeRuntimeService(ServiceSetup service, ConstantPool region, MethodHandle mh) {
        this.key = service.key();
        this.pool = requireNonNull(region);
        this.mh = requireNonNull(mh);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return mh.bindTo(pool);
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode mode() {
        return ServiceMode.TRANSIENT;
    }

    /** {@inheritDoc} */
    @Override
    public Object provideInstance() {
        try {
            return mh.invoke(pool);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresProvisionContext() {
        return false;
    }

    @Override
    public String toString() {
        return RuntimeService.toString(this);
    }
}
