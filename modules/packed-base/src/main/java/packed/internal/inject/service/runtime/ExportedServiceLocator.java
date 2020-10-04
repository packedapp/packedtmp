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

import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.Service;
import packed.internal.component.ComponentNode;

/**
 *
 */
public final class ExportedServiceLocator extends AbstractServiceLocator {

    private final ComponentNode component;

    /** All services that this injector provides. */
    private final Map<Key<?>, RuntimeService<?>> services;

    public ExportedServiceLocator(ComponentNode component, Map<Key<?>, RuntimeService<?>> services) {
        this.services = requireNonNull(services);
        this.component = requireNonNull(component);
    }

    @Override
    protected String failedToUseMessage(Key<?> key) {
        // /child [ss.BaseMyBundle] does not export a service with the specified key

        // FooBundle does not export a service with the key
        // It has an internal service. Maybe you forgot to export it()
        // Is that breaking encapsulation

        return "'" + component.path() + "' does not export a service with the specified key, key = " + key;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    protected <T> RuntimeService<T> getService(Key<T> key) {
        return (RuntimeService<T>) services.get(key);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Map<Key<?>, Service> services() {
        return (Map) services;
    }
}
