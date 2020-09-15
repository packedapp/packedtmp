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

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.ProvideContext;
import packed.internal.component.RuntimeRegion;
import packed.internal.service.buildtime.BuildtimeService;

/** An entry holding a constant. */
public final class ConstantInjectorEntry<T> extends RuntimeService<T> {

    /** The singleton instance. */
    private final T constant;

    /**
     * Creates a new entry.
     *
     * @param service
     *            the build entry to create this entry from
     */
    @SuppressWarnings("unchecked")
    public ConstantInjectorEntry(BuildtimeService<T> service, RuntimeRegion region, int index) {
        super(service);
        this.constant = requireNonNull((T) region.getSingletonInstance(index));
    }

    /**
     * @param configSite
     * @param key
     */
    public ConstantInjectorEntry(ConfigSite configSite, Key<T> key, @Nullable T instance) {
        super(configSite, key);
        this.constant = requireNonNull(instance);
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideContext ignore) {
        return constant;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }
}
