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
package app.packed.state.sandbox;

import java.util.Optional;

/**
 * An immutable representation of a transition from one state to another state.
 * <p>
 * An instance of this interface is available for injection into any methods annotated with {@link Leaving}.
 */
public interface StateTransition {

    /**
     * Returns a readable description of the event that caused the transition. Or empty if no description is available.
     * 
     * @return a readable description of the event that caused the transition. Or empty if no description is available
     */
    Optional<String> event(); // describeEvent()... ala describeConstable

    /**
     * The state than the entity is transitioning from.
     * 
     * @return state than the entity is transitioning from
     */
    String from();

    /**
     * The state than the entity is transitioning to.
     * 
     * @return state than the entity is transitioning to
     */
    String to();

    default boolean isFailed() {
        return !failure().isEmpty();
    }

    default Optional<Throwable> failure() {
        return Optional.empty();
    }
}
// Facts
/// Transitions are immutable and contain no control methods

//Questions
// ? Do StateTransition know anything about possible states????
// ? isEndState()... I think it honestly would be nice...

// StateDescriptor

// If we fail startup, and wants to shutdown...
// to() should return Stopping, not Error...

//Fra, Til, Errors, IsRestarting, ...

//Igen hvis det kun er runstate saa Runstate transition...

//Action ConfigurationSite? ActionSite?, ...

// Something about failures
// boolean isEndState

// Something about whether or not this is the desired state???