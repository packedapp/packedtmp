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

import java.util.Optional;

/**
 * Immutable
 */
// Kan injectes ind enhver metode annoteret med @On....

// Fra, Til, Errors, IsRestarting, ...

//Igen hvis det kun er runstate saa Runstate transition...

// Action ConfigurationSite? ActionSite?, ...
public interface LifecycleTransition {

    /**
     * Returns a readable string, representing the reason for the transition.
     * 
     * @return a readable string, representing the reason for the transition
     */
    Optional<String> action();

    /**
     * The state than an entity is transitioning from.
     * 
     * @return state than an entity is transitioning from
     */
    String from();

    /**
     * The state than an entity is transitioning to.
     * 
     * @return state than an entity is transitioning to
     */
    String to();

    default boolean isFailed() {
        return true;
    }

    default Optional<Throwable> failure() {
        return Optional.empty();
    }
}

// Something about failures
// boolean isEndState

// Something about whether or not this is the desired state???