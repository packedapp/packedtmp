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

import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.lifecycle.LifecycleApplicationController;
import app.packed.lifecycle.LifecycleApplicationController.StopOption;

/**
 *
 */
// Was DaemonApp (maybe it will be again)
// Could be a simple primitive class that wraps LAC
public interface AsyncApp extends AutoCloseable {

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
    LifecycleApplicationController lifecycle();

    default void stop(StopOption... options) {
        lifecycle().stop(options);
    }

    default void stopAsync(StopOption... options) {
        lifecycle().stop(options);
    }

    // launching the image will result in a daemon being returned in the starting state

    // Should this be the app in an unitialized state instead
    public static ApplicationLauncher<AsyncApp> build(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static ApplicationLauncher<AsyncApp> buildImage(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static AsyncApp start(Assembly assembly, String[] args, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static AsyncApp start(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
    
    static ApplicationDriver<AsyncApp> driver() {
        throw new UnsupportedOperationException();
    }
}
