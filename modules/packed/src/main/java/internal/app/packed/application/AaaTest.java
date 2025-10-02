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
package internal.app.packed.application;

import java.util.concurrent.TimeUnit;

import app.packed.application.App;
import app.packed.application.ApplicationLauncher;
import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationTemplate;
import app.packed.application.BootstrapApp;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.lifecycle.Initialize;
import app.packed.bean.lifecycle.OnStart;
import app.packed.bean.lifecycle.Stop;
import app.packed.binding.Key;
import app.packed.component.guest.ComponentHostContext;
import app.packed.component.guest.FromGuest;
import app.packed.runtime.ManagedLifecycle;
import app.packed.runtime.RunState;
import app.packed.runtime.StopOption;

/**
 *
 */
public class AaaTest extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(TB.class);
    }

    public static void main(String[] args) throws InterruptedException {
        App a = AApp.BOOTSTRAP_APP.launch(RunState.STARTING, new AaaTest());

        IO.println("STate " + a.state());
        Thread.sleep(4000);
    }

    public static class TB {

        @Initialize
        public void init() {
            IO.println("INIT");
            IO.println(Thread.currentThread());
        }

        @OnStart
        public void onStart() throws InterruptedException {
            Thread.sleep(1000);
            IO.println("STARTING");
            IO.println(Thread.currentThread());
        }

        @Stop
        public void onstop() throws InterruptedException {
            Thread.sleep(1000);
            IO.println("soppping");
        }
    }

    static final class AApp implements App {

        /** The bootstrap app for this application. */
        public static final BootstrapApp<AApp> BOOTSTRAP_APP = BootstrapApp.of(ApplicationTemplate.ofManaged(AApp.class));

        /** Manages the lifecycle of the app. */
        private final ManagedLifecycle lifecycle;

        AApp(@FromGuest ManagedLifecycle lc, ComponentHostContext context) {
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
        public record AppImage(BootstrapApp.Image<AApp> image) implements App.Image {

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

            /** {@inheritDoc} */
            @Override
            public <T> ApplicationLauncher provide(Key<? super T> key, T value) {
                return image;
            }
        }
    }
}
