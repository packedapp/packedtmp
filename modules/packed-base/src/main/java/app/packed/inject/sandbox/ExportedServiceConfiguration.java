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
package app.packed.inject.sandbox;

import app.packed.base.Key;
import app.packed.bundle.BaseAssembly;
import app.packed.inject.ServiceExtension;
import packed.internal.inject.service.sandbox.InjectorAssembler;

/**
 * A configuration object for an exported service. An instance of this interface is usually obtained by calling the
 * various provide or export methods located on {@link ServiceExtension}, {@link InjectorAssembler} or
 * {@link BaseAssembly}.
 */
public interface ExportedServiceConfiguration<T> {

    /**
     * Registers this service under the specified key.
     *
     * @param key
     *            the key for which to register the service under
     * @return this configuration
     * @see #key()
     */
    default ExportedServiceConfiguration<T> as(Class<? super T> key) {
        return as(Key.of(key));
    }

    /**
     * Registers this service under the specified key.
     *
     * @param key
     *            the key for which to register the service under
     * @return this configuration
     * @see #key()
     */
    ExportedServiceConfiguration<T> as(Key<? super T> key);

    /**
     * Returns the key that the service is registered under.
     *
     * @return the key that the service is registered under
     * @see #as(Key)
     * @see #as(Class)
     */
    Key<?> key();
}
