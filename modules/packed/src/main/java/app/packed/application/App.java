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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import app.packed.assembly.Assembly;
import app.packed.container.Wirelet;
import app.packed.runtime.RunState;
import app.packed.runtime.StopOption;
import internal.app.packed.application.PackedApp;

/**
 * An entry point for... This class contains a number of methods that can be to execute or analyze programs that are
 * written use Packed.
 *
 * An entry point for all the various types of applications that are available in Packed.
 * <p>
 * This class does not prov For creating application -instances -mirrors and -images.
 */
// [Checked|Not-Checked]Launch, Mirror, Launcher, ReuseableLauncher

// Only thing you can get out is information about errors

// App must either have threads running after it has started, or an entry point.
// Ellers er det et sort hul...
public interface App extends AutoCloseable {

    /**
     * Blocks until all tasks within the application have completed after a shutdown request, or the timeout occurs, or the
     * current thread is interrupted, whichever happens first.
     *
     * @param timeout
     *            the maximum time to wait
     * @param unit
     *            the time unit of the timeout argument
     * @return {@code true} if the application terminated and {@code false} if the timeout elapsed before termination
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    boolean awaitState(RunState state, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Closes the app (synchronously).
     * <p>
     * Calling this method is equivalent to calling {@code host().stop()}, but this method is called close in order to
     * support try-with resources via {@link AutoCloseable}.
     *
     * <p>
     * If the app has already terminated, invoking this method has no effect.
     *
     **/
    @Override
    void close();

    /** {@return the current state of the app} */
    RunState state();

    // Maybe Options are per App type and then maps into something else???
    // Cancel makes no sense, for example, well maybe.
    // pause() makes no sense -> Because we do not have a resume method
    // But then again restart
    void stop(StopOption... options);

    /**
     * This method is identical to {@link #run(Assembly, Wirelet...)} except that it will never wraps any unhandled
     * exceptions from the application.
     *
     * @param assembly
     * @param wirelets
     * @throws Throwable
     */
    @SuppressWarnings("unused")
    static void checkedRun(RunState state, Assembly assembly, Wirelet... wirelets) throws ExecutionException {
        PackedApp.BOOTSTRAP.launch(state, assembly, wirelets);
    }

    /**
     * Builds the application from the specified assembly and returns a image that can be used to launch a <b>single</b>
     * instance of the application at a later point.
     * <p>
     * If you need to launch multiple instances of the same application. You can specify
     * {@code ApplicationImageWirelets.reusable()} in the wirelet part of this method.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @return an image that can be used to launch a single instance of the application
     */
    static App.Image imageOf(Assembly assembly, Wirelet... wirelets) {
        return new PackedApp.AppImage(PackedApp.BOOTSTRAP.imageOf(assembly, wirelets));
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
    static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return PackedApp.BOOTSTRAP.mirrorOf(assembly, wirelets);
    }

    /**
     * Builds an application and prints out its structure using {@code System.out}.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     */
    static void print(Assembly assembly, Wirelet... wirelets) {
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
    static void run(Assembly assembly, Wirelet... wirelets) {
        PackedApp.BOOTSTRAP.launch(RunState.TERMINATED, assembly, wirelets);
    }

    static App start(Assembly assembly, Wirelet... wirelets) {
        return PackedApp.BOOTSTRAP.launch(RunState.RUNNING, assembly, wirelets);
    }

    // Kunne jo vaere man gerne ville returnere et eller andet???
    static void test(Assembly assembly, Consumer<? /* TestObject */> cno, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // Problemet er lidt her at hvis App bestemmer noget som helst... andet en et raw interface
    // Saa skal vi ogsaa bruge den naar vi tester. Fordi ellers er det jo en anden application

    // Paa en eller anden maade vil vi teste nogle ting
    // Er det her???? Eller et andet sted?
    // Altsaa meningen er vel vi bygger en app med alt muligt gejl

    // Maaske Brug Verify...
    // Og koer den med Tester.xyz IDK. Super godt sporgsmaal

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
    static void verify(Assembly assembly, Wirelet... wirelets) {
        PackedApp.BOOTSTRAP.verify(assembly, wirelets);
    }

    /** An image for App. */
    // Mirror???? Would be nice to know what is in the image...
    interface Image {

        /** Runs the application represented by this image. */
        void run();

        /**
         * Runs the application represented by this image.
         *
         * @param wirelets
         *            optional wirelets
         * @throws app.packed.container.WireletException
         *             if a build wirelet is exposed
         */
        void run(Wirelet... wirelets);

        /**
         * Starts the app and waits until it has fully started.
         *
         * @return
         */
        App start();

        App start(Wirelet... wirelets);
    }
}
