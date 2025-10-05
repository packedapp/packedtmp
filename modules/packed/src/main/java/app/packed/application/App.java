package app.packed.application;

import java.util.concurrent.TimeUnit;

import app.packed.application.App.Launcher;
import app.packed.assembly.Assembly;
import app.packed.binding.Key;
import app.packed.container.Wirelet;
import app.packed.runtime.RunState;
import app.packed.runtime.StopOption;

/**
 * Represents the main entry point for executing and managing Packed applications. This interface provides methods for
 * running, analyzing, and controlling the lifecycle of applications built using the Packed framework.
 *
 * <p>
 * The App interface implements {@link AutoCloseable}, allowing it to be used with try-with-resources statements for
 * automatic resource management. It provides various static factory methods for different ways of creating and
 * executing applications, as well as instance methods for controlling running applications.
 *
 * <p>
 * Applications can be in different states as defined by {@link RunState}, and their lifecycle can be controlled through
 * methods like {@link #stop(StopOption...)} and monitored via {@link #awaitState(RunState, long, TimeUnit)}.
 *
 * <p>
 * Example usage: <pre>{@code
 * // Run an application to completion
 * App.run(myAssembly);
 *
 * // Start an application and control it
 * try (App app = App.start(myAssembly)) {
 *     // Do something while the app is running
 *     app.stop();
 * }
 * }</pre>
 *
 * @see Assembly
 * @see RunState
 * @see Wirelet
 */
// Rename to SimpleApp???
public interface App extends AutoCloseable, ApplicationInterface {

    /**
     * Waits for the application to reach a specified state, with a timeout.
     *
     * <p>
     * This method blocks until either:
     * <ul>
     * <li>The application reaches the specified state
     * <li>The specified timeout period elapses
     * <li>The current thread is interrupted
     * </ul>
     *
     * @param state
     *            the target state to wait for
     * @param timeout
     *            the maximum time to wait
     * @param unit
     *            the time unit of the timeout argument
     * @return {@code true} if the application reached the specified state, {@code false} if the timeout elapsed first
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting
     */
    boolean awaitState(RunState state, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Closes this application, stopping it if it's still running.
     *
     * <p>
     * This method provides compatibility with the try-with-resources statement and is equivalent to calling {@code stop()}.
     * If the application has already terminated, this method has no effect.
     */
    @Override
    void close();

    /**
     * Returns the current state of the application.
     *
     * @return the current {@link RunState} of the application
     */
    RunState state();

    /**
     * Initiates an orderly shutdown of the application.
     *
     * <p>
     * The exact behavior of the shutdown process can be customized using the provided stop options.
     *
     * @param options
     *            the options to control the shutdown process
     */
    void stop(StopOption... options);

    /**
     * Creates an application image that can be used to launch a single instance of the application.
     *
     * <p>
     * The returned image represents a pre-built application that can be launched at a later time. By default, the image can
     * only be used to launch a single instance. For reusable images, use {@code ApplicationImageWirelets.reusable()} in the
     * wirelets.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets for configuration
     * @return an image that can be used to launch the application
     */
    static Image imageOf(Assembly assembly, Wirelet... wirelets) {
        return new PackedApp.AppImage(PackedApp.BOOTSTRAP_APP.imageOf(assembly, wirelets));
    }

    static Launcher launcherOf(Assembly assembly, Wirelet... wirelets) {
        return new PackedApp.AppLauncher(PackedApp.BOOTSTRAP_APP.launcher(assembly, wirelets));
    }

    static void main(String[] args) {
        // launcher(
        CliLaunchers.mainArgs(App.launcherOf(null), args).start();
    }

    /**
     * Creates a mirror representation of the application for analysis purposes.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets for configuration
     * @return a mirror representing the application structure
     * @throws RuntimeException
     *             if the application fails to build
     */
    static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return PackedApp.BOOTSTRAP_APP.mirrorOf(assembly, wirelets);
    }

    /**
     * Builds an application and prints its structure to {@code System.out}.
     * <p>
     * If you need more detailed output you can use {@link ApplicationMirror#printer()}.
     * <p>
     * This is a convenience method for debugging and analysis purposes.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets for configuration
     */
    static void print(Assembly assembly, Wirelet... wirelets) {
        mirrorOf(assembly, wirelets).print();
    }

    /**
     * Builds and runs an application to completion.
     *
     * <p>
     * This method blocks until the application reaches the {@link RunState#TERMINATED} state.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets for configuration
     * @throws RuntimeException
     *             if the application fails to build or run
     */
    static void run(Assembly assembly, Wirelet... wirelets) {
        PackedApp.BOOTSTRAP_APP.launch(RunState.TERMINATED, assembly, wirelets);
    }

    /**
     * Tests an application using the provided test consumer.
     *
     * @param assembly
     *            the application's assembly
     * @param cno
     *            the test consumer
     * @param wirelets
     *            optional wirelets for configuration
     * @throws UnsupportedOperationException
     *             currently not implemented
     */
    // Alternativ AppTester.of(Assembly, Wirelets)
//
//    static void test(Assembly assembly, Consumer<? /* TestObject */> cno, Wirelet... wirelets) {
//        throw new UnsupportedOperationException();
//    }

    /**
     * Builds and starts an application, returning when it reaches the {@link RunState#RUNNING} state.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets for configuration
     * @return the running application instance
     * @throws app.packed.build.BuildException
     *             if the application failed to build
     * @throws UnhandledApplicationException
     *             if the application failed to start
     */
    static App start(Assembly assembly, Wirelet... wirelets) {
        return PackedApp.BOOTSTRAP_APP.launch(RunState.RUNNING, assembly, wirelets);
    }

    /**
     * Builds and verifies an application without running it.
     *
     * <p>
     * This method can be used to validate the application structure and configuration without actually executing it.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets for configuration
     * @throws RuntimeException
     *             if the application fails to build
     */
    static void verify(Assembly assembly, Wirelet... wirelets) {
        PackedApp.BOOTSTRAP_APP.verify(assembly, wirelets);
    }

    /**
     * Represents a pre-built application image that can be used to launch application instances.
     */
    interface Image extends ApplicationImage, App.Launcher {}

    interface Launcher extends ApplicationLauncher {

        // Config also


        @Override
        default <T> Launcher provide(Key<? super T> key, T value) {
            return null;
        }

        default Launcher alwaysRestart() {
            return this;
        }

        // rename to arg

        // @LaunchArgument/@LaunchArg <--- Injectable

        default Launcher ignoreAllExceptions() {
            return this;
        }

        /**
         * Runs the application to completion.
         *
         * @param wirelets
         *            optional runtime wirelets
         * @throws UnhandledApplicationException
         *             if the application failed during executing
         */
        void run();

        /**
         * Starts the application and waits until it is fully running.
         *
         * @param wirelets
         *            optional runtime wirelets
         * @return the running application instance
         * @throws UnhandledApplicationException
         *             if the application fails to start
         */
        App start();
    }
}

// Det er jo kun images der er problemet...
class CliLaunchers {

    static Launcher mainArgs(Launcher launcher, String[] args) {
        return launcher;
    }

}

///**
//* Creates a printer for detailed application structure output.
//*
//* @param assembly
//*            the application's assembly
//* @param wirelets
//*            optional wirelets for configuration
//* @return a printer for the application structure
//*/
//// Maybe skip this one, and only have print()
//static MirrorPrinter printer(Assembly assembly, Wirelet... wirelets) {
//  return mirrorOf(assembly, wirelets).printer();
//}

///**
//* Runs an application with exception checking, wrapping any unhandled exceptions in {@link ApplicationException}.
//*
//* <p>
//* This method is similar to {@link #run(Assembly, Wirelet...)}, but provides additional safety by ensuring all
//* exceptions are wrapped in {@link ApplicationException}.
//*
//* @param assembly
//*            the application's assembly
//* @param wirelets
//*            optional wirelets for configuration
//* @throws ApplicationException
//*             if the application fails during execution
//* @throws RuntimeException
//*             if the application fails to build
//*/
//static void checkedRun(Assembly assembly, Wirelet... wirelets) throws UnhandledApplicationException {
//  PackedApp.BOOTSTRAP_APP.checkedLaunch(RunState.TERMINATED, assembly, wirelets);
//}
//
///**
//* Starts an application with exception checking, wrapping any unhandled exceptions in {@link ApplicationException}.
//*
//* @param assembly
//*            the application's assembly
//* @param wirelets
//*            optional wirelets for configuration
//* @return a running instance of the application
//* @throws ApplicationException
//*             if the application fails to start
//* @throws RuntimeException
//*             if the application fails to build
//*/
//static App checkedStart(Assembly assembly, Wirelet... wirelets) throws UnhandledApplicationException {
//  return PackedApp.BOOTSTRAP_APP.checkedLaunch(RunState.RUNNING, assembly, wirelets);
//}
