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
package app.packed.lifetime;

/**
 * An enum containing all valid states of an application, container or bean.
 *
 * There are 4 <b>steady</b> states: {@link #UNINITIALIZED}, {@link #INITIALIZED}, {@link #RUNNING} and {@link #TERMINATED}.
 *
 * There are 3 <b>intermediate</b> states: {@link #INITIALIZING}, {@link #STARTING} and {@link #STOPPING}.
 *
 * Steady states normally requires external stimuli to transition to a new state. For example, that the user invokes a
 * {@code start} function of some kind on the object. Which results in the object transitioning from
 * {@link #INITIALIZED} state to {@link #STARTING} state. Intermediate states on the other hand normally transitions
 * "automatically". That is, when all the startup code has been successfully executed. The object will automatically
 * transition to the {@link #RUNNING} state without the user having to do anything.
 * <p>
 * This enum does not contain information about whether or not a guest is restarted.
 */
// Failure on Initializain -> Stopping or Terminated?
// Stop called on Initialized -> Stopping or Terminated? (should be same as above)
// Stop while starting
// RunState.... Tjah det implyer maaske Execution.... Men det daekker jo over begrebet runtime...

// Det betyder maaske ogsaa at restart ikke er en del

// TODO jeg tror vi meget bedre kan beskrive det her naar vi har fundet ud af det med state machines
// Fx, hvad vil det sige at stoppe?? Det vil sige at terminere alle state machines der koere ind i den.
// Managed Resource
// Status vs State..
// Was RunState

//// ApplicationInstanceState
//// BeanInstanceState

// ManagedState?? Hmm Vi har jo @Initialize for non-managed application
// Lifetime state
// ContainerState?
public enum RunState {

    /**
     * The initial state of a instance. This state is typically used for reading and validating the configuration of the
     * guest. Throwing an exception or error if some invariant is broken.
     * <p>
     * If the guest is successfully finishes the initialization phase, it will move to the {@link #INITIALIZED} state. If it
     * fails, it will move to the {@link #TERMINATED} state.
     */
    // Ideen er vist lidt at en lazy container can rapportere denne state
    UNINITIALIZED,

    /**
     * The initial state of a instance. This state is typically used for reading and validating the configuration of the
     * guest. Throwing an exception or error if some invariant is broken.
     * <p>
     * If the guest is successfully finishes the initialization phase, it will move to the {@link #INITIALIZED} state. If it
     * fails, it will move to the {@link #TERMINATED} state.
     */
    INITIALIZING,

    /**
     * This state indicates that the guest has completed the {@link #INITIALIZING} phase successfully.
     * <p>
     * The instance will remain in this state until it is started. For example, by the user calling {@link Host#start()}.
     * After which the instance will transition to the {@link #STARTING} state.
     */
    INITIALIZED,

    /**
     * Indicates that the guest has been started, for example, by the user calling {@link Host#start()}. However, all
     * components have not yet completed startup. When all components have been properly started the container will
     * transition to the {@link #RUNNING} state. If any component fails to start up properly. The container will
     * automatically shutdown and move to the {@link #STOPPING} phase.
     */
    STARTING,

    /**
     * The guest is running normally. The guest will remain in this state until it is shutdown, for example, by the user
     * calling {@link Host#stop(Host.StopOption...)}. After which it will transition to the {@link #STOPPING} state.
     */
    RUNNING,

    /**
     * The guest is currently in the process of being stopped. When the guest has been completely stopped it will transition
     * to the {@link #TERMINATED} state.
     */
    STOPPING,

    /** The final lifecycle state. Once the instance reaches this state it will never transition to any other state. */
    TERMINATED;

    /**
     * Returns true if the guest is in any of the specified states, otherwise false.
     *
     * @param states
     *            the states to test against
     * @return true if the guest is in any of the specified states, otherwise false
     */
    public boolean isAnyOf(RunState... states) {
        for (RunState s : states) {
            if (s == this) {
                return true;
            }
        }
        return false;
    }

    public boolean isStableState() {
        return !isTransitionalState();
    }

    public boolean isTransitionalState() {
        return this == INITIALIZING || this == STARTING || this == STOPPING;
    }

    /**
     * Returns true if the guest has been shut down (in the {@link #STOPPING} or {@link #TERMINATED} state).
     *
     * @return true if the guest has been shut down (in the stopping or terminated state).
     */
    public boolean isShutdown() {
        return this == STOPPING || this == TERMINATED;
    }

    public boolean isAlive() {
        return this != UNINITIALIZED && this != TERMINATED;
    }
}
