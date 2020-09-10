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
package packed.internal.service.runtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.inject.ProvidePrototypeContext;
import app.packed.inject.ProvisionException;
import packed.internal.component.Region;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.util.ThrowableUtil;

/** A runtime service node for prototypes. */
public class PrototypeInjectorEntry<T> extends RuntimeService<T> {

    /** The method handle used to create new instances. */
    private final MethodHandle mh;

    /** The region used when creating new instances. */
    private final Region region;

    /**
     * @param service
     */
    public PrototypeInjectorEntry(BuildtimeService<T> service, Region region, MethodHandle mh) {
        super(service);
        this.region = requireNonNull(region);
        this.mh = requireNonNull(mh);
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvidePrototypeContext site) {
        try {
            return (T) mh.invoke(region);
        } catch (Throwable e) {
            ThrowableUtil.throwIfUnchecked(e);
            throw new ProvisionException("Failed to inject ", e);
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
