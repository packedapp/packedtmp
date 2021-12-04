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
package app.packed.application.programs2;

import java.util.concurrent.CompletableFuture;

import app.packed.application.ApplicationImage;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.inject.service.ServiceLocator;
import app.packed.lifecycle.LifecycleApplicationController;

/**
 *
 */

// Ideen med forskellen paa run og execute.
// Er at run() smider en UnhandledApplicationException (RuntimeApplicationException)
// execute() smider aldrig, men returnere en App2 som kan queries for hvad der gik galt
//   og hvilke state man er i
public interface App2 {

    public static void run(Assembly assembly, Wirelet... wirelets) {
        ServiceLocator.of(assembly, wirelets);
    }

    public static void run(Assembly assembly, String[] args, Wirelet... wirelets) {
        ServiceLocator.of(assembly, wirelets);
    }

    public static App2 execute(Assembly assembly, Wirelet... wirelets) {
        ServiceLocator.of(assembly, wirelets);
        return null;
    }

    public static App2 execute(Assembly assembly, String[] args, Wirelet... wirelets) {
        ServiceLocator.of(assembly, wirelets);
        return null;
    }

    public static ApplicationImage<Void> imageOf(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}

// Synchronous run until X. Then start a thread that runs until Y
interface AsyncApp extends AutoCloseable {

    CompletableFuture<Void> asCompletableFuture(); // Maybe stage

    LifecycleApplicationController runtime();

    public static LifecycleApplicationController start(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static LifecycleApplicationController start(Assembly assembly, String[] args, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
