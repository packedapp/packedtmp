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
package app.packed.element;

/**
 * 
 * 
 * A component relation is a snapshot of how things are.
 * 
 * C
 * 
 */

// Problemet er at vi vil have en snapshot...
// Vi skal ikke have noget live...
// Maaske af det derfor

// Special cases

// Not In same system
// -> distance = -1, inSameContainer=false, isStronglyBound=false, isWeaklyBound=false

// Same component
// -> distance = 0, inSameContainer=true, isStrongBound=?, isWeaklyBound=false
public interface ComponentRelation extends Iterable<Component> {

    Component from();

    Component to();

    int distance();

    // The container itself and
    boolean isInSameContainer();

    boolean isStronglyBound();

    boolean isWeaklyBound();

    // https://en.wikipedia.org/wiki/Tree_structure
    // description()? -> Same, parent, child, descendend, ancestor,
}

interface Dddd {

    default void fff(Component c2, Component c3) {
        if (c2.relationTo(c3).isStronglyBound()) {

        }
    }
}

//interface InterExtensionRelationship {
// from, to
// isX
// distance
//}