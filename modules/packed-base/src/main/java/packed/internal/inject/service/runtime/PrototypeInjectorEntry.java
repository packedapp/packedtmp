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

import app.packed.inject.ProvisionContext;
import packed.internal.component.RuntimeRegion;
import packed.internal.inject.service.assembly.ServiceAssembly;
import packed.internal.util.ThrowableUtil;

/** A runtime service node for prototypes. */
public class PrototypeInjectorEntry<T> extends RuntimeService<T> {

    /** The method handle used to create new instances. */
    private final MethodHandle mh;

    /** The region used when creating new instances. */
    private final RuntimeRegion region;

    /**
     * @param service
     */
    public PrototypeInjectorEntry(ServiceAssembly<T> service, RuntimeRegion region, MethodHandle mh) {
        super(service);
        this.region = requireNonNull(region);
        this.mh = requireNonNull(mh);
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvisionContext site) {
        try {
            return (T) mh.invoke(region);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }
}
