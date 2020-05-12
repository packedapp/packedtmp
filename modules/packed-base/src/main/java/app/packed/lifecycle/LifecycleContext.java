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

import java.util.Set;

/**
 *
 */
// Does not implement Equals, and the runtime may return diffent instances
// for the same object.

// Context giver jo som regel control.. Skal vi have en readonly udgave???
// Nej taenker vi exposer den fra ArtifactContext og saa maa folk sgu selv om
// det...

// Desired er ikke en state i en StateMachine....
public interface LifecycleContext {

    /**
     * Returns the current state of the lifecycle enabled object.
     * 
     * @return the current state state of the lifecycle enabled object
     */
    String current();

    /**
     * Returns the desired state of the lifecycle enabled object. This may differ from the value returned by
     * {@link #current()}, for example, if you start an entity which must run through a
     * 
     * @return the desired state of the lifecycle enabled object
     */
    default String desired() {
        return current();
    }

    /**
     * Returns whether or not the current state and the desired state are identical.
     * 
     * @return whether or not the current state and the desired state are identical
     */
    boolean isStable();

    /**
     * Any states the object might transition to next. If the object has reached an end state, an empty set is returned.
     * 
     * @return the possible next states
     */
    Set<String> nextStates();

    /**
     * Returns an immutable snapshot of the current state.
     * 
     * @return an immutable snapshot of the current state
     */
    LifecycleContext snapshot(); // Or Maybe we have LifecycleState which this interface extends. + returned by this method
    // And maybe have an isSnapshot() <- indicating whether or not the instance is live or snapshot

    // Hmm I mean this is a hard failure...
    // What about the notification we where talking about
    // void fail(Throwable cause);
}

// List<String> path(); path to get from current state to desired state...

//isStatePossible(String state) <- whether or not we can ever reach that state...
//isMonothoic, isDeterministic..