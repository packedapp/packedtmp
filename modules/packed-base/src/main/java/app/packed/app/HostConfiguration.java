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
package app.packed.app;

import app.packed.component.ComponentConfiguration;
import app.packed.util.Key;

/**
 *
 */
interface HostConfiguration extends ComponentConfiguration {

    HostConfiguration as(Key<? extends Host> key);
}
// Component
// -> Container
// -> Noneton
// -> Singleton
// -> Manyton
// -> Host
// -> Placeholder <- A component that just organizes other components

// Ville vaere fint hvis de havde hver deres forbogstav... kunne vaere let at vise dem
// grafisk saa. Maaske 2 bogstaver AS-> ActorSystem, AC -> Actor, AS -> AgentSystem??

// Singleton -> Listener, Service, 1 instance component

// ComponentInstanceMultiplicity
/// NONE, SINGLE, MANY