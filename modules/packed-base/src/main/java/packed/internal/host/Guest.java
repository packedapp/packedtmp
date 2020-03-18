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
package packed.internal.host;

import java.util.Optional;

import app.packed.component.ComponentPath;

/**
 * A guest in an artifact that is hosted in a host
 */
// Bliver noedt

// Har lifecycle state....
// Kan startes stoppes, taenker jeg..
// Restartest.
// Hosten har fuld control............

// Ved restart... laver vi altid et swap....
/// latest() vil altid returnere non-null

// Replace with twin....

// Bliver en guest erstattet ved restart????
// Nah det vil jeg ikke mene...
// Kun selve artifacten??
// En GuestInstans bliver erstattet....
// Men selve Guest'en vil bare pege på en ny....

public interface Guest<A> {

    /**
     * The artifact this guest
     * 
     * @return the artifact
     */
    Optional<GuestInstance<A>> artifact();

    /**
     * Returns this guest's artifact type.
     * 
     * @return this guest's artifact type
     */
    Class<?> artifactType();

    ComponentPath path();

    GuestState state();

    class GuestState {

        // Altsaa en GuestState er vel ikke anderledes end en almindelige containers state????
        // Jo for den har f.eks. en Uninitialized state...

        // Suspended
        // Resuming | Starting
        // Terminated, but lingering
        // Shutdown, will restart

        // Shutdown(For restart)
        // Clean shutdown....Hmmmm... Hvis GET nu er stuck med en tråd der...
    }
}
// ComponentSource.followSuspendedGuests() default false