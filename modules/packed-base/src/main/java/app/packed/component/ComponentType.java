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

// Sealed type....

// SingleLetter
// H <- Host
// S <- ActorSystem
// A <- Actor
// I <- Instance
// M <- Many
// C <- Container
// V <- VirtualContainer

// Task --> ComponentContext.addTask(Class).
// Task --> ComponentContext.addTask(Class, Composer<? super TaskConfiguration>)).
/**
 * The various types of components that are available in Packed.
 */
// Like ElementType
public enum ComponentType {

    COMPONENT_INSTANCE,

    /** A container holds other components and provide strong boundaries containers in-between. */
    CONTAINER,

    /** A host allows for relationship between artifacts. */
    HOST,

    STATELESS;
}

// configuration tyoe
// runtime tyoe
// context type
