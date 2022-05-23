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
import app.packed.lifecycle.RunState;

/**
 * An entry point for... This class contains a number of methods that can be to execute or analyze programs that are
 * written use Packed.
 * 
 * An entry point for all the various types of applications that are available in Packed.
 * <p>
 * This class does not prov For creating application -instances -mirrors and -images.
 * 
 */
public final class App {

    /** A driver for this application. */
    private static final ApplicationDriver<Void> DRIVER = ApplicationDriver.builder().buildVoid();

    /** Not today Satan, not today. */
    private App() {}

    // The launcher can be used exactly once
    public static ApplicationLauncher<Void> newLauncher(Assembly assembly, Wirelet... wirelets) {
        return DRIVER.imageOf(assembly, wirelets);
    }

    public static ApplicationLauncher<Void> newReusableLauncher(Assembly assembly, Wirelet... wirelets) {
        return DRIVER.imageOf(assembly, wirelets);
    }

    /** {@return the application driver used by this class.} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static ApplicationDriver<Void> driver() {
        return (ApplicationDriver) DRIVER;
    }

    /**
     * Builds an application from the specified assembly and returns a mirror representing the application.
     * 
     * @param assembly
     *            the application's assembly 
     * @param wirelets
     *            optional wirelets
     * @return a mirror representing the application
     * @throws BuildException
     *             if the application could not be build
     */
    public static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return DRIVER.mirrorOf(assembly, wirelets);
    }

    public static void print(Assembly assembly, Wirelet... wirelets) {
        // not in final version I think
        DRIVER.print(assembly, wirelets);
    }

    
    /**
     * This method will create and start an {@link Program application} from the specified source. Blocking until the run
     * state of the application is {@link RunState#TERMINATED}.
     * <p>
     * Entry point or run to termination
     * <p>
     * 
     * @param assembly
     *            the assembly to execute
     * @param wirelets
     *            wirelets
     * @throws RuntimeException
     *             if the application failed to run properly
     */
    /**
     * Builds an application from the specified assembly. If successful a single instance of the application that will
     * created and executed until it is {@link RunState#TERMINATED terminated}. After which this method will return.
     * 
     * @param assembly
     *            the assembly representing the application
     * @param wirelets
     *            optional wirelets
     * @throws BuildException
     *             if the application could not be build
     * @throws UnhandledApplicationException
     *             if the fails during runtime
     */
    public static void run(Assembly assembly, Wirelet... wirelets) {
        driver().launch(assembly, wirelets);
    }
}

// static class Launcher {
//
//    static Launcher launcher() {
//        return new Launcher();
//    }
//
//    // Kunne have noget med noget Throwable
// }
