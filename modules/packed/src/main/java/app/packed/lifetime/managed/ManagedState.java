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
package app.packed.lifetime.managed;

import java.util.Optional;

import app.packed.lifetime.RunState;

/**
 * The state immutable snapshot of the state of a container.
 */
// Generic StateInfo immutable class...
//  HostInfo?

// LifetimeState <--- lige nu har vi lifetimes uden RunState

// Det er ikke application state. Det kunne ogsaa vaere en Session

// Ved ikke om den bare skal hedde RunStateSnapshot.
// Ved ikke hvad metoderne skal hedde hvis man har begge dele paa et App interface.

public interface ManagedState {

    /** {@return the actual state of the entity.} */
    RunState currentState();

    /** {@return the desired state of the entity.} */
    RunState desiredState();

    boolean isFailed();

    // Det er ikke umiddelbart muligt at returnere den nye container...
    // Eftersom den maaske er ved at vaere constructed
    boolean isRestarting();

    // is or has restarted. Need to query the host???
    // or maybe return String, which is the name of the new container
    // idk

    /**
     * Returns whether or not the container is transitioning from one state to another.
     * 
     * @return true if the container's desired state is different from its actual state, other false
     */
    default boolean isTransitioning() {
        // Altsaa hvad hvis vi restarter... Saa er vi altid transitioning...
        // Ja det passer vel meget godt...
        return currentState() != desiredState();
    }

    /**
     * If the current state is a failure state. Returns the actual failure.
     * 
     * @return the state failure (optional)
     */
    // Maybe Exception??? Not sure an error
    Optional<Throwable> throwable();

    // long generation(); Everytime the current state changes... gen++.
    // Generation har kun noget at goere med current???
    // Generation 1, efter restart Generation 2...
}
//Desired State is the state that you want the system to be in
//Actual State is the state that the system is actually in

// Desired 25 threads, actual 10 threads -> Add more threads...

//https://downey.io/blog/desired-state-vs-actual-state-in-kubernetes/

//ArtifactState??? ContainerState... Altsaa
//Paa component niveau...
//Hvad hvis vi har en actor????

//SnapshotState???
//Altsaa vi vil jo gerne hedde noget andet end noget med State..

//Status in the name is 5% better...
//ControlLoop

//StateSnapshot / StatusSnapshot / RunstateSnapshot
//DesiredRunState
//RunState.Snapshot...
//Altsaa det skal vaere forbundet til

//Was GuestSnapshotState

//GuestDetailedState
//GuestStateDetails

//transitions
//multi dimensional
