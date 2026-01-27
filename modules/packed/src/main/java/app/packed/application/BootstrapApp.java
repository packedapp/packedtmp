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
package app.packed.application;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.assembly.Assembly;
import app.packed.bean.Bean;
import app.packed.container.Wirelet;
import app.packed.lifecycle.LifecycleKind;
import app.packed.lifecycle.RunState;
import internal.app.packed.application.PackedBootstrapImage;
import internal.app.packed.application.PackedBootstrapImage.ImageMapped;

/**
 * A bootstrap app is a special type of application that can be used to create other (non-bootstrap) applications.
 * <p>
 * Bootstrap apps cannot directly modify the applications that it bootstraps. It cannot, for example, install a new
 * extension in the application. However, it can say it can only bootstrap applications that have the extension
 * installed, failing with a build exception if the developer does not install the extension. As such, the bootstrap app
 * can setup requirements for the application that it bootstraps. But it cannot directly make the needed changes to the
 * bootstrapped application.
 * <p>
 * Bootstrap applications are rarely used directly by users. Instead users typically use thin wrappers such as
 * {@link App} or {@link app.packed.service.ServiceLocator} to create new applications. However, if greater control of
 * how an application behaves, a user may create their own bootstrap application.
 * <p>
 * Normally, you would never create more than a single instance of a bootstrap app.
 * <p>
 * Bootstrap application instances are safe for concurrent usage.
 *
 * @param <E>
 *            the type of applications this bootstrap app creates
 */
public sealed interface BootstrapApp<I> extends ApplicationInterface permits PackedBootstrapApp, MappedBootstrapApp {

//    // Thrown on an Unhandled exception
//    // Alternativ APE er Runtime... Yeah I think so
//    I checkedLaunch(RunState state, Assembly assembly, Wirelet... wirelets) throws UnhandledApplicationException;

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
    Image<I> imageOf(Assembly assembly, Wirelet... wirelets);

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
     * @throws UnhandledApplicationException
     *             if the application threw an exception that could not be handled
     * @see App#run(Assembly, Wirelet...)
     */
    default I launch(RunState state, Assembly assembly, Wirelet... wirelets) {
        return launcher(assembly, wirelets).launch(state);
    }

    Launcher<I> launcher(Assembly assembly, Wirelet... wirelets);

    /**
     * Creates a new bootstrap app that maps the application using the specified mapper.
     *
     * @param <E>
     *            the type to map the application to
     * @param mapper
     *            the application mapper
     * @return the new bootstrap app
     */
    // I think this only makes sense if someone wants to expose the bootstrap app to users
    default <E> BootstrapApp<E> map(Function<? super I, ? extends E> mapper) {
        requireNonNull(mapper, "mapper is null");
        return new MappedBootstrapApp<>(this, mapper);
    }

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

    // withExpectingResult
    BootstrapApp<I> withExpectsResult(Class<?> resultType);

    static <A> BootstrapApp<A> of(LifecycleKind lifecycleKind, Bean<A> bean) {
        return PackedBootstrapApp.of(lifecycleKind, bean);
    }

    /**
     * A base image represents a pre-built application.
     * <p>
     * Instances of this class are typically not exposed to end-users. Instead it is typically wrapped in another image
     * class such as {@link App.Image}.
     */
    sealed interface Image<A> extends ApplicationImage, BootstrapApp.Launcher<A> permits PackedBootstrapImage {

        // Descriptors??? iisLazy, Mirror?

        // Do we want a more specific ApplicationResult? Something where the state is??
        // Maybe we can take a BiConsumer(ErrorContext, A)

        // Problem is here when is

        // Meningen er at prøve at styre fejl håndteringen bedre
        // <T> T BiFunction<@Nullable A, ErrorHandle e>
        //
//        default Result<A> compute(Object unhandledErrorHandler, Wirelet... wirelets) {
//            throw new UnsupportedOperationException();
//        }
        //
//        // Failure before A is created,
//        // Failure after A is created
//        // Action -> Return something, or throw something
//        // Tror ikke det giver mening foerend vi har en god error handling story
//        default Result<A> compute(Wirelet... wirelets) {
//            throw new UnsupportedOperationException();
//        }

        /**
         * Returns a new base image that maps this image.
         *
         * @param <E>
         *            the type to map the image to
         * @param mapper
         *            the mapper
         * @return a new base image that maps the result of this image
         */
        default <E> Image<E> map(Function<? super A, ? extends E> mapper) {
            requireNonNull(mapper, "mapper is null");
            return new ImageMapped<>(this, mapper);
        }
    }

    /**
    *
    */
    interface Launcher<A> extends ApplicationLauncher {

        /**
         * Launches an instance of the application that this image represents in the specified state.
         * <p>
         * What happens here is dependent on the underlying application template. The behaviour of this method is identical to
         * {@link BootstrapApp#launch(Assembly, Wirelet...)}.
         *
         * @param state
         *            the state the application should launch into
         * @return an application instance
         *
         * @see BootstrapApp#launch(Assembly, Wirelet...)
         */
        A launch(RunState state);
    }
}

// I don't if we have any usecases Maybe just skip it
record MappedBootstrapApp<A, E>(BootstrapApp<A> app, Function<? super A, ? extends E> mapper) implements BootstrapApp<E> {

    /** {@inheritDoc} */
    @Override
    public <T> BootstrapApp<T> map(Function<? super E, ? extends T> mapper) {
        return new MappedBootstrapApp<>(app, this.mapper.andThen(mapper));
    }

    /** {@inheritDoc} */
    @Override
    public BootstrapApp<E> withExpectsResult(Class<?> resultType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Image<E> imageOf(Assembly assembly, Wirelet... wirelets) {
        Image<A> ba = app.imageOf(assembly, wirelets);
        return ba.map(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return app.mirrorOf(assembly, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public void verify(Assembly assembly, Wirelet... wirelets) {
        app.verify(assembly, wirelets);
    }
//
//    /** {@inheritDoc} */
//    @Override
//    public E checkedLaunch(RunState state, Assembly assembly, Wirelet... wirelets) throws UnhandledApplicationException {
//        A result = app.checkedLaunch(state, assembly, wirelets);
//        E e = mapper.apply(result);
//        return e;
//
//    }

    /** {@inheritDoc} */
    @Override
    public Launcher<E> launcher(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}

///**
//* Augment the driver with the specified wirelets, that will be processed when building or instantiating new
//* applications.
//* <p>
//* For example, to : <pre> {@code
//* BootstrapApp<App> app = ...;
//* app = app.with(ApplicationWirelets.timeToRun(2, TimeUnit.MINUTES));
//* }</pre>
//*
//* ApplicationW
//* <p>
//* This method will make no attempt of validating the specified wirelets.
//*
//* <p>
//* Wirelets that were specified when creating the driver, or through previous invocation of this method. Will be
//* processed before the specified wirelets.
//*
//* @param wirelets
//*            the wirelets to add
//* @return the new bootstrap app
//*/
//// Before orAfter <---- Just this question alone makes we want to not support it
//@Deprecated
//default BootstrapApp<I> withWirelets(boolean before, Wirelet... wirelets) {
//  // Lad os ogsaa lige se med expectsResult, inde vi implementere det
//  throw new UnsupportedOperationException();
//}