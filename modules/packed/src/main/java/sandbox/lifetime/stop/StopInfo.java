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
package sandbox.lifetime.stop;

import java.util.Optional;
import java.util.Set;

import app.packed.lifetime.RunState;

/**
 * Information about why a lifetime was stopped. Once a lifetime has been stopped this information will not change.
 * <p>
 * For example, if a lifetime fails while stopping with an exception this does not change what {@link #failure()}
 * returns
 */
// Den bliver lavet praecis en gang. Og aendre sig ikke
// Saa den kan ikke rigtig have informationer omkring forhold
// mellem root lifetimen og children (isDependant)


// Hvad hvis child bliver lukket ned normalt
// Og saa en parent lifetime lukker exceptionelt???


//LifetimeStopInfo?
public interface StopInfo {

    /** {@return the reason for stopping the lifetime. */
    StopReason reason();

    Optional<Throwable> failure();

    boolean isCancelled();

    boolean isCompletedNormally();

    boolean isCompletedExceptionally();

    /** {@return the previous state of the lifetime before it was stopped.} */
    // Is always starting or running. What if initialized.stop?
    RunState previousState();

    //// If the lifetime completed normally with a result

    /** {@return true if the lifetime completed normally with a result, otherwise false.} */
    boolean hasResult();

    // isNormal, isExceptional

    // Cause

    // Whats next

    Set<String> tags(); // user tags? IDK
}
//// Hmm, Don't think so. Let say it says container
//// And we have child application. Would lead to some confusion
// eller Scope [Application, Container, Bean]

//// Nope vil jeg mene, fordi saa kan vi lige pludselig ikke share den
// isDependendant, isRoot, int depth()?? hmm;
// Det kan komme paa initialize
