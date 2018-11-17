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
 * The various states a lifecycle enabled entity can be in an.
 *
 * There are 3 <b>steady</b> states: {@link #INITIALIZED}, {@link #RUNNING} and {@link #TERMINATED}.
 *
 * There are 3 <b>intermediate</b> states: {@link #INITIALIZING}, {@link #STARTING} and {@link #STOPPING}.
 *
 * Steady states normally requires external stimuli to transition to a new state. For example, that the user invokes a
 * {@code start} function of some kind on the object. Which results in the object transitioning from
 * {@link #INITIALIZED} state to {@link #STARTING} state. Intermediate states on the other hand normally transitions
 * "automatically". That is, when all the startup code has been successfully executed. The object will automatically
 * transition to the {@link #RUNNING} state without the user having to do anything.
 */
public enum LifecycleState {

    /**
     * The initial state in the lifecycle of an entity. The en will remain in this state until it is started by an external
     * action, for example, by calling start. After which it will transition to the {@link #STARTING} state.
     *
     * Typically this state is used for reading and validating the configuration of entities.
     *
     */
    INITIALIZING,

    /**
     * This is the state an entity reaches immediately after it has been successfully initialized.
     * <p>
     * The entity will remain in this state until it is started by an external action of some kind. For example, if the user
     * calls some kind of {@code start} function. After which it will transition to the {@link #STARTING} state.
     */
    INITIALIZED,

    /**
     * The container has been started by an external action. However, all components have not yet completed startup. When
     * all components have been properly started the container will transition to the {@link #RUNNING} state. If any
     * component fails to start up properly. The container will automatically shutdown and move to the {@link #STOPPING}
     * phase.
     */
    STARTING,

    /**
     * The entity is running normally. The entity will remain in this state until it is shutdown by some stop action, after
     * which it will transition to the {@link #STOPPING} state.
     *
     */
    RUNNING,

    /**
     * The entity is currently in the process of being shutdown. When the entity has completed shutdown it will transition
     * to the {@link #TERMINATED} state.
     */
    STOPPING,

    /**
     * The final lifecycle state of an entity, it cannot transition to any other state.
     */
    TERMINATED;

    /**
     * Returns true if the entity is in any of the specified states, otherwise false.
     *
     * @param states
     *            the states to check
     * @return true if the entity is in any of the specified states, otherwise false
     */
    public boolean isAnyOf(LifecycleState... states) {
        for (LifecycleState s : states) {
            if (s == this) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the entity has been shut down (in the {@link #STOPPING} or {@link #TERMINATED} state).
     *
     * @return true if the entity has been shut down (in the shutdown or terminated state).
     */
    public boolean isShutdown() {
        return this == TERMINATED || this == STOPPING;
    }
}
