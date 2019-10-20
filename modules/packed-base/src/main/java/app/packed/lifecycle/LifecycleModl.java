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
package app.packed.lifecycle;

/**
 *
 */
// Set in extensions... Especially how lazy is handled...

// Lazy = Class instantiation

// Lazy is viral

// Must record dependencies in some way.........a public API todo it.

// Meaning
/// PExtension.registerDependencyBetween(ComponentConfiguration from, ComponentConfiguration to);
// Saying that in order to use to, from must be proper initialized as well

// Is that right down to the default lifecycle model????
// @OnStart(anyOrder=true)
class LifecycleModl {

}
// Hvordan goer man det paa tvaers af containere hosts....

//// Tillader vi cirkler???
/////// Saa virker det med at koere foran hinanden ihvertfald ikke
// Bliver svaert

// @Provide

// Udefra kan man koere de her skridt
////
//// ContainerWirelets.lazyInstantiation
//// ContainerWirelets.lazyOnInjection

/// Lifecycle is a viral extension
//// Meaning that if a child container has the extension, the parent container must also have the extension..

/// Giver det mening at en container har en Lifecycle???
/// Vil isaer vaere rart

// A container is considered running when all non-lazy components are in the running state
// As well as all child containers...

// If a components has never been started, should shutdown be run???

// Giver vel mening, paa Container, Component og App (via Container)