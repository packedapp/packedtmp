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
package packed.internal.service;

import java.util.Map;

import app.packed.base.Key;
import app.packed.inject.service.Service;
import app.packed.inject.service.ServiceRegistry;
import packed.internal.service.build.ServiceSetup;
import packed.internal.util.CollectionUtil;

/** An abstract implementation of ServiceRegistry. */
public abstract class AbstractServiceRegistry implements ServiceRegistry {

    /** An empty service registry */
    public static final ServiceRegistry EMPTY = new UnchangeableServiceRegistry(Map.of());

    /**
     * {@inheritDoc}
     * 
     * @implNote we cannot create default methods for methods in java.lang.Object. So we put it here instead.
     */
    @Override
    public String toString() {
        return asMap().toString();
    }

    /**
     * Creates a new service registry by making an immutable copy of the specified map of services.
     * 
     * @param map
     *            the map to make an immutable copy
     * @return a new service registry
     */
    public static ServiceRegistry copyOf(Map<Key<?>, ? extends ServiceSetup> map) {
        return new UnchangeableServiceRegistry(CollectionUtil.copyOf(map, b -> b.exposeAsService()));
    }

    /** The registry implementation returned by {@link #copyOf(Map)}. */
    private static final class UnchangeableServiceRegistry extends AbstractServiceRegistry {

        /** The services that the registry contains. */
        private final Map<Key<?>, Service> services;

        /**
         * Creates a new unchangeable service registry.
         * 
         * @param services
         *            the services that make of the registry
         */
        private UnchangeableServiceRegistry(Map<Key<?>, Service> services) {
            this.services = Map.copyOf(services);
        }

        /** {@inheritDoc} */
        @Override
        public Map<Key<?>, Service> asMap() {
            return services; // the returned map is immutable
        }
    }
}
