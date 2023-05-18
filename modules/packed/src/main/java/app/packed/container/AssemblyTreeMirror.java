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

import internal.app.packed.container.TreeMirror;

/**
 * Represents a collection of assemblies that are ordered in a rooted tree.
 * <p>
 * This
 */

// Multi app.
// application.assemblies() All assemblies that make of the application. Child applications not included.

// application.tree().assemblies() <--- Application tree for assemblies

public interface AssemblyTreeMirror extends TreeMirror<AssemblyMirror> {

    /**
     *
     */
    void print();

}
