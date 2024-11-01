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
package app.packed.application;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import app.packed.component.guest.ComponentHostContext;
import app.packed.component.guest.FromGuest;
import app.packed.container.Wirelet;
import app.packed.runtime.ManagedLifecycle;
import app.packed.runtime.RunState;
import app.packed.runtime.StopOption;
import internal.app.packed.ValueBased;

/** The default implementation of {@link App}. */
@SuppressWarnings("rawtypes")
@ValueBased
// Move to .application when finished
// Keep App as interface if people want to mock it, extend it, ect.
final class PackedJobApp implements JobApp {

    /** The bootstrap app for this application. */
    public static final BootstrapApp<PackedJobApp> BOOTSTRAP_APP = BootstrapApp.of(ApplicationTemplate.ofManaged(PackedJobApp.class));

    /** Manages the lifecycle of the app. */
    private final ManagedLifecycle lifecycle;

    PackedJobApp(@FromGuest ManagedLifecycle lc, @FromGuest Future<?> result, ComponentHostContext context) {
        this.lifecycle = lc;
    }

    /** {@inheritDoc} */
    @Override
    public Future asFuture() {
        return null;
    }

    Object result() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public RunState state() {
        return lifecycle.currentState();
    }

    @Override
    public String toString() {
        return state().toString();
    }

    /** Implementation of {@link app.packed.application.App.Image}. */
    record AppImage(BaseImage<PackedJobApp> image) implements JobApp.Image {

        /** {@inheritDoc} */
        @Override
        public Object run(Wirelet... wirelets) {
            PackedJobApp a = image.launch(RunState.TERMINATED, wirelets);
            return a.result();
        }

        /** {@inheritDoc} */
        @Override
        public JobApp start(Wirelet... wirelets) {
            return image.launch(RunState.RUNNING, wirelets);
        }

        /** {@inheritDoc} */
        @Override
        public Object checkedRun(Wirelet... wirelets) throws ApplicationException {
            PackedJobApp a = image.checkedLaunch(RunState.TERMINATED, wirelets);
            return a.result();
        }

        /** {@inheritDoc} */
        @Override
        public JobApp checkedStart(Wirelet... wirelets) throws ApplicationException {
            return image.checkedLaunch(RunState.RUNNING, wirelets);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean awaitState(RunState state, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {}

    /** {@inheritDoc} */
    @Override
    public void stop(StopOption... options) {}
}

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
