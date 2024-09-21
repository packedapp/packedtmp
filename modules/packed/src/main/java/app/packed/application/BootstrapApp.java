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
import java.util.function.Function;

import app.packed.assembly.Assembly;
import app.packed.container.Wirelet;
import app.packed.runtime.RunState;
import internal.app.packed.application.PackedBootstrapApp;

/**
 *
 */
// Will ResultApp?
public sealed interface BootstrapApp<A> permits PackedBootstrapApp {

    BootstrapApp<A> expectsResult(Class<?> resultType);

    /**
     * An application image is a stand-alone program, derived from an {@link app.packed.container.Assembly}, which runs the
     * application represented by the assembly — and no other.
     * <p>
     * By configuring an image ahead of time, the actual time to instantiation the system can be severely decreased often
     * down to a couple of microseconds. In addition to this, images can be reusable, so you can create multiple systems
     * from a single image.
     * <p>
     * Application images typically have two main use cases:
     *
     * GraalVM Native Image
     *
     * Recurrent instantiation of the same application.
     *
     * Creating artifacts in Packed is already really fast, and you can easily create one 10 or hundres of microseconds. But
     * by using artifact images you can into hundres or thousounds of nanoseconds.
     * <p>
     * Use cases: Extremely fast startup.. graal
     *
     * Instantiate the same container many times
     * <p>
     * Limitations:
     *
     * No structural changes... Only whole artifacts
     *
     * <p>
     * An image can be used to create new instances of {@link app.packed.application.App} or other applications. Artifact
     * images can not be used as a part of other containers, for example, via
     *
     * @see App#imageOf(Assembly, Wirelet...)
     */
    /**
     * Create a new base image by using the specified assembly and optional wirelets.
     *
     * @param assembly
     *            the assembly that should be used to build the image
     * @param wirelets
     *            optional wirelets
     * @return the new base image
     * @throws RuntimeException
     *             if the image could not be build
     */
    BaseImage<A> imageOf(Assembly assembly, Wirelet... wirelets);

    /**
     * Builds an application, launches it and returns an application interface instance (possible {@code void})
     * <p>
     * Typically, methods calling this method is not named {@code launch} but instead something that better reflects what
     * exactly launch means for the particular type of application.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @return an application interface instance or void
     * @throws RuntimeException
     *             if the application could not be built or failed to launch
     * @see App#run(Assembly, Wirelet...)
     */
    A launch(Assembly assembly, Wirelet... wirelets);

    /**
     * Builds an application and returns a mirror representing it.
     * <p>
     * If a special mirror supplied was set using {@link Composer#specializeMirror(Supplier)} when creating this bootstrap
     * app. The mirror returned from this method can be safely cast to the specialized application mirror.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @return a mirror representing the application
     * @throws RuntimeException
     *             if the application could not be build
     */
    ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets);

    /**
     * Builds and verifies an application.
     *
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     * @throws RuntimeException
     *             if the application could not be build or verified
     */
    void verify(Assembly assembly, Wirelet... wirelets);
}

interface BootstrapAppSandbox<A> {

    // Hvorfor ikke bruge BootstrapApp'en som en launcher???
    // Det betyder selv at vi altid skal chaine..
    // Men det er vel ok
    // map()->

    default Launcher<A> launcher() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new bootstrap app that maps the application using the specified mapper.
     *
     * @param <E>
     *            the type to map the application to
     * @param mapper
     *            the application mapper
     * @return the new bootstrap app
     */
    default <E> BootstrapApp<E> map(Function<? super A, ? extends E> mapper) {
        throw new UnsupportedOperationException();
    }

    /**
     * Augment the driver with the specified wirelets, that will be processed when building or instantiating new
     * applications.
     * <p>
     * For example, to : <pre> {@code
     * BootstrapApp<App> app = ...;
     * app = app.with(ApplicationWirelets.timeToRun(2, TimeUnit.MINUTES));
     * }</pre>
     *
     * ApplicationW
     * <p>
     * This method will make no attempt of validating the specified wirelets.
     *
     * <p>
     * Wirelets that were specified when creating the driver, or through previous invocation of this method. Will be
     * processed before the specified wirelets.
     *
     * @param wirelets
     *            the wirelets to add
     * @return the new bootstrap app
     */
    default BootstrapApp<A> with(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
        // return new BootstrapApp<>(new BootstrapAppSetup(setup.mirrorSupplier, setup.template.withWirelets(wirelets),
        // setup.applicationLauncher));
    }

    /**
     * A launcher is used before an application is launched.
     */
    public interface Launcher<A> {

        default boolean isUseable() {
            // An image returns true always

            // Optional<A> tryLaunch(Wirelet... wirelets)???
            return true;
        }

        A launch(Assembly assembly, Wirelet... wirelets);

        // /**
        // * Launches an instance of the application that this image represents.
        // *
        // * @throws ApplicationLaunchException
        // * if the application failed to launch
        // * @throws IllegalStateException
        // * if the image has already been used to launch an application and the image is not a reusable image
        // * @return the application interface if available
        // */
        // default A checkedLaunch() throws ApplicationLaunchException {
//         return checkedLaunch(new Wirelet[] {});
        // }
        //
        // default A checkedLaunch(Wirelet... wirelets) throws ApplicationLaunchException {
//         throw new UnsupportedOperationException();
        // }

        /**
         * Returns the launch mode of application(s) created by this image.
         *
         * @return the launch mode of the application
         *
         */
        RunState launchMode(); // usageMode??

        /**
         * Returns a new launcher that maps the result of the launch.
         *
         * @param <E>
         *            the type to map the launch result to
         * @param mapper
         *            the mapper
         * @return a new application image that maps the result of the launch
         */
        <E> Launcher<E> map(Function<? super A, ? extends E> mapper);

        Optional<ApplicationMirror> mirror();

        // Hmmmmmmm IDK
        // Could do sneaky throws instead
        A throwingUse(Wirelet... wirelets) throws Throwable;

        default BaseImage<A> with(Wirelet... wirelets) {
            // Egentlig er den kun her pga Launcher
            throw new UnsupportedOperationException();
        }

        /**
         * Returns a mirror for the application if available.
         *
         * @param image
         *            the image to extract the application mirror from
         * @return a mirror for the application
         * @throws UnsupportedOperationException
         *             if the specified image was not build with BuildWirelets.retainApplicationMirror()
         */
        // Eller bare Optional<Mirror>
        static ApplicationMirror extractMirror(BaseImage<?> image) {
            throw new UnsupportedOperationException();
        }

        // ALWAYS HAS A CAUSE
        // Problemet jeg ser er, hvad skal launch smide? UndeclaredThrowableException

        // App.execute
        // App.checkedExecute <---

        // Maaske er det LifetimeLaunchException
//        public static class ApplicationLaunchException extends Exception {
        //
//            private static final long serialVersionUID = 1L;
        //
//            RunState state() {
//                return RunState.INITIALIZED;
//            }
//        }
    }
}