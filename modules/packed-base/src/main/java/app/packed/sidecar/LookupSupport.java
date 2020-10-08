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
package app.packed.sidecar;

import java.lang.invoke.MethodHandles;

/**
 *
 */
// Ideen er at vi dropper support for generisk lookup thingling...
// Istedet for kalder man ind på den her metode...
// I en statisk initializer i klassen...
// Den anden annotering er sgu for forvirrende...

// Cross module extension hirakies

// Maaske er det snare i component
// Altsaa det er jo kun taenkt paa source support
////

//ComponentSupport
// SidecarSupport <--- Saa kan vi lige saa godt have en Sidecar klasse...
// OpenSupport
class LookupSupport {

    // FieldSidecar.openFields();
    // FieldSidecar.openMethods();

    // Tror alligevel en dedikeret klasse er bedst...
    // Vil gerne have nogle metoder der tager en hel klasse...
    //

    // Hvis felterne er statiske kan vi ikke lade det at en constructor ikke er protected/public
    // styre adgangen...
    // Alle kan bruge installStatic()...
    // Saa tror realm skal have adgang til alle members...

    // Hvad hvis den er aabent til et andet modul...
    // Saa behoever vi vel ikke denne?? IDK

    // Consumer<Realm> <--- for detaljeret check.

    // Must be called in static... before the class is first used... Otherwise throws ISE
    // Taenker vi gemmer det i class value der bliver checket fra en source member class value
    public static void openFieldsForAllSubclasses(MethodHandles.Lookup lookup, Class<?> superClass) {
        // allows subclasses...
    }

    public static void openFieldsForSubclasses(MethodHandles.Lookup lookup, Class<?> superClass, String... modules) {
        // allows subclasses...
    }
}
