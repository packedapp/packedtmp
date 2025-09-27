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

import java.util.concurrent.TimeUnit;

import app.packed.component.guest.ComponentHostContext;
import app.packed.component.guest.FromGuest;
import app.packed.runtime.ManagedLifecycle;
import app.packed.runtime.RunState;
import app.packed.runtime.StopOption;
import internal.app.packed.ValueBased;

/** The default implementation of {@link App}. */
@ValueBased
final class PackedApp implements App {

    /** The bootstrap app for this application. */
    public static final BootstrapApp<PackedApp> BOOTSTRAP_APP = BootstrapApp.of(ApplicationTemplate.ofManaged(PackedApp.class));

    /** Manages the lifecycle of the app. */
    private final ManagedLifecycle lifecycle;

    PackedApp(@FromGuest ManagedLifecycle lc, ComponentHostContext context) {
        this.lifecycle = lc;
    }

    /** {@inheritDoc} */
    @Override
    public boolean awaitState(RunState state, long timeout, TimeUnit unit) throws InterruptedException {
        return lifecycle.await(state, timeout, unit);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        lifecycle.stop();
    }

    /** {@inheritDoc} */
    @Override
    public RunState state() {
        return lifecycle.currentState();
    }

    /** {@inheritDoc} */
    @Override
    public void stop(StopOption... options) {
        lifecycle.stop(options);
    }

    @Override
    public String toString() {
        return state().toString();
    }

    /** Implementation of {@link app.packed.application.App.Image}. */
    record AppImage(BootstrapImage<PackedApp> image) implements App.Image {

        /** {@inheritDoc} */
        @Override
        public void run() {
            image.launch(RunState.TERMINATED);
        }

        /** {@inheritDoc} */
        @Override
        public App start() {
            return image.launch(RunState.RUNNING);
        }
//
//        /** {@inheritDoc} */
//        @Override
//        public void checkedRun(Wirelet... wirelets) throws UnhandledApplicationException {
//            image.checkedLaunch(RunState.TERMINATED, wirelets);
//        }
//
//        /** {@inheritDoc} */
//        @Override
//        public App checkedStart(Wirelet... wirelets) throws UnhandledApplicationException {
//            return image.checkedLaunch(RunState.RUNNING, wirelets);
//        }
    }
}
