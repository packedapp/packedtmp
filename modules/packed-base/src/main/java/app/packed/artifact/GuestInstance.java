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
package app.packed.artifact;

import app.packed.container.Bundle;

/**
 *
 */
// For example, restart will create a new instance every time....
/// But maybe that is just the artifact.

// Or maybe you can add a guest instance... that have not yet started....

// Kadow...

// Alle instancer har

// Spoergsmall, kan man have en ny instance type????
// Taenker jeg ikke..... Lad os holde det simpler....

// Restart
// Suspend/Resume
// NOT Updating (Ingen grund til at komplicere det
// NOT load balancing, canary deployment...

// Vi vil gerne undvaere denne... Basalt set container den bare en artifact....

// Instance Twin???
public interface GuestInstance<A> {

    long id();

    Guest<A> guest();

    Class<? extends Bundle> sourceType();
}
// Twin ->