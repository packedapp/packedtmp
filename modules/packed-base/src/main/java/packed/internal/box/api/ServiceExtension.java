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
package packed.internal.box.api;

import static java.util.Objects.requireNonNull;

import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;

/**
 *
 */
public abstract class ServiceExtension {

    /**
     * Adds the specified key to the list of optional services.
     * 
     * @param key
     *            the key to add
     */
    public abstract void addOptional(Key<?> key);

    /**
     * Adds the specified key to the list of required services.
     * 
     * @param key
     *            the key to add
     */
    public abstract void addRequired(Key<?> key);

    public final <T> ServiceConfiguration<T> export(Class<T> key) {
        return export(Key.of(requireNonNull(key, "key is null")));
    }

    /**
     * Exposes an internal service outside of this bundle.
     * 
     * 
     * <pre> {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class);}
     * </pre>
     * 
     * You can also choose to expose a service under a different key then what it is known as internally in the
     * <pre> {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service.class);}
     * </pre>
     * 
     * @param <T>
     *            the type of the exposed service
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #export(Key)
     */
    protected abstract <T> ServiceConfiguration<T> export(Key<T> key);

    protected abstract <T> ServiceConfiguration<T> export(ServiceConfiguration<T> configuration);
}
