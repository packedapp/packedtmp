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
package app.packed.bean.hooks.sandboxinvoke.mirror;

import java.util.List;

import app.packed.base.Variable;

/**
 *
 */

// Modellere en dependecy (typisk en parameter)
// Dependecy arguments (wrapped 

public sealed interface Dependency permits ResolvedDependency, UnresolvedDependency {
    // Den er for composites skyld... Men hvad er forskellen paa at lave en bean som
    // Men er det i virkeligheden bare en dynamisk producer??? Composite
    // For hvad er forskellen egentlig til en prototype (record) bean????
    List<Dependency> dependencies();

    Class<?> getExpectedType();

    // Maaske er composite en dependency type???
    // Altsaa det er jo ikke en bean... Man kan ikke have annoteringer...

    /**
     * {@return whether or not the dependency is satisfiable
     */
    boolean isSatisfiable(); // isSatisfied

    Variable variable();
}

//Altsaa det er goer det jo lidt traels...
// Eller maaske resolved dependency istedet for????
interface ZCompositeDependency /* extends Dependency */ {

}
