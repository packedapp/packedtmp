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

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.ProvisionContext;
import app.packed.inject.Service;
import app.packed.inject.ServiceLocator;
import packed.internal.inject.PackedProvideContext;
import packed.internal.inject.service.assembly.ServiceAssembly;

/** An entry that represents a service at runtime. */
public abstract class RuntimeService<T> implements Service {

    /** The point where this entry was registered. */
    private final ConfigSite configSite;

    /** The key under which the service is available. */
    private final Key<T> key;

    RuntimeService(ConfigSite configSite, Key<T> key) {
        this.configSite = requireNonNull(configSite);
        this.key = requireNonNull(key);
    }

    /**
     * Creates a new runtime node from a build entry.
     *
     * @param buildEntry
     *            the build node to create the runtime node from
     */
    RuntimeService(ServiceAssembly<T> buildEntry) {
        this(buildEntry.configSite(), buildEntry.key());
    }

    public final ConfigSite configSite() {
        return configSite;
    }

    public T forLocator(ServiceLocator locator) {
        ProvisionContext pc = PackedProvideContext.of(key);
        T t = getInstance(pc);
        return t;
    }

    /**
     * Returns an instance.
     * 
     * @param request
     *            a request if needed by {@link #requiresPrototypeRequest()}
     * @return the instance
     */
    public abstract T getInstance(@Nullable ProvisionContext request);

    @Override
    public abstract boolean isConstant();

    /** {@inheritDoc} */
    @Override
    public final Key<T> key() {
        return key;
    }

    public abstract boolean requiresPrototypeRequest();

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key());
        sb.append("[isConstant=").append(isConstant()).append(']');
        return sb.toString();
    }
}
