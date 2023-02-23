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
package app.packed.context;

/**
 *
 */
// Alternativt ContextScope

// Tror baade vi skal have for hvem og span.
// For hvem er jo kun interessant for containere

public enum ContextSpan {

    // Maaske er det i virkeligheden en setting naar man laver en ContainerTemplate?
    // Denne context, skal automatisk propagate til alle sub containers
    CONTAINER_TREE,

    CONTAINER_LIFETIME,

    CONTAINER,

    /**
     * The context is available from all operation within a single bean.
     */
    BEAN,

    /**
     * The context is available from within a single operation.
     * <p>
     * This doesn't mean that other operations on the same bean can define the same context
     */
    OPERATION;
}

// For container maaske er de inherited per default...
// Men man kan ContextWirelets.removeAllContext();
// Men man kan ContextWirelets.removeContexts(SessionContext.class);

// alternativt ContextWirelets.propagateContexts(SessionContext.class);

// Hmm hvis vi propagater contexts bryder vi jo lidt i en container...
// Just saying... Det er mest det med at resolve keys Hvor man lige pludselig kan injecte ting...