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

import app.packed.component.guest.FromGuest;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.runtime.ManagedLifecycle;
import app.packed.runtime.RunState;
import app.packed.runtime.StopOption;

/**
 * Ideen var lidt at man bare extended nogle faa metoder. Og saa havde man en working implementation.
 */
// IDK hvad er det man vil?
class AbstractApp implements App {
    final ManagedLifecycle lc;

    AbstractApp(ManagedLifecycle lc) {
        this.lc = lc;
    }

    /** {@inheritDoc} */
    @Override
    public boolean awaitState(RunState state, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {

    }

    /**
     * Creates a new app image by wrapping the specified bootstrap image.
     *
     * @param image
     *            the bootstrap app image to wrap
     * @return an app image
     */
    protected final App.Image newImage(BaseImage<?> image) {
        return new AppImage(image);
    }

    /** {@inheritDoc} */
    @Override
    public RunState state() {
        return lc.currentState();
    }

    /** {@inheritDoc} */
    @Override
    public void stop(StopOption... options) {
        throw new UnsupportedOperationException();
    }

    /** Implementation of {@link app.packed.application.App.Image}. */
    record AppImage(BaseImage<?> image) implements App.Image {

        /** {@inheritDoc} */
        @Override
        public void run() {
            image.launch(RunState.TERMINATED);
        }

        /** {@inheritDoc} */
        @Override
        public void run(Wirelet... wirelets) {
            image.launch(RunState.TERMINATED, wirelets);
        }

        /** {@inheritDoc} */
        @Override
        public App start() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public App start(Wirelet... wirelets) {
            return null;
        }
    }

    /** Default implementation of App. */
    static final class DefaultApp extends AbstractApp {
        DefaultApp(@FromGuest ManagedLifecycle c) {
            super(c);
        }

        static final BootstrapApp<DefaultApp> BOOTSTRAP = ApplicationTemplate.of(DefaultApp.class, c -> {
            c.container(ContainerTemplate.MANAGED);
        }).newBootstrapApp();
    }
}
