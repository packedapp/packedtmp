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
package packed.internal.inject.service.assembly;

import static java.util.Objects.requireNonNull;

import app.packed.base.AttributeMap;
import app.packed.base.Key;
import app.packed.config.ConfigSite;
import app.packed.inject.Service;

/** An implementation of {@link Service} because {@link ServiceAssembly} is not immutable. */
public final class PackedService implements Service {

    /** The configuration site of the service. */
    private final ConfigSite configSite;

    /** The key of the service. */
    private final Key<?> key;

    private final boolean isConstant;

    /**
     * Creates a new descriptor.
     * 
     * @param key
     *            the key of the service
     * @param configSite
     *            the config site of the service
     */
    public PackedService(Key<?> key, ConfigSite configSite, boolean isConstant) {
        this.key = requireNonNull(key);
        this.configSite = requireNonNull(configSite);
        this.isConstant = isConstant;
    }

    @Override
    public AttributeMap attributes() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ServiceDescriptor[key=" + key + ", configSite=" + configSite + "]";
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return isConstant;
    }
}
