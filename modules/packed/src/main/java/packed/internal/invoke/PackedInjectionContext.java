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
package packed.internal.invoke;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import app.packed.base.Key;
import app.packed.inject.InjectionContext;
import app.packed.inject.service.Service;
import packed.internal.service.AbstractServiceRegistry;

/** Implementation of {@link InjectionContext}. */
public final class PackedInjectionContext extends AbstractServiceRegistry implements InjectionContext {

    /** All services that available for injection. */
    private final Map<Key<?>, Service> services;

    private final Class<?> target;

    public PackedInjectionContext(Class<?> target, Map<Key<?>, Service> services) {
        this.target = requireNonNull(target);
        this.services = requireNonNull(services);
    }

    /** {@inheritDoc} */
    @Override
    public Map<Key<?>, Service> asMap() {
        return services;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> targetClass() {
        return target;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "InjectionContext[" + target.getCanonicalName() + "]: " + services.keySet();
    }
}
