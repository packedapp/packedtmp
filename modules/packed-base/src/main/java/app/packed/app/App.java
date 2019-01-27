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
package app.packed.app;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import app.packed.bundle.Bundle;
import app.packed.container.Container;
import app.packed.container.ContainerConfiguration;
import app.packed.lifecycle.LifecycleOperations;

/**
 *
 */
public interface App {

    /**
     * Returns the name of this container.
     * <p>
     * If no name was explicitly when configuring the container, a unique (on a best-effort basis) name was generated.
     *
     * @return the name of this container
     */
    String getName();

    /**
     * Initiates an orderly asynchronously shutdown of the application. In which currently running tasks will be executed,
     * but no new tasks will be started. Invocation has no additional effect if the application has already been shut down.
     * <p>
     * There are (currently) no method similar to {@link ExecutorService#shutdownNow()}.
     *
     * @return a future that can be used to query whether the container has completed shutdown. Or is still in the process
     *         of shutting down the container.
     */
    // Maybe shutdown().cancel() <- can be similar to ExecutorService#shutdownNow
    CompletableFuture<App> shutdown();

    /**
     * Initiates an orderly asynchronously shutdown of the application because of an exceptional condition. Invocation has
     * no additional effect if the app has already been shut down.
     *
     * @param cause
     *            the cause of the shutdown
     * @return a future that can be used to query whether the application has completed shutdown. Or is still in the process
     *         of being shutdown.
     */
    CompletableFuture<Container> shutdown(Throwable cause);

    LifecycleOperations<? extends App> state();
}

class AppStatic {

    // App static kan lave apps, der ikke kraever injection af interfaces...
    // Eller.....

    static App of(Consumer<? super ContainerConfiguration> c) {
        throw new UnsupportedOperationException();
    }

    static void run(Bundle b) {
        throw new UnsupportedOperationException();
    }

    static void run(Bundle b, String[] args) {
        throw new UnsupportedOperationException();
    }

    static App run(Consumer<? super ContainerConfiguration> c) {
        throw new UnsupportedOperationException();
    }

    // static class App {
    //
    // static void of(Consumer<? super ContainerConfiguration> c) {}
    // }
}
