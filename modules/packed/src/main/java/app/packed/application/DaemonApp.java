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

import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.lifetime.RunState;
import app.packed.lifetime.StopOption;
import sandbox.lifetime.external.LifecycleController;

/**
 * Similar to App with the extra features that you can
 *
 * Close it
 *
 * Query the state
 *
 * Await states
 */
@SuppressWarnings("exports")
public interface DaemonApp extends AutoCloseable {

    /**
     * Blocks until all tasks within the application have completed after a shutdown request, or the timeout occurs, or the
     * current thread is interrupted, whichever happens first.
     *
     * @param timeout
     *            the maximum time to wait
     * @param unit
     *            the time unit of the timeout argument
     * @return {@code true} if the application terminated and {@code false} if the timeout elapsed before termination
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    boolean awaitState(RunState state, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Closes the app (synchronously). Calling this method is equivalent to calling {@code host().stop()}, but this method
     * is called close in order to support try-with resources via {@link AutoCloseable}.
     *
     **/
    @Override
    default void close() {
        lifecycle().stop();
    }

    /**
     * Returns the applications's host.
     *
     * @return this application's host.
     */
    LifecycleController lifecycle();

    default void stop(StopOption... options) {
        lifecycle().stop(options);
    }

    // Maybe Options are per App type and then maps into something else???
    // Cancel makes no sense, for example, well maybe.
    // pause() makes no sense -> Because we do not have a resume method
    default void stopAsync(StopOption... options) {
        lifecycle().stop(options);
    }

    private static BootstrapApp<DaemonApp> bootstrap() {
        throw new UnsupportedOperationException();
    }

    static Image imageOf(Assembly assembly, Wirelet... wirelets) {
        return new Image(bootstrap().imageOf(assembly, wirelets));
    }

    static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return bootstrap().mirrorOf(assembly, wirelets);
    }

    static DaemonApp start(Assembly assembly, Wirelet... wirelets) {
        return bootstrap().launch(assembly, wirelets);
    }

    static void verify(Assembly assembly, Wirelet... wirelets) {
        bootstrap().verify(assembly, wirelets);
    }

    /** An application image for App. */
    public static final class Image {

        /** The bootstrap image we are delegating to */
        private final BootstrapApp.Image<DaemonApp> image;

        private Image(BootstrapApp.Image<DaemonApp> image) {
            this.image = image;
        }

        /** Runs the application represented by this image. */
        public DaemonApp start() {
            return image.launch();
        }

        /**
         * Runs the application represented by this image.
         *
         * @param wirelets
         *            optional wirelets
         */
        public DaemonApp start(Wirelet... wirelets) {
            return image.launch(wirelets);
        }
    }
}
