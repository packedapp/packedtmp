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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.bean.BeanExtensionPoint;
import app.packed.container.AbstractComposer;
import app.packed.container.Assembly;
import app.packed.container.ContainerWrapperCompanion;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.lifetime.LifetimeKind;
import app.packed.lifetime.managed.ManagedLifetimeController;
import app.packed.operation.Op;
import app.packed.service.ServiceLocator;
import internal.app.packed.application.PackedApplicationDriver;

/**
 * Application drivers are responsible for creating (root) applications.
 * <p>
 * Packed comes with a number of predefined application drivers:
 * <p>
 * Application drivers are normally never exposed to end users.
 * 
 * <p>
 * If these are not sufficient, it is very easy to build your own.
 * 
 * Which is probably your best bet is to look at the source code of them to create your own.
 * <p>
 * This class can be used to create custom artifact types if the built-in artifact types such as {@link App} and
 * {@link ServiceLocator} are not sufficient. In fact, the default implementations of both {@link App} and
 * {@link ServiceLocator} uses an artifact driver themselves.
 * <p>
 * Normally, you never create more than a single instance of an application driver.
 * 
 * @param <A>
 *            the type of application interface this driver creates.
 * @see App#driver()
 */
// Environment + Application Interface + Result

// Refactoring
//// En build(Wirelet... wirelets) metode
//// Companion objects must be added in order of the recieving MethodHandle

@SuppressWarnings("rawtypes")
public sealed interface ApplicationDriver<A> permits PackedApplicationDriver {

    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     * 
     * @return a set of disabled extensions
     */
    Set<Class<? extends Extension<?>>> bannedExtensions();

    /**
     * Create a new application image by using the specified assembly and optional wirelets.
     * 
     * @param assembly
     *            the assembly that should be used to build the image
     * @param wirelets
     *            optional wirelets
     * @return the new image
     * @throws RuntimeException
     *             if the image could not be build
     */
    // Andre image optimizations
    //// Don't cache beans info
    /// Nu bliver jeg i tvivl igen... Fx med Tester
    ApplicationImage<A> imageOf(Assembly assembly, Wirelet... wirelets);

    /**
     * Builds an application using the specified assembly and optional wirelets and returns a new instance of it.
     * <p>
     * This method is typical not called directly by end-users. But indirectly through methods such as
     * {@link App#run(Assembly, Wirelet...)} .
     * 
     * @param assembly
     *            the main assembly of the application
     * @param wirelets
     *            optional wirelets
     * @return the launched application instance
     * @throws RuntimeException
     *             if the image could not be build
     * @throws LifecycleException
     *             if the application failed to initialize
     * @throws RuntimeException
     *             if the application had an executing phase and it fails
     * @see App#run(Assembly, Wirelet...)
     */
    A launch(Assembly assembly, Wirelet... wirelets); // newInstance

    /**
     * Returns whether or not applications produced by this driver have an {@link ManagedLifetimeController}.
     * <p>
     * Applications that are not runnable will always be launched in the Initial state.
     * 
     * @return whether or not the applications produced by this driver are runnable
     */
    LifetimeKind lifetimeKind();

    /**
     * Creates a new application mirror from the specified assembly and optional wirelets.
     * <p>
     * The {@link BuildTaskInfo application descriptor} will returns XXX at build time.
     * 
     * @param assembly
     *            the assembly to create an application mirror from
     * @param wirelets
     *            optional wirelets
     * @return an application mirror
     * 
     * @throws RuntimeException
     *             if the mirror could not be build
     */
    ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets);

    ApplicationImage<A> reusableImageOf(Assembly assembly, Wirelet... wirelets);

    void verify(Assembly assembly, Wirelet... wirelets);

    /**
     * Augment the driver with the specified wirelets, that will be processed when building or instantiating new
     * applications.
     * <p>
     * For example, to : <pre> {@code
     * ApplicationDriver<App> driver = App.driver();
     * driver = driver.with(ApplicationWirelets.timeToRun(2, TimeUnit.MINUTES));
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
     * @return the augmented application driver
     */
    ApplicationDriver<A> with(Wirelet... wirelets); // not much of usecase anymore I think

    /**
     * Returns a new {@code ApplicationDriver} builder that will build an application driver without a wrapper bean.
     *
     * @return the new builder
     */
    static Builder<Void> builder() {
        return new PackedApplicationDriver.Builder<>(null);
    }
//
//    static <A> Builder<A> builder(Class<A> wrapperType, MethodHandle wrapperFactory) {
//        // Maybe only have Factory...
//        return new PackedApplicationDriver.Builder<>(null);
//    }

    static <A> Builder<A> builder(MethodHandles.Lookup caller, Class<A> wrapperType) {
        return builder(BeanExtensionPoint.factoryOf(wrapperType));
    }

    static <A> Builder<A> builder(Op<A> wrapperFactory) {
        return new PackedApplicationDriver.Builder<>(wrapperFactory);
    }

    /**
     * A builder for an application driver. An instance of this interface is acquired by calling
     * {@link ApplicationDriver#builder()}.
     */
    /* sealed */ interface Builder<A> /* permits PackedApplicationDriver.Builder */ {

        // Maaske konfigure man dem direkte paa extension support klassen
        //// Det jeg taener er at man maaske har mulighed for at konfigure dem. F.eks.
        // ServiceApplicationController.alwaysWrap();

        // Problemet her er at vi gerne maaske fx vil angive LaunchState for Lifetime.
        // Hvilket ikke er muligt

        // noget optional??? ellers
        /**
         * @param companions
         * @return this builder
         * @throws UnsupportedOperationException
         *             if this builder does not have a wrapper
         */
        default Builder addCompanion(ContainerWrapperCompanion... companions) {
            return this;
        }

        <S> ApplicationDriver<S> build(Class<S> wrapperType, MethodHandle wrapperFactory, Wirelet... wirelets);

        /**
         * Creates a new artifact driver.
         * <p>
         * The specified implementation can have the following types injected.
         * 
         * If the specified implementation implements {@link AutoCloseable} a {@link ManagedLifetimeController} can also be
         * injected.
         * <p>
         * Fields and methods are not processed.
         * 
         * @param <A>
         *            the type of artifacts the driver creates
         * @param caller
         *            a lookup object that must have full access to the specified implementation
         * @param wrapperType
         *            the implementation of the artifact
         * @return a new driver
         */
        <S> ApplicationDriver<S> build(MethodHandles.Lookup caller, Class<? extends S> wrapperType, Wirelet... wirelets);

        default ApplicationDriver<A> build(Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        ApplicationDriver<Void> buildVoid(Wirelet... wirelets);

        /**
         * Disables 1 or more extensions. Attempting to use a disabled extension will result in an RestrictedExtensionException
         * being thrown
         * 
         * @param extensionTypes
         *            the types of extension to disable
         * @return
         */
        Builder<A> disableExtension(Class<? extends Extension<?>> extensionType);

        /**
         * Application produced by the driver are executable. And will be launched by the specified launch mode by default.
         * <p>
         * The default launchState can be overridden at later point by using XYZ
         * 
         * @return this builder
         */
        Builder<A> managedLifetime();

        @SuppressWarnings("unchecked")
        default Builder<A> requireExtension(Class<? extends Extension>... extensionTypes) {

            return this;
        }

        default Builder<A> restartable() {
            return this;
        }

        // Det er jo ogsaa en companion
        default Builder<A> resultType(Class<?> resultType) {
            throw new UnsupportedOperationException();
        }

        default Builder<A> specializeMirror(Supplier<? extends ApplicationMirror> supplier) {
            throw new UnsupportedOperationException();
        }

        // Maaske kan man have et form for accept filter...

        // Vi skal soerge for vi ikke klasse initialisere... Det er det

        // Bliver de arvet??? Vil mene ja...
        // Naa men vi laver bare en host/app der saa kan goere det...

        // Kan ogsaa lave noget BiPredicate der tager
        // <Requesting extension, extension that was requested>

        // Spies

        // Kan jo altsaa ogsaa vaere en Wirelet...
        // WireletScope...

        /**
         * Indicates that the any application create by this driver is not runnable.
         * 
         * @return this builder
         */
        // https://en.wikipedia.org/wiki/Runtime_system
        // noRuntimeEnvironment appénwerwer wer

        // Add ApplicationRuntimeExtension to list of unsupported extensions
        // noApplicationRuntime
//        Builder disableApplicationRuntime(); // or notRunnable() (it was this originally)

        // Application can only take an assembly of this type...

        // fx disallow(BytecodeGenExtension.class);
        // fx disallow(ThreadExtension.class);
        // fx disallow(FileExtension.class);
        // fx disallow(NetExtension.class); -> you want to use network.. to bad for you...

//        default Builder linkExtensionBean(Class<? extends Extension> extensionType, Class<?> extensionBean) {
//            
//            // Taenker lidt den bliver erstattet af ApplicationController?
//            
//            // extension must be available...
//            // An extensionBean of the specified type must be installed by the extension in the root container
//            return this;
//        }

    }

    // Ville jo vaere fedt at genbruge den for ikke application-drivere.
    // Apps on Apps.
    public final class Composer extends AbstractComposer {
        /// Hmm interessant
        // fungere dog ikke super godt mht til den generiske parameter
        static ApplicationDriver<Void> of(ComposerAction<? super Composer> action, Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        static <A> ApplicationDriver<A> of(Class<A> application, ComposerAction<? super Composer> action, Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }
    }
}

// Foer var den som wirelet.
// Men Problemet med en wirelet og ikke en metode er at vi ikke beregne ApplicationBuildKind foerend
// vi har processeret alle wirelets

// Alternativ paa Driveren -> Fungere daarlig naar vi har child apps

// eller selvstaendig metode -> Er nok den bedste for nu

// og saa ServiceLocator.newReusableImage
///**
//* @param launchMode
//* @return
//* @throws UnsupportedOperationException
//*             if the driver was not built as executable.
//* @see Builder#executable(RunState)
//*/
//ApplicationDriver<A> withLaunchMode(RunState launchMode);