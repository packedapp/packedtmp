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
package app.packed.inject.serviceexpose;

import java.util.Optional;

import app.packed.base.Key;

/**
 *
 */
public interface ServiceConfiguration<T> {

    Key<?> defaultKey();
    
    // Once a bean has been exported, its key cannot be changed...
    ServiceConfiguration<T> export();

    /**
     * Registers this service under the specified key.
     *
     * @param key
     *            the key for which to register the service under
     * @return this configuration
     * @see #key()
     */
    default ServiceConfiguration<T> exportAs(Class<? super T> key) {
        return exportAs(Key.of(key));
    }

    /**
     * Registers this service under the specified key.
     *
     * @param key
     *            the key for which to register the service under
     * @return this configuration
     * @see #key()
     */
    ServiceConfiguration<T> exportAs(Key<? super T> key);

    /** {@return the key that the service is exported under}. */
    Optional<Key<?>> exportedAs();

    ServiceConfiguration<T> provide();

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #provideAs(Key)
     */
    default ServiceConfiguration<T> provideAs(Class<? super T> key) {
        return provideAs(Key.of(key));
    }

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #provideAs(Class)
     */
    ServiceConfiguration<T> provideAs(Key<? super T> key);

    Optional<Key<?>> providedAs();

}
