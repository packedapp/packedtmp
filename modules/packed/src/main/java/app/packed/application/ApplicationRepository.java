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
package app.packed.application;

import java.util.Optional;
import java.util.stream.Stream;

import internal.app.packed.application.RuntimeApplicationRepository;

/**
 *
 * <p>
 * For now, we don't track instances here. We need some else for this
 */
public sealed interface ApplicationRepository<H extends ApplicationHandle<?,?>> permits RuntimeApplicationRepository {

    Optional<H> get(String name);

    /** {@return whether or not applications can be added or removed from the repository} */
    default boolean isReadOnly() {
        return true;
    }

    void remove(String name);

    int size();

    /** {@return a stream of all applications that have been installed into the repository} */
    Stream<H> stream();

    // An application can be, NA, INSTALLING, AVAILABLE
    // Don't know if we at runtime
}
