/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.service.mirrorold;

import app.packed.bean.BeanMirror;
import app.packed.operation.OperationMirror;

/**
 *
 */

// Taenker vi skal kun have en service der bare er en constant og ikke dukker op som en bean.

// Skal vi kunne sige noget om det er en runtime constant? eller en compile time constant?
// Ja vil jeg mene. Vi vil gerne vide hvornaar den skal instantieres.


// Vi har behov for at kunne se hvor en given provider er blevet brugt...
//// Saa enten skal vi have en extra peger klasse (ProvidedServiceMirror).
//// Eller ogsaa laver vi noget generisk useSite (Relationsship) functionalitet paa de forskellige mirrors.

// From Where
// From Who


public sealed interface ServiceProviderIsThisUsefulMirror permits BeanMirror, OperationMirror, BuildArgumentMirror {

    // Hvor faar man pathen? Man skal vel hen til bindingen, og saa gaa tilbage igen...

    // default Set<?> userAsServices() {
    //   relatoinsShips(SSS.class);
    // }

}
