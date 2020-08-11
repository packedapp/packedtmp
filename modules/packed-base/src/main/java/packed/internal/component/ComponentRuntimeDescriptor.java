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
package packed.internal.component;

import app.packed.component.ComponentDriver;

/**
 * The different types of components that are supported in Packed.
 */

// Til noedt kan vi kalde den BuildinComponentType

// Driver + Bundle + X... Skal gaa igen imellem images...
// Faktisk er navnet vel ogsaa her????? Nah ikke for rod images containere...
public final class ComponentRuntimeDescriptor {

    // A single Method...
    // All Other methods are ignored...
    // Also Annotations et
//    public static final ComponentDescriptor FUNCTION = new ComponentDescriptor();

    public static final ComponentRuntimeDescriptor COMPONENT_INSTANCE = new ComponentRuntimeDescriptor(3);

    /** A container holds other components and provide strong boundaries between different containers. */
    public static final ComponentRuntimeDescriptor CONTAINER = new ComponentRuntimeDescriptor(2);
//
//    /**
//     * A host allows for dynamic wiring between a host and a guest container. Unlike the static wiring available via, for
//     * example, via {@link Bundle#link(Bundle, Wirelet...)}.
//     */
//    HOST,

    public static final ComponentRuntimeDescriptor STATELESS = new ComponentRuntimeDescriptor(1);

    ComponentRuntimeDescriptor(int depth) {
        this.depth = depth;
    }

    /** The depth of the component in a tree of components. */
    // Depth kan have 8 bit-> full depth, 8 bit, container depth, 8 bit artifact depth.
    final int depth;

    static ComponentRuntimeDescriptor of(ComponentDriver<?> driver, PackedComponentConfigurationContext context) {
        return new ComponentRuntimeDescriptor(context.depth());
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