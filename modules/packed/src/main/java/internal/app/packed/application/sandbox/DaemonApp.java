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
package internal.app.packed.application.sandbox;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationLauncher;
import app.packed.application.ApplicationMirror;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.lifetime.sandbox.ManagedLifetimeController;
import app.packed.lifetime.sandbox.StopOption;

/**
 *
 */
// Was DaemonApp (maybe it will be again)
// Could be a simple primitive class that wraps LAC
// Hah det er maaske en traad app? Der bliver jo noedt til minimum at koere en traad...
// Og saa alligevel ikke den kan vaere 100% passiv
public interface DaemonApp extends AutoCloseable {

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
    ManagedLifetimeController lifecycle();

    default void stop(StopOption... options) {
        lifecycle().stop(options);
    }

    default void stopAsync(StopOption... options) {
        lifecycle().stop(options);
    }

    // launching the image will result in a daemon being returned in the starting state

    // Should this be the app in an unitialized state instead
    public static ApplicationLauncher<DaemonApp> build(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static ApplicationLauncher<DaemonApp> buildImage(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static DaemonApp start(Assembly assembly, String[] args, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static DaemonApp start(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
    
    static ApplicationDriver<DaemonApp> driver() {
        throw new UnsupportedOperationException();
    }
}
