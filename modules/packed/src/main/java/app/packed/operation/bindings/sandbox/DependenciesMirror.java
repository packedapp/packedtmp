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
package app.packed.operation.bindings.sandbox;

import java.util.Collection;
import java.util.Set;

import app.packed.bean.BeanMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.operation.OperationMirror;
import internal.app.packed.container.Mirror;

/**
 *
 */
// Ideen er lidt at vi har en form for element der kan have dependencies.

// Fx hvad depender en bean paa
// Fx hvad depender en operation paa
// Fx hvad depender en Binding paa

// DirectDependencies only

// Ved ikke om den giver mening...
// Eller om vi kun skal have services?
// I 9/10 tilfaelde er jeg ligeglade med dependencies ppa
// extension beans...
public interface DependenciesMirror extends Mirror {

    Collection<BeanMirror> beans();

    default Collection<BeanMirror> beans(boolean includeSynthetic, boolean includeExtensiosn) {
        return null;
    }

    // if it creates dependencies on other containers
    Collection<ContainerMirror> containers();

    Set<Class<? extends Extension<?>>> extensions();

    /** {@return whether or not there are any dependencies.} */
    boolean isEmpty();

    /** {@return a collection of all operations that creates a dependency.} */
    Collection<OperationMirror> operations();
}
