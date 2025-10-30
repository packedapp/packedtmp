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
package app.packed.container;

import java.util.function.Function;

import app.packed.application.ApplicationMirror;
import app.packed.bean.lifecycle.LifecycleModel;
import app.packed.context.Context;
import app.packed.runtime.StopOption;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.container.wirelets.InternalBaseWirelet;

/**
 * Wirelets that can be used when building an application.
 * <p>
 * Attempting to use any of these wirelets on anything else then when building an exception throws WireletNotApplicable,
 * InappropiateWireletException
 */
// Was BaseWirelets - But could be a misunderstood with regards to BaseExtension
// Maybe ApplicationWirelets if only usable on the Application
// Something like propagateContext is not though
public final class Wirelets {
    private Wirelets() {}

    // How does this work with scope?
    public static Wirelet argList(String... args) {
        throw new UnsupportedOperationException();
    }

    Wirelet propageAllContexts() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    static final Wirelet propageteContext(Class<? extends Context<?>>... contexts) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    static final Wirelet propageteContextIfPresent(Class<? extends Context<?>>... contexts) {
        throw new UnsupportedOperationException();
    }

    // A build exception is never retryable

    /**
     * By default Packed does not wrap runtime exceptions in a Build Exception
     *
     * @return
     */
    // Maybe even allow RuntimeException -> RuntimeException mapper?
    static Wirelet alwaysThrowBuildException() {
        throw new UnsupportedOperationException();
    }

    // Should probably be a lifetime wirelet
    public static Wirelet conditionalOnApplicationLifetimeKind(LifecycleModel kind, Wirelet w) {
        throw new UnsupportedOperationException();
    }

    // Start codegen in background threads

    /**
     * Does not build the application until it is needed.
     *
     * @return a wirelet
     */
    // Fungere kun for image, eller non-root applications

    // Hah, hvad med application mirror... Jamen saa bliver den jo bare ignoret.
    // What about layden????
    public static Wirelet buildApplicationLazily() {
        // Hmm I'm not super stocked, on the other hand explosion of methods
        // I think we might want an ApplicationBuilder???
        // Kan jo ikke bare bruge den med fx Entrypoints...

        return ApplicationBuildLazilyWirelet.INSTANCE;
    }

    static final class ApplicationBuildLazilyWirelet extends InternalBaseWirelet {
        private static final ApplicationBuildLazilyWirelet INSTANCE = new ApplicationBuildLazilyWirelet();

        /** {@inheritDoc} */
        @Override
        public void onBuild(PackedContainerInstaller<?> installer) {
            checkIsApplication(installer, this); // maybe explicit error msg
            installer.applicationInstaller.optionBuildApplicationLazy = true;
        }
    }

    // spawn 10 threads, that creates method handles...
    // There are a couple of strategies
    // Normally we eagerly generate all code
    /// lazyCodegen().guardBy(System.getProperty("sdfsdf")=="noLazy");
    // will build the application, but not generate the code until last minute
    static Wirelet lazyCodegen() {
        // lazyBuild will always triump
        throw new UnsupportedOperationException();
    }

    /**
     * By default images created using {@link App#imageOf(app.packed.container.Assembly, Wirelet...)} and similar methods
     * can only be used a single time. Once launched, the reference to
     * <p>
     * By specifying this wirelet when creating an application image. The image can be used any number of times.
     * <p>
     * Specifying this wirelet when launching an application immediately create application mirrors
     *
     * @return
     * @see App#imageOf(app.packed.container.Assembly, Wirelet...)
     */
    // I actually think this is only useful for root images
    public static Wirelet resuableImage() {
        // Vi har droppet at lave flere metoder imageOf, imageResuableOf
        // Vi laver et image eagerly, og det kan launches 1 gang.
        // Saa faar vi fail-faster vi hvis vi proever fx at launche twice

        return ApplicationReusableImageWirelet.INSTANCE;
    }

    static final class ApplicationReusableImageWirelet extends InternalBaseWirelet {
        private static final ApplicationReusableImageWirelet INSTANCE = new ApplicationReusableImageWirelet();

        /** {@inheritDoc} */
        @Override
        public void onBuild(PackedContainerInstaller<?> installer) {
            checkIsApplication(installer, this); // maybe explicit error msg
            installer.applicationInstaller.optionBuildReusableImage = true;
        }
    }

    /**
     * Installs a shutdown hook similar to {@link #shutdownHook(StopOption...)} exact that the thread passed to
     * {@link Runtime#addShutdownHook(Thread)} can be customized.
     *
     * @param threadFactory
     *            a factory that is used to create the shutdown hook thread
     * @param options
     *            stop options
     * @return a shutdown hook wirelet
     * @see Runtime#addShutdownHook(Thread)
     */
    // Why shouldn't I be able to use this on runtime???
    public static Wirelet shutdownHook(Function<Runnable, Thread> threadFactory, StopOption... options) {
        // When should we install it? Just after we have been fully initilized?
        // I think so, alternative is as the first operation when starting
        final class ApplicationShutdownHookWirelet extends InternalBaseWirelet {

            /** {@inheritDoc} */
            @Override
            public void onBuild(PackedContainerInstaller<?> installer) {}
        }
        return new ApplicationShutdownHookWirelet();
    }

    /**
     * Returns a wirelet that will install a shutdown hook for an application.
     * <p>
     * As shutting down the root will automatically shutdown all of its child applications. Attempting to specify a shutdown
     * hook wirelet when launching a non-root application will fail with an exception.
     * <p>
     * Attempting to use more than one shutdown hook wirelet on an application will fail
     *
     * Attempting to use it anywhere else than on a root application will fail
     *
     * Attempting to use this wirelet for an application in an {@link LifetimeKind#UNMANAGED} will fail
     *
     * @return a shutdown hook wirelet
     * @see #shutdownHook(Function, app.packed.lifetime.sandbox.StopOption...)
     * @see Runtime#addShutdownHook(Thread)
     */
    // Ogsaa skrive noget om hvad der sker hvis vi stopper
    // Multiple shutdown hooks? I don't think we should do any checks.
    // Problem is if we have ApplicationConfiguration.installShutdownHook()
    // And CliApp uses a wirelet at the same time
    // Maybe only ApplicationConfiguration,
    public static Wirelet shutdownHook(StopOption... options) {
        // https://www.baeldung.com/spring-boot-shutdown
        return shutdownHook(r -> new Thread(r), options);
    }

    // Nahh, saa maa man have den injected i en bean. Men fraek nok ellers/
    // Vi supportere ikke som udgangspunkt mirrors uden et injection site.
    // Men hmm, vi har vel altid muligheden i forbindelse med launch????
    // Har vi en ApplicationStartToken??? Man kan injecte og forspoerge paa
    static Wirelet startupBanner(Function<ApplicationMirror, String> bannerMaker) {
        throw new UnsupportedOperationException();
    }

    // IDK, det kunne ogsaa bare vaere en bean man registrere
    // baseExtension().startupBanner();
    // Tror faktisk ikke det er en wirelet
    public static Wirelet startupBanner(String banner) {
        throw new UnsupportedOperationException();
    }

    // excludes start?? IDK
    // Altsaa det giver jo mening at faa en total tid taenker jeg
    // Omvendt tror jeg ikke det giver mening at tage build time med...
    //

    // timeToStart() If the application does not start within the X time
    // Shut it down

}

//// Can only be used together with image
//public static Wirelet delayedBuild() {
//  // delayed vs lazyBuild?
//  throw new UnsupportedOperationException();
//}