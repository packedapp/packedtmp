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
package sandbox.application;

import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationTemplate;
import app.packed.application.BootstrapApp;
import app.packed.component.guest.FromGuest;

/**
 * Must have a main in a bean with application lifetime.
 *
 * @see app.packed.lifetime.Main
 */
/// retur typer

// App
// * void
// * Result<void>
// * Daemon

//Job
// * R        (or exception)
// * Result<R>
// * Job<R>

//// 3 active result wise
// ContainerLifetime <- a base result type (can never be overridden, usually Object.class)
// Assembly,
//// Completable
//// Entrypoint
// result check

// async / result / checked exception

// Error handling kommer ogsaa ind her...
// Skal vi catche or returnere???
// Det vil jeg bootstrap app'en skal tage sig af...
public final class Te {

    /** The bootstrap app. */
    private static final BootstrapApp<Holder> BOOTSTRAP = BootstrapApp.of(ApplicationTemplate.ofManaged(Holder.class));

    public static void main(String[] args) {
        BOOTSTRAP.getClass();
    }

    record Holder(@FromGuest ApplicationMirror am) {}
}
