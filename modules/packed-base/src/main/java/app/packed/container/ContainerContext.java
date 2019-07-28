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
package app.packed.container;

/**
 *
 */

// Always one per container, not exposed per default....
// Løser også voews problem med interne services vs externe services...
// Saa injector/container er altid externe services.....
interface ContainerContext {

    /**
     * Returns the name of the container.
     * <p>
     * If no name was explicitly when configuring the container, a unique (on a best-effort basis) name was generated.
     *
     * @return the name of this container
     */
    String name();

    // Hmm
    void shutdownApp();

    void shutdownApp(Throwable cause);

    // lots of lifecycle
    // shutdown
    // start
}

// En almindelig Injector, har ikke export/imports....