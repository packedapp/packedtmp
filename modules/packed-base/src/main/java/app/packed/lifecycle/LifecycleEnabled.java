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
interface LifecycleEnabled {

}
// Maaske er der forskel paa Det med string states og det andet....
// Saa to forskellige objekter.

// Okay,

// Vi har noget streng states som kun er paa containere.
// Vi har noget starup/shutdown som kun er paa containere.
// Vi har noget uninstall, som vel er paa begge.

// Lifecycle... uninstall/install -> Det tror jeg sgu er .container
// Det har basalt set ikke noget med lifecycle at goere..

//////////////////////
// Naar man kalder .stop()-> Vil man gerne have entiteten i stop tilstanden naar metoden returnere...

// Node-> Network enabled stufff