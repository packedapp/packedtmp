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
package app.packed.component;

import java.util.Optional;

/**
 * 
 * 
 * A component relation is a snapshot of how things are.
 * 
 * C
 * 
 */

// Problemet er at vi vil have en snapshot...
// Tager vi snapshot af attributer??? Nej det goer vi altsaa ikke...
// Rimlig meget overhead

// Tror topol

// Vi skal ikke have noget live...
// Maaske af det derfor

// Special cases

// Not In same system
// -> distance = -1, inSameContainer=false, isStronglyBound=false, isWeaklyBound=false

// Same component
// -> distance = 0, inSameContainer=true, isStrongBound=?, isWeaklyBound=false

// Teanker vi flytter den et andet sted end attributer...

// Altsaa den walk man kan lave via Iterable... ville det vaere rart at kunne beskrive relationsship
// for den.. Altsaa cr[4] er foraeldre til cr[3] og saa 

// https://en.wikipedia.org/wiki/Path_(graph_theory)
public interface ComponentRelation extends Iterable<Component> {

    /**
     * -1 if they are not in the same system. 0 if identical
     * 
     * @return the distance between the two components
     */
    int distance();

    Component from();

    /**
     * Returns whether or not the two components are in the same container.
     * 
     * @return whether or not the two components are in the same container
     */
    // ContainerRelations.isInSameContainer(rel);
    // from().attributes.get(container) == to().attributes.get(container);
    boolean isInSameContainer();

    /**
     * Returns whether or not the two components are in the same system.
     * 
     * @return whether or not the two components are in the same system
     */
    boolean isInSameSystem();

    // The container itself and

    boolean isStronglyConnected();

    default WiringStrength wiringStrength() {
        return isStronglyConnected() ? WiringStrength.STRONG : WiringStrength.WEAK;
    }

    boolean isWeaklyConnected();

    // is empty for components that are not in the same system
    /**
     * The lowest common ancestor for the two components. Or {@link Optional#empty()} if not in the same system.
     * 
     * @return lowest common ancestor for the two components
     */
    Optional<Component> lowestCommonAncestor();

    Component to();

    // https://en.wikipedia.org/wiki/Tree_structure
    // description()? -> Same, parent, child, descendend, ancestor,
}

//interface InterExtensionRelationship {
// from, to
// isX
// distance
//}