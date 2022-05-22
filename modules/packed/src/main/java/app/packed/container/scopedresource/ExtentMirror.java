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
package app.packed.container.scopedresource;

import java.util.Optional;
import java.util.Set;

import app.packed.application.Realm;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;

/**
 *
 */
//// Et scope
// Tilhoere en extension som definere det. Feks CLI eller DB.
// Den har en root container + et traa hvor den er tilgangelig.
// Den har en root key som er den key den er tilgaenglig under i root containeren.
// Den kan have en local key i en child container

// Application Eventbus... <EventBusExtension> owner= Application, participants = who ever uses it

// Er vel en public overridable klasse?

// Extent?

public interface ExtentMirror<E extends Extension<E>> {

    Optional<String> alternative(); // Tror ikke andet end strings giver mening

    Realm owner();

    Set<Realm> participants();

    ContainerMirror root();
    
    Set<?> useSites();
}
//// Den kan vaere application-wide
//// Den kan vaere private
//// Den kan vaere all descendens