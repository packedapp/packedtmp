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
package sandbox.lifetime;

/**
 * Manages a single lifetime instance
 */

// Kalder den instance, men er ikke sikker paa vi har behov for det

// Er aldrig en primitve klasse. Da vi er 100% mutable (med mindre vi self redelegere)
// Tror den er sealed
public interface ManagedLifetime {

    // Hvad hvis vi kalder den i initialize

    // Hvis man er initialized skal man bare smide...
    boolean tryFail(Throwable cause);
}
// ---- Features
// currentState, desiredState
// awaitState
// start?, stop(with config) fail
// runAfterState



//Int state
//Outcome (Object result/Throwable failure)
//Awaiters (if you need to signal stuff)
//Thread (used for callation)
//
//---
//Det her er mere eller mindre indholdet af FutureTask