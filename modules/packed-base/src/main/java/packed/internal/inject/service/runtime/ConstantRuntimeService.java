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
import java.lang.invoke.MethodHandles;

import app.packed.base.Key;
import app.packed.inject.ProvisionContext;
import app.packed.inject.ServiceMode;
import packed.internal.component.ConstantPool;
import packed.internal.inject.service.build.ServiceSetup;

/** An runtime service holding a constant. */
public final class ConstantRuntimeService extends RuntimeService {

    /** The constant the runtime service is holding. */
    private final Object constant;

    /** The key under which the service is available. */
    private final Key<?> key;

    /**
     * @param key
     */
    public ConstantRuntimeService(Key<?> key, Object constant) {
        this.key = requireNonNull(key);
        this.constant = requireNonNull(constant);
    }

    /**
     * Creates a new entry.
     *
     * @param service
     *            the build entry to create this entry from
     */
    public ConstantRuntimeService(ServiceSetup service, ConstantPool pool, int poolIndex) {
        this.key = requireNonNull(service.key());
        this.constant = requireNonNull(pool.getSingletonInstance(poolIndex));
    }
    
    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return MethodHandles.constant(key().rawType(), constant);
    }

    /** {@inheritDoc} */
    @Override
    public Object getInstance(ProvisionContext ignore) {
        return constant;
    }

    /** {@inheritDoc} */
    @Override
    public final Key<?> key() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode mode() {
        return ServiceMode.CONSTANT;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresProvisionContext() {
        return false;
    }
}
