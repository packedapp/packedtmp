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

import packed.internal.container.ComponentNameWirelet;

/**
 *
 */
public class ContainerWirelets {

    /**
     * Returns a wirelet that will set the name of a container once wired, overriding any name that has previously been set,
     * for example, via {@link Bundle#setName(String)}.
     * 
     * @param name
     *            the name of the container
     * @return a wirelet that will set name of a container once wired
     */
    // setName
    public static Wirelet name(String name) {
        return new ComponentNameWirelet(name);
    }
}
