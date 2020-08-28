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
package app.packed.base;

/**
 *
 */

// IDeen er lidt at vi f.eks. kan generere bootstrap klasser som en del af kompilering.

// Jeg tror det vigtigste er at det er startup-optimeringer hvis du ikke bruger Graal...

// De kommer til at bliver kol

// Vi bliver nok ogsaa noedt til at tilfoeje det til module-info
// provide xxxx uses app.packed.base.Bootstrap

// Tror ikke rigtig vi har brug for den. Det med images er lazy...

abstract class Bootstrap {}
// Den koere maaske bare Sidecar bootstraps for alle componenter concurrently...

// Den skal foerst koeres naar imaged bruges foerste gang...

// Altsaa det bliver noedt til at bliver recompileret hvis man opdatere dependencies...

// FJP.spawnXXm

// Kraever ogsaa initializering af Sidecar instanserne..

// > 400 component source types