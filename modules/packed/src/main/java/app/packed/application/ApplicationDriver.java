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

import app.packed.container.Assembly;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.inject.service.ServiceLocator;
import app.packed.lifecycle.LifecycleApplicationController;
import app.packed.lifecycle.LifecycleException;
import app.packed.lifecycle.RunState;
import app.packed.lifetime.LifetimeBridge;
import internal.app.packed.application.PackedApplicationDriver;

/**
 * Application drivers are responsible for building applications.
 * <p>
 * Packed comes with a number of predefined application drivers:
 * 
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
     * @throws BuildException
     *             if the image could not be build
     */
    ApplicationImage<A> imageOf(Assembly  assembly, Wirelet... wirelets);

    /**
     * Returns whether or not applications produced by this driver have an {@link LifecycleApplicationController}.
     * <p>
     * Applications that are not runnable will always be launched in the Initial state.
     * 
     * @return whether or not the applications produced by this driver are runnable
     */
    boolean isExecutable();

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
     * @throws BuildException
     *             if the application could not be build
     * @throws LifecycleException
     *             if the application failed to initializing
     * @throws RuntimeException
     *             if the application had an executing phase and it fails
     * @see App#run(Assembly, Wirelet...)
     */
    A launch(Assembly  assembly, Wirelet... wirelets); // newInstance

    /**
     * Returns the launch mode of applications created by this driver.
     * <p>
     * The launch mode can be overridden by using {@link ExecutionWirelets#launchMode(RunState)}.
     * <p>
     * Drivers for applications without a runtime will always return {@link RunState#INITIALIZED}.
     * 
     * @return the default launch mode of application's created by this driver
     * @see #launch(Assembly, Wirelet...)
     * @see #imageOf(Assembly, Wirelet...)
     */
    RunState launchMode();

    // Foer var den som wirelet.
    // Men Problemet med en wirelet og ikke en metode er at vi ikke beregne ApplicationBuildKind foerend
    // vi har processeret alle wirelets

    // Alternativ paa Driveren -> Fungere daarlig naar vi har child apps

    // eller selvstaendig metode -> Er nok den bedste for nu

    // og saa ServiceLocator.newReusableImage

    /**
     * Creates a new application mirror from the specified assembly and optional wirelets.
     * <p>
     * The {@link ApplicationInfo application descriptor} will returns XXX at build time.
     * 
     * @param assembly
     *            the assembly to create an application mirror from
     * @param wirelets
     *            optional wirelets
     * @return an application mirror
     */
    ApplicationMirror mirrorOf(Assembly  assembly, Wirelet... wirelets);


    // Andre image optimizations
    //// Don't cache beans info
    /// Nu bliver jeg i tvivl igen... Fx med Tester 
    ApplicationImage<A> reusableImageOf(Assembly  assembly, Wirelet... wirelets);

    /** {@return the type (typically an interface) of the application instances created by this driver.} */
    // This is not the resultType
    Class<?> type();

    default void verify(Assembly  assembly, Wirelet... wirelets) {
        // Attempts to build a mirror, throws Verification

        // Altsaa vi skal have verify med taenker jeg..
    }

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
    ApplicationDriver<A> with(Wirelet... wirelets);

    /**
     * @param launchMode
     * @return
     * @throws UnsupportedOperationException
     *             if the driver was not built as executable.
     * @see Builder#executable(RunState)
     */
    ApplicationDriver<A> withLaunchMode(RunState launchMode);

    /**
     * Returns a new {@code ApplicationDriver} builder.
     *
     * @return the new builder
     */
    public static Builder builder() {
        return new PackedApplicationDriver.Builder();
    }

    /**
     * A builder for an application driver. An instance of this interface is normally acquired via
     * {@link ApplicationDriver#builder()}.
     */
    /*sealed*/ interface Builder /*permits PackedApplicationDriver.Builder*/ {

        // Maaske konfigure man dem direkte paa extension support klassen
        //// Det jeg taener er at man maaske har mulighed for at konfigure dem. F.eks. ServiceApplicationController.alwaysWrap();
       
        // Problemet her er at vi gerne maaske fx vil angive LaunchState for Lifetime.
        // Hvilket ikke er muligt
        
        
        @SuppressWarnings("unchecked")
        default Builder requireExtension(Class<? extends Extension>... extensionTypes) {
            
            return this;
        }
        
        // noget optional??? ellers
        default Builder addController(LifetimeBridge<?>... controllers) {
            return this;
        }
        
        default Builder linkExtensionBean(Class<? extends Extension> extensionType, Class<?> extensionBean) {
            
            // Taenker lidt den bliver erstattet af ApplicationController?
            
            // extension must be available...
            // An extensionBean of the specified type must be installed by the extension in the root container
            return this;
        }
        
        /**
         * Creates a new artifact driver.
         * <p>
         * The specified implementation can have the following types injected.
         * 
         * If the specified implementation implements {@link AutoCloseable} a {@link LifecycleApplicationController} can also be injected.
         * <p>
         * Fields and methods are not processed.
         * 
         * @param <S>
         *            the type of artifacts the driver creates
         * @param caller
         *            a lookup object that must have full access to the specified implementation
         * @param implementation
         *            the implementation of the artifact
         * @return a new driver
         */
        <S> ApplicationDriver<S> build(MethodHandles.Lookup caller, Class<? extends S> implementation, Wirelet... wirelets);

        // Hvorfor har vi en caller her???
        <A> ApplicationDriver<A> build(MethodHandles.Lookup caller, Class<A> artifactType, MethodHandle mh, Wirelet... wirelets);

        ApplicationDriver<Void> buildVoid(Wirelet... wirelets);
        
        /**
         * Disables 1 or more extensions. Attempting to use a disabled extension will result in an
         * RestrictedExtensionException being thrown
         * 
         * @param extensionTypes
         *            the types of extension to disable
         * @return
         */
        Builder disableExtension(Class<? extends Extension<?>> extensionType);

        /**
         * Application produced by the driver are executable. And will be launched by the specified launch mode by default.
         * <p>
         * The default launchState can be overridden at later point by using XYZ
         * 
         * @param launchState
         *            the launch mode of the application
         * @return this builder
         * @see ApplicationDriver#withLaunchMode(RunState)
         */
        Builder executable(RunState launchState);

        default Builder restartable() {
            return this;
        }


        default Builder resultType(Class<?> resultType) {
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
        // noRuntimeEnvironment appénwerwer  wer

        // Add ApplicationRuntimeExtension to list of unsupported extensions
        // noApplicationRuntime
//        Builder disableApplicationRuntime(); // or notRunnable() (it was this originally)

        // Application can only take an assembly of this type...

        // fx disallow(BytecodeGenExtension.class);
        // fx disallow(ThreadExtension.class);
        // fx disallow(FileExtension.class);
        // fx disallow(NetExtension.class); -> you want to use network.. to bad for you...

        // Det er faktisk en okay maade at lave det her paa
        // Vi kan maaske endda have en. AsBean()?
        /**
         * Will add a service locator for injection of all exported services from the root container.
         * 
         * @return this builder
         */
        //// Altsaa vi kan vel godt lave et eller andet saa vi kan "exportere" ting fra extensions
        // publically
        // ServiceExtensionSupport.registerWithApplicationDriver(ApplicationDriver.Builder builder);
        default Builder serviceLocator() {
            //// Vi skal have en generics version alle extensions kan tilbyde "services" via
            //// Vi bliver noedt til at konvertere en extension bean paa en eller anden maade
            //// Da vi fx kan lave flere ServiceLocators fra et single image

            // ExtensionBean -> X + Default X if not available

            throw new UnsupportedOperationException();
        }
    }
}
interface OldDriver<A> {
    
    // maa kalde internal_app_instance.jobCompleter.awaitResult or whatever
    default A launchJob(Assembly  assembly, Wirelet... wirelets) {
        // Er ikke sikker paa vi kan bruge den hed med signaturen <A>

        // JobDriver
        throw new UnsupportedOperationException();
    }
    // Maaske er den her paa ApplicationRuntimeExtension.launch()
    // JobExtension.execute()
}
