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

import app.packed.application.BootstrapApp.Image;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.util.Result;
import sandbox.extension.container.ContainerHolderService;

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
public final class JobApp {

    /** The bootstrap app. */
    private static final BootstrapApp<Holder> BOOTSTRAP = BootstrapApp.of(Holder.class, c -> c.managedLifetime().expectsResult(Object.class));

    @SuppressWarnings("unchecked")
    public static <T> Result<T> compute(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
        return (Result<T>) Result.ofFuture(BOOTSTRAP.launch(assembly, wirelets).result);
    }

    public static <T> Image<T> imageOf(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static <T> T run(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
        Holder result = BOOTSTRAP.expectsResult(resultType).launch(assembly, wirelets);
        Object t = result.result.resultNow();
        return resultType.cast(t);
    }

    static <T> Future<T> runAsync(Class<?> resultType, Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    record Holder(@ContainerHolderService Future<?> result) {}
}
