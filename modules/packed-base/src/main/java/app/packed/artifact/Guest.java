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
package app.packed.artifact;

import java.util.Optional;

import app.packed.component.ComponentPath;

/**
 *
 */
// Bliver noedt

// Har lifecycle state....
// Kan startes stoppes, taenker jeg..
// Restartest.
// Hosten har fuld control............

// Ved restart... laver vi altid et swap....
/// latest() vil altid returnere non-null

// Replace with twin....
public interface Guest<A> {

    /**
     * Returns the type of artifact that this guest object wraps.
     * 
     * @return the type of artifact that this guest object wraps
     */
    Class<?> artifactType();

    Optional<GuestInstance<A>> latest();

    ComponentPath path();

    GuestState state();

    class GuestState {
        // Suspended
        // Resuming | Starting
        // Terminated, but lingering
        // Shutdown, will restart

        // Shutdown(For restart)
        // Clean shutdown....Hmmmm... Hvis GET nu er stuck med en tråd der...
    }
}
// ComponentSource.followSuspendedGuests() default false