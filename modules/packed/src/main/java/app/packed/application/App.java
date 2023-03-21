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

/**
 * An entry point for... This class contains a number of methods that can be to execute or analyze programs that are
 * written use Packed.
 *
 * An entry point for all the various types of applications that are available in Packed.
 * <p>
 * This class does not prov For creating application -instances -mirrors and -images.
 */
// [Checked|Not-Checked]Launch, Mirror, Launcher, ReuseableLauncher

// App must either have threads running after it has started, or an entry point.
// Ellers er det et sort hul...
public final class App {

    /** The bootstrap app. */
    private static final BootstrapApp<Void> BOOTSTRAP = BootstrapApp.of(c -> c.managedLifetime());

    /** Not today Satan, not today. */
    private App() {}

    /**
     * Builds an application and returns a launcher that can be used to launch a <b>single</b> instance of the application.
     * <p>
     * If you need to launch multiple instances of the same application specify {@code ApplicationImageWirelets.resuable()}.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @return an application image that can be used to launch a single instance of the application
     */
    public static App.Image imageOf(Assembly assembly, Wirelet... wirelets) {
        return new Image(BOOTSTRAP.imageOf(assembly, wirelets));
    }

    /**
     * Builds an application and returns a mirror representing it.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @return a mirror representing the application
     * @throws RuntimeException
     *             if the application could not be build
     */
    public static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return BOOTSTRAP.mirrorOf(assembly, wirelets);
    }

    public static void print(Assembly assembly, Wirelet... wirelets) {
        // not in final version I think IDK, why not...
        // I think it is super usefull
        //// Maybe have something like enum PrintDetail (Minimal, Normal, Full)
        // ApplicationPrinter.Full, ApplicationPrinter.Normal

//      static void print(Assembly assembly, Object printDetails, Wirelet... wirelets) {
//      // printDetails=Container, Assemblies,////
//      mirrorOf(assembly, wirelets).print();
//  }
        mirrorOf(assembly, wirelets).print();
    }

    public static void run(Assembly assembly) {
        run(assembly, new Wirelet[0]);
    }
    /**
     * Builds and executes an application from the specified assembly and optional wirelets.
     * <p>
     * If the application is built successfully from the assembly. A single instance of the application will be created and
     * executed. This method will block until the application reaches the {@link RunState#TERMINATED terminated} state.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @throws RuntimeException
     *             if the application failed to build or run
     */
    public static void run(Assembly assembly, Wirelet... wirelets) {
        BOOTSTRAP.launch(assembly, wirelets);
    }

    /**
     * Builds and verifies an application.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @throws RuntimeException
     *             if the application could not be build
     */
    public static void verify(Assembly assembly, Wirelet... wirelets) {
        BOOTSTRAP.verify(assembly, wirelets);
    }

    /** An application image for App. */
    public static final class Image {

        /** The bootstrap image we are delegating to */
        private final BootstrapApp.Image<?> image;

        private Image(BootstrapApp.Image<?> image) {
            this.image = image;
        }

        /** Runs the application represented by this image. */
        public void run() {
            image.launch();
        }

        /**
         * Runs the application represented by this image.
         *
         * @param wirelets
         *            optional wirelets
         */
        public void run(Wirelet... wirelets) {
            image.launch(wirelets);
        }
    }
}

