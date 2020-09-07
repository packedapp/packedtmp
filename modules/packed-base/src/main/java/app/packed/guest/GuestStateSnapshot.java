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
package app.packed.guest;

import java.util.Optional;

/**
 *
 */

// ArtifactState??? ContainerState... Altsaa
// Paa component niveau...
// Hvad hvis vi har en actor????

// SnapshotState???
// Altsaa vi vil jo gerne hedde noget andet end noget med State..

// Status in the name is 5% better...
// ControlLoop

// StateSnapshot / StatusSnapshot / RunstateSnapshot
// DesiredRunState
//RunState.Snapshot...
// Altsaa det skal vaere forbundet til

public interface GuestStateSnapshot {

    /**
     * Returns the current state.
     * 
     * @return the current state
     */
    GuestState actual(); // actual is maybe better???

    /**
     * Returns the desired state of the guest.
     * 
     * @return the desired state of the guest
     */
    GuestState desired();

    boolean isFailed();

    Optional<Throwable> error();

    boolean isRestarting();

    default boolean isTransitioning() {
        // Altsaa hvad hvis vi restarter... Saa er vi altid transitioning...
        // Ja det passer vel meget godt...
        return actual() != desired();
    }

    // long generation(); Everytime the current state changes... gen++.
    // Generation har kun noget at goere med current???
    // Generation 1, efter restart Generation 2...
}
//Desired State is the state that you want the system to be in
//Actual State is the state that the system is actually in

// Desired 25 threads, actual 10 threads -> Add more threads...

//https://downey.io/blog/desired-state-vs-actual-state-in-kubernetes/