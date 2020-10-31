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
package packed.internal.inject.service;

import java.util.Map;

import app.packed.base.Key;
import app.packed.inject.Service;
import app.packed.inject.ServiceRegistry;
import packed.internal.inject.service.build.BuildtimeService;
import packed.internal.util.CollectionUtil;

/** An abstract implementation of ServiceRegistry. */
public abstract class AbstractServiceRegistry implements ServiceRegistry {

    /** An empty service registry */
    public static final ServiceRegistry EMPTY = new ImmutableRegistry(Map.of());

    /** {@inheritDoc} */
    @Override
    // cannot create default methods for methods in java.lang.Object.
    public String toString() {
        return asMap().toString();
    }

    /**
     * Creates a new service registry by making an immutable copy of the specified service map.
     * 
     * @param map
     *            the map to make an immutable copy
     * @return a new service registry
     */
    public static ServiceRegistry copyOf(Map<Key<?>, ? extends BuildtimeService> map) {
        return new ImmutableRegistry(CollectionUtil.copyOf(map, b -> b.toService()));
    }

    /** The registry implementation returned by {@link #copyOf(Map)}. */
    private static final class ImmutableRegistry extends AbstractServiceRegistry {

        /** The services that are wrapped */
        private final Map<Key<?>, Service> services;

        /**
         * Creates a new immutable service registry.
         * 
         * @param services
         *            the services that make of the registry
         */
        private ImmutableRegistry(Map<Key<?>, Service> services) {
            this.services = Map.copyOf(services);
        }

        /** {@inheritDoc} */
        @Override
        public Map<Key<?>, Service> asMap() {
            return services; // services is immutable
        }
    }
}
