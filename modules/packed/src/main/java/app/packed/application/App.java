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

import java.util.Optional;

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

    /** The default driver. */
    private static final ApplicationDriver<Void> DEFAULT_DRIVER = ApplicationDriver.builder().buildVoid();

    /** Not today Satan, not today. */
    private App() {}

    /**
     * 
     * @return
     */
    static App.Customizer customize() {
        throw new UnsupportedOperationException();
    }

    public static App.Launcher newImage(Assembly assembly, Wirelet... wirelets) {
        return new Launcher(DEFAULT_DRIVER.newImage(assembly, wirelets));
    }

    /**
     * Builds an application and returns a launcher that can be used to launch a <b>single</b> instance of the application.
     * <p>
     * If you need to launch multiple instances of the same application use {@link #newImage(Assembly, Wirelet...)}.
     * Or maybe use that Application wirelet... I don't really think it is that common
     * 
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @return a launcher that can be used to launch a single instance of the application
     */
    public static App.Launcher newLauncher(Assembly assembly, Wirelet... wirelets) {
        return new Launcher(DEFAULT_DRIVER.newLauncher(assembly, wirelets));
    }

    /**
     * Builds an application from the specified assembly and returns a mirror representing the application.
     * 
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @return a mirror representing the application
     * @throws RuntimeException
     *             if the application could not be build
     */
    public static ApplicationMirror newMirror(Assembly assembly, Wirelet... wirelets) {
        return DEFAULT_DRIVER.newMirror(assembly, wirelets);
    }

    public static void print(Assembly assembly, Object printDetails, Wirelet... wirelets) {
        // printDetails=Container, Assemblies,////
        newMirror(assembly, wirelets).print();
    }

    public static void print(Assembly assembly, Wirelet... wirelets) {
        // not in final version I think IDK, why not...
        // I think it is super usefull
        //// Maybe have something like enum PrintDetail (Minimal, Normal, Full)
        // ApplicationPrinter.Full, ApplicationPrinter.Normal
        newMirror(assembly, wirelets).print();
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
     * @throws RuntimeException
     *             if the application failed to build or run
     * @see #checkedRun(Assembly, Wirelet...)
     */
    public static void run(Assembly assembly, Wirelet... wirelets) {
        DEFAULT_DRIVER.launch(assembly, wirelets);
    }

    public static void verify(Assembly assembly, Wirelet... wirelets) {
        DEFAULT_DRIVER.verify(assembly, wirelets);
    }

    // class probably... no need for an interface. Noone is going to call in
    interface Customizer {
        ApplicationMirror newMirror(Assembly assembly, Wirelet... wirelets);

        // exception application panic/build strategy

        Customizer restartable();

        void run(Assembly assembly, Wirelet... wirelets);
    }

    public static final class Launcher {
        
        private final ApplicationLauncher<?> original;

        Launcher(ApplicationLauncher<?> original) {
            this.original = original;
        }

        public void run() {
            original.launch();
        }

        public void run(Wirelet...wirelets) {
            original.launch(wirelets);
        }
    }
}

// Tror godt vi kan vaere et naested interface...
// man kalder altid .app
// I don't think we catch BuildException here

// Integration with CompletableFuture and other futures

// ApplicationLauncher? launcherOf?

// Den fungere ikke super godt med fx DaemonApp. Som jo mere er en slags Future
interface SandboxAppResult<A> {
    Optional<Throwable> cause();

    int exitCode(); // or is this cliApp???

    boolean isFailed();

    boolean isSuccess();

    A result();
}

class Usage {
    public static void main(String[] args) {
        App.customize().restartable().run(null);
    }
}
