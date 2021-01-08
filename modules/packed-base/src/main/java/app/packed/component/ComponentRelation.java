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
 * A component relation is an immutable representation of a relationship between two components. It is typically created
 * via {@link Component#relationTo(Component)}.
 */
public interface ComponentRelation extends Iterable<Component> {

    /**
     * -1 if {@link #source()} and {@link #target()} are not in the same system. 0 if source and target are identical.
     * 
     * @return the distance between the two components
     */
    int distance();

    /**
     * Finds the lowest common ancestor for the two components if they are in the same system. Or {@link Optional#empty()}
     * if not in the same system.
     * 
     * @return lowest common ancestor for the two components. Or empty if not in the same system
     */
    Optional<Component> findLowestCommonAncestor();

    /**
     * Returns whether or not the two components are in the same container.
     * 
     * @return whether or not the two components are in the same container
     */
    boolean inSameContainer();

    /**
     * Whether or not {@link #source()} and {@link #target()} belongs to the same guest. Or in other words whether or not
     * they are strongly connected.
     * 
     * @return whether or not source and target belongs to the same guest
     */
    // Maaske har vi kun de to andre...
    boolean inSameGuest();

    /**
     * Returns whether or not the two components are in the same system. Two components are in the same system, iff they
     * have the same {@link Component#root() system component}.
     * 
     * @return whether or not the two components are in the same system
     * @see Component#root()
     */
    default boolean isInSameNamespace() {
        return isInSame(ComponentSystemType.NAMESPACE);
    }

    default boolean isInSame(ComponentSystemType systemType) {
        return source().root() == target().root();
    }
    
    // Just here because it might be easier to remember...
    default boolean isStronglyConnected() {
        return inSameGuest();
    }

    default boolean isWeaklyConnected() {
        return !inSameGuest();
    }

    /**
     * The source of the relation.
     * 
     * @return the source of the relation
     */
    Component source();

    /**
     * The target of the relation.
     * 
     * @return the target of the relation
     */
    Component target();
}

//Taenker den er immutable...

//Problemet er at vi vil have en snapshot...
//Tager vi snapshot af attributer??? Nej det goer vi altsaa ikke...
//Rimlig meget overhead

//Tror topol

//Vi skal ikke have noget live...
//Maaske af det derfor

//Special cases

//Not In same system
//-> distance = -1, inSameContainer=false, isStronglyBound=false, isWeaklyBound=false

//Same component
//-> distance = 0, inSameContainer=true, isStrongBound=?, isWeaklyBound=false

//Teanker vi flytter den et andet sted end attributer...

//Altsaa den walk man kan lave via Iterable... ville det vaere rart at kunne beskrive relationsship
//for den.. Altsaa cr[4] er foraeldre til cr[3] og saa 

//A component in a runtime system is not the same as in the build time system.

//I would say the restartable image of one system is not in the same system as
//the same restartable image when we have restarted.

//https://en.wikipedia.org/wiki/Path_(graph_theory)
//ComponentConnection

//A ComponentRelation is directed
//Can we have attributes??
//if (rel.inSameContainer()) 

//En relation er jo mere end topology... Syntes

//walkFromSource(); Syntes maaske ikke den skal extende Iteralble...
//Maaske fromSourceIterator
// https://en.wikipedia.org/wiki/Tree_structure
// description()? -> Same, parent, child, descendend, ancestor,

//default WiringStrength wiringStrength() {
//    return hasSameGuest() ? WiringStrength.STRONG : WiringStrength.WEAK;
//}

//interface InterExtensionRelationship {
// from, to
// isX
// distance
//}