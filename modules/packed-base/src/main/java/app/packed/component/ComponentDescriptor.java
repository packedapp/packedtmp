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

/**
 * The different types of components that are supported in Packed.
 */

// Til noedt kan vi kalde den BuildinComponentType
public final class ComponentDescriptor {

    // A single Method...
    // All Other methods are ignored...
    // Also Annotations et
//    public static final ComponentDescriptor FUNCTION = new ComponentDescriptor();

    public static final ComponentDescriptor COMPONENT_INSTANCE = new ComponentDescriptor();

    /** A container holds other components and provide strong boundaries between different containers. */
    public static final ComponentDescriptor CONTAINER = new ComponentDescriptor();
//
//    /**
//     * A host allows for dynamic wiring between a host and a guest container. Unlike the static wiring available via, for
//     * example, via {@link Bundle#link(Bundle, Wirelet...)}.
//     */
//    HOST,

    public static final ComponentDescriptor STATELESS = new ComponentDescriptor();

    ComponentDescriptor() {}

    public String initializeNameDefaultName(AbstractComponentConfiguration acc) {
        return acc.initializeNameDefaultName();
    }
}

//Sealed type....

//SingleLetter
//H <- Host
//S <- ActorSystem
//A <- Actor
//I <- Instance
//M <- Many
//C <- Container
//V <- VirtualContainer

//Task --> ComponentContext.addTask(Class).
//Task --> ComponentContext.addTask(Class, Composer<? super TaskConfiguration>)).

//Like ElementType

// configuration tyoe
// runtime tyoe
// context type

// Task <------