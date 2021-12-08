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

import app.packed.application.programs.SomeAppImage;
import app.packed.build.BuildException;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;

/**
 * An entry point for... This class contains a number of methods that can be to execute or analyze programs that are
 * written use Packed.
 * 
 * An entry point for all the various types of applications that are available in Packed.
 * <p>
 * This class does not prov For creating application -instances -mirrors and -images.
 * 
 * @see SomeAppImage
 */
// Tror maaske bare den doer med PanicException som standard hvis der gaar noget galt...
// noHookCaching...
// Maaske rename til App

// String res = App.job().restartable().execute(new Assembly);

//AppServer

//App.create/start/execute + mirror + launcher
public final class App {

    /** The artifact driver used by this class. */
    // Maybe use single-use-image as well...
    // Man kan sige det er en slags Profile+SystemInterface i et.
    // Men saa betyder det jo ogsaa at det er 2 forskellige drivere...
    // Eller ogsaa skal ImageWirelets lazy bruges

    // Install as System Namespace
    // Virker underlige at den returnere Complietion... Det betyder jo ogsaa at vi ikke skal smide
    // PanicException hvad jeg syntes vi skal

    /** A daemon driver. */
    private static final ApplicationDriver<Void> DRIVER = ApplicationDriver.builder().buildVoid();

    /** Not today Satan, not today. */
    private App() {}

    /** {@return the artifact driver used by this class.} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static ApplicationDriver<Void> driver() {
        return (ApplicationDriver) DRIVER;
    }

//    /**
//     * Runs the application.
//     * 
//     * @param assembly
//     *            the assembly to use for running
//     * @param args
//     *            program arguments
//     * @param wirelets
//     *            optional wirelets
//     * @throws RuntimeException
//     *             if the application failed to run properly
//     */
//    public static void run(Assembly<?> assembly, String[] args, Wirelet... wirelets) {
//        driver().launch(assembly, CliWirelets.args(args).andThen(wirelets));
//    }

    /**
     * This method will create and start an {@link Program application} from the specified source. Blocking until the run
     * state of the application is {@link RunState#TERMINATED}.
     * <p>
     * Entry point or run to termination
     * <p>
     * This method will automatically install a shutdown hook wirelet using
     * {@link LifecycleWirelets#shutdownHook(app.packed.state.Host.StopOption...)}.
     * 
     * @param assembly
     *            the assembly to execute
     * @param wirelets
     *            wirelets
     * @throws RuntimeException
     *             if the application failed to run properly
     */
    /**
     * Builds an application from the specified assembly. After which a single instance is created that runs until
     * termination.
     * 
     * Waiting until the application has termianted before returning.
     * 
     * @param assembly
     *            the assembly representing the application
     * @param wirelets
     *            optional wirelets
     * @throws BuildException
     *             if the specified application could not be build
     * @throws UnhandledApplicationException
     *             if the fails during runtime
     */
    public static void run(Assembly assembly, Wirelet... wirelets) {
        driver().launch(assembly, wirelets);
    }

    public static ApplicationImage<Void> build(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static ApplicationImage<Void> buildImage(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    static Launcher launcher() {
        return new Launcher();
    }

    public static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
    
    static class Launcher {
        // Kunne have noget med noget Throwable
    }
}


//////// Nope
//??? App kunne ogsaa vaere en klasse hvor der shutcuts for alt?
//App.cli(new Assembly());
//App.mirror(new Assembly());
//App.serviceLocator().newLauncher(new Assembly);
//App.serviceLocator(new Assembly());
//App.daemon().restartable().execute(new Assembly);
//App.daemon().restartable().newLauncher(new Assembly);
