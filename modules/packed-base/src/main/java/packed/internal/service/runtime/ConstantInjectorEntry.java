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
import app.packed.service.ProvideContext;
import app.packed.service.ServiceMode;
import packed.internal.service.build.BuildEntry;

/** An entry holding a constant. */
// Can't implement both ServiceDescriptor and Provider...
public final class ConstantInjectorEntry<T> extends InjectorEntry<T> {

    /** The singleton instance. */
    @Nullable
    private final T constant;

    /**
     * Creates a new entry.
     *
     * @param entry
     *            the build entry to create this entry from
     * @param constant
     *            the singleton instance
     */
    public ConstantInjectorEntry(BuildEntry<T> entry, @Nullable T constant) {
        super(entry);
        this.constant = requireNonNull(constant);
    }

    /**
     * @param configSite
     * @param key
     * @param description
     */
    public ConstantInjectorEntry(ConfigSite configSite, Key<T> key, @Nullable String description, @Nullable T instance) {
        super(configSite, key, description);
        this.constant = requireNonNull(instance);
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideContext ignore) {
        return constant;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode instantiationMode() {
        return ServiceMode.SINGLETON;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }
}
