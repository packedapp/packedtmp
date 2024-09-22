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
package app.packed.service;

import static java.util.Objects.requireNonNull;

import app.packed.binding.Key;
import app.packed.container.Wirelet;

/**
 * Various wirelets that applies to {@link BaseExtension}.
 */
public class ServiceWirelets2 {

    // Double Provide overrides, Double Provide fails
    public static <T> Wirelet provideInstance(Class<T> key, T instance) {
        return provideInstance(Key.of(key), instance);
    }

    /**
     * Returns a wirelet that will provide the specified instance to the target container. Iff the target container has a
     * service of the specific type as a requirement.
     * <p>
     * Invoking this method is identical to invoking {@code to(t -> t.provideInstance(instance))}.
     *
     * @param instance
     *            the service to provide
     * @return a wirelet that will provide the specified service
     */
    public static <T> Wirelet provideInstance(Key<T> key, T instance) {
        requireNonNull(key, "key is null");
        requireNonNull(instance, "instance is null");
        throw new UnsupportedOperationException();
//        return transformRequirements(t -> t.provideInstance(key, instance));
    }
}
