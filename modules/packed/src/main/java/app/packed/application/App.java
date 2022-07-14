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

import app.packed.application.sandbox.UnhandledApplicationException;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.lifetime.RunState;

/**
 * An entry point for... This class contains a number of methods that can be to execute or analyze programs that are
 * written use Packed.
 * 
 * An entry point for all the various types of applications that are available in Packed.
 * <p>
 * This class does not prov For creating application -instances -mirrors and -images.
 */
// [Checked|Not-Checked]Launch, Mirror, Launcher, ReuseableLauncher
public final class App {

    /** The default application driver. */
    private static final ApplicationDriver<Void> DEFAULT_DRIVER = ApplicationDriver.builder().buildVoid();

    /** Not today Satan, not today. */
    private App() {}

    /**
     * Builds an application and returns a launcher that can be used to launch a <u>single</u> instance of the application.
     * <p>
     * If you need to launch multiple instances of the same application use {@link #buildReusable(Assembly, Wirelet...)}. Or
     * maybe use that Application wirelet... I don't really think it is that common
     * 
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @return a launcher that can be used to launch a single instance of the application
     */
    public static ApplicationLauncher<Void> build(Assembly assembly, Wirelet... wirelets) {
        return DEFAULT_DRIVER.newImage(assembly, wirelets);
    }

    public static ApplicationLauncher<Void> buildReusable(Assembly assembly, Wirelet... wirelets) {
        return DEFAULT_DRIVER.newImage(assembly, wirelets);
    }

    /**
     * @param assembly
     *            the assembly representing the application
     * @param wirelets
     *            optional wirelets
     * @throws ApplicationLaunchException
     *             if the application failed to build
     * @throws RuntimeException
     *             if the build
     */
    public static void checkedRun(Assembly assembly, Wirelet... wirelets) throws ApplicationLaunchException {
        DEFAULT_DRIVER.launch(assembly, wirelets);
    }

    /**
     * 
     * @return
     */
    static App.Customizer customize() {
        throw new UnsupportedOperationException();
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
    public static ApplicationMirror mirror(Assembly assembly, Wirelet... wirelets) {
        return DEFAULT_DRIVER.mirrorOf(assembly, wirelets);
    }

    public static void print(Assembly assembly, Wirelet... wirelets) {
        // not in final version I think IDK, why not...
        // I think it is super usefull
        //// Maybe have something like enum PrintDetail (Minimal, Normal, Full)
        // ApplicationPrinter.Full, ApplicationPrinter.Normal
        mirror(assembly, wirelets).print();
    }

    /**
     * Builds and executes an application from the specified assembly and optional wirelets.
     * <p>
     * If the application is built successfully from the assembly. A single instance of the application will be created and
     * executed. This method will block until the application reaches the {@link RunState#TERMINATED terminated} state.
     * 
     * @param assembly
     *            the assembly representing the application
     * @param wirelets
     *            optional wirelets
     * @throws BuildException
     *             if the application failed to build
     * @throws UnhandledApplicationException
     *             if the fails during runtime
     * @see #checkedRun(Assembly, Wirelet...)
     */
    public static void run(Assembly assembly, Wirelet... wirelets) {
        DEFAULT_DRIVER.launch(assembly, wirelets);
    }

    public static void verify(Assembly assembly, Wirelet... wirelets) {
        // Det er jo bare Mirror uden at returnere det...
        DEFAULT_DRIVER.launch(assembly, wirelets);
    }

    // class probably... no need for an interface. Noone is going to call in
    interface Customizer {
        ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets);

        Customizer restartable();

        void run(Assembly assembly, Wirelet... wirelets);
    }
}

class Usage {
    public static void main(String[] args) {
        App.customize().restartable().run(null);
    }
}
