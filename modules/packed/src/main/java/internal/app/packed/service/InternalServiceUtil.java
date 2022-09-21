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
package internal.app.packed.service;

import java.util.Map;

import app.packed.base.Key;
import internal.app.packed.service.runtime.ServiceRegistry;
import internal.app.packed.service.sandbox.Service;
import internal.app.packed.util.CollectionUtil;

/**
 *
 */
public class InternalServiceUtil {

    /** An empty service registry */
    public static final ServiceRegistry EMPTY = new UnchangeableServiceRegistry(Map.of());

    public static Key<?> checkKey(Class<?> beanClass, Class<?> key) {
        return Key.of(key);
    }

    public static Key<?> checkKey(Class<?> beanClass, Key<?> key) {
        return key;
    }

    /**
     * Creates a new service registry by making an immutable copy of the specified map of services.
     * 
     * @param map
     *            the map to make an immutable copy
     * @return a new service registry
     */
    public static ServiceRegistry copyOf(Map<Key<?>, ? extends Service> map) {
        return new UnchangeableServiceRegistry(CollectionUtil.copyOf(map, b -> b));
    }

    /** The registry implementation returned by {@link #copyOf(Map)}. */
    private record UnchangeableServiceRegistry(Map<Key<?>, Service> asMap) implements ServiceRegistry {

        @Override
        public String toString() {
            return asMap().toString();
        }
    }
}
