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
package app.packed.container;

/**
 *
 */

//Configuration
/// Buildtime only
/// Shared -> Is the wirelet consumed (maybe Consumable)

/// TargetSite -> Where it can be used
/// Scope -> Visibility of the wirelet (can never be cross application)

public class WireletFlag {

    public WireletFlag ALLOW_RUNTIME; // on

    /** Wirelets that are not shared must be consumed exactly once. */
    public WireletFlag ALLOW_SHARED; // alternative skal man overskrive en metode... Tror ikke det er udbredt
    // Problemet er den metode skal have Variable'sene med som parameter.
    // Supplier<List<Places>>

    // alternative har vi en metode vi overskriver
    public WireletFlag ALLOW_UNCONSUMED;


    // 1 bit build time only

    // 1 bit shared

    // X bits <-- where it can be used
    // All containers
    // All Container lifetime roots
    // All applications

    // Visibility
    //
    //
}



// Skal tage det i constructeren

// Ent