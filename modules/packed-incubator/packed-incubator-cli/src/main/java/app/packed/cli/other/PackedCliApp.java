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
package app.packed.cli.other;

import app.packed.application.App;
import app.packed.application.ApplicationTemplate;
import app.packed.application.BootstrapApp;
import app.packed.bean.Bean;
import app.packed.assembly.Assembly;
import app.packed.container.Wirelet;
import app.packed.lifecycle.RunState;

/**
 *
 */
//Like App but calls system exit and prints stack
//traces to system.err

// CliApp er maaske i virkeligheden mere noget der har praecist mest 1 entrypoint.

// Er ikke noedvendigvis managed. Eller det kan den jo ligesaa godt eftersom vi ikke
// har start/stop. Alt koere indefor en operation

// CliApp
//// Always takes String[] (Why cli otherwise?) when launching (Not mirrors ect)
//// Calls system exit on failure
//// Install a shutdown hook if managed.

// Vs App
//// I don't it is Closable. Actually I think it may only have static methods?
/////// Or maybe return a (manual) cliApp with system. and system.exit();
//// I think these are two main differences

public final class PackedCliApp {

    /** Nope. */
    private PackedCliApp() {}

    public static void run(Assembly assembly, String[] args, Wirelet... wirelets) {
        App.run(assembly);
    }
//
//    public static void run(Assembly assembly, Wirelet... wirelets) {
//        App.run(assembly);
//    }

    public static void mirrorOf(Assembly assembly, Wirelet... wirelets) {
        bootstrap().mirrorOf(assembly, wirelets);
    }

    public static int runNoSystemExit(Assembly assembly, String[] args) {
        App.run(assembly);
        return 0;
    }

    public static void verify(Assembly assembly) {
        bootstrap().verify(assembly);
    }

    /**
     * Returns the bootstrap app. If interfaces allowed non-public fields we would have stored it in a field instead of this
     * method.
     */
    private static BootstrapApp<Void> bootstrap() {
        class ServiceLocatorBootstrap {
            private static final BootstrapApp<Void> APP = BootstrapApp.ofManaged(Bean.of(Void.class));
        }
        return ServiceLocatorBootstrap.APP;
    }
//
//    public static final class Result {
//
//    }

    /** An application image for App. */
    public static final class Image {

        /** The bootstrap image we are delegating to */
        private final BootstrapApp.Image<?> image;

        private Image(BootstrapApp.Image<?> image) {
            this.image = image;
        }

        /** Runs the application represented by this image. */
        public void run(String[] args) {
            image.launch(RunState.TERMINATED);
        }
    }

    public class Launcher {
        // Tror faktisk vi er chatty som default
        // Sgu da ikke hvis vi er en CLI. Det vil jeg da vaere ret traet af.
        // CliApp.launcher().silent().run();
        Launcher silent() {
            return this;
        }
    }
}

