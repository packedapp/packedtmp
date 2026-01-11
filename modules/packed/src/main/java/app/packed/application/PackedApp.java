/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import static app.packed.component.SidehandleBinding.Kind.FROM_CONTEXT;

import java.util.concurrent.TimeUnit;

import app.packed.bean.Bean;
import app.packed.binding.Key;
import app.packed.component.SidehandleBinding;
import app.packed.component.SidehandleContext;
import app.packed.lifecycle.RunState;
import app.packed.lifecycle.runtime.ManagedLifecycle;
import app.packed.lifecycle.runtime.StopOption;
import internal.app.packed.ValueBased;

/** The default implementation of {@link App}. */
@ValueBased
final class PackedApp implements App {

    /** The bootstrap app for this application. */
    // Hmm, read of constructor, think we need module expose to packed, should probably be in the docs somewhere
    public static final BootstrapApp<PackedApp> BOOTSTRAP_APP = BootstrapApp.of(ApplicationTemplate.builder(Bean.of(PackedApp.class)).build());

    /** Manages the lifecycle of the app. */
    private final ManagedLifecycle lifecycle;

    PackedApp(@SidehandleBinding(FROM_CONTEXT) ManagedLifecycle lc, SidehandleContext context) {
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
    record AppImage(BootstrapApp.Image<PackedApp> image) implements App.Image {

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

        /** {@inheritDoc} */
        @Override
        public ApplicationMirror mirror() {
            return image.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return image.name();
        }
    }

    /** Implementation of {@link app.packed.application.App.Image}. */
    record AppLauncher(BootstrapApp.Launcher<PackedApp> launcher) implements App.Launcher {

        /** {@inheritDoc} */
        @Override
        public <T> AppLauncher provide(Key<? super T> key, T value) {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            launcher.launch(RunState.TERMINATED);
        }

        /** {@inheritDoc} */
        @Override
        public App start() {
            return launcher.launch(RunState.RUNNING);
        }
    }
}
