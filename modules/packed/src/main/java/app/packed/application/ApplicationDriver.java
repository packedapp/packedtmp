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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.application.programs.Program;
import app.packed.application.programs.SomeApp;
import app.packed.base.TypeToken;
import app.packed.bean.BeanMirror;
import app.packed.build.BuildException;
import app.packed.bundle.Bundle;
import app.packed.bundle.Wirelet;
import app.packed.exceptionhandling.PanicException;
import app.packed.extension.Extension;
import app.packed.extension.UnavailableExtensionException;
import app.packed.inject.service.ServiceExtension;
import app.packed.inject.service.ServiceLocator;
import app.packed.job.JobAssembly;
import app.packed.job.JobExtension;
import app.packed.lifecycle.InitializationException;
import app.packed.state.sandbox.InstanceState;
import app.packed.validate.Validation;
import packed.internal.application.PackedApplicationDriver;

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
 * This class can be used to create custom artifact types if the built-in artifact types such as {@link Program} and
 * {@link ServiceLocator} are not sufficient. In fact, the default implementations of both {@link Program} and
 * {@link ServiceLocator} uses an artifact driver themselves.
 * <p>
 * Normally, you never create more than a single instance of an application driver.
 * 
 * @param <A>
 *            the type of application interface this driver creates.
 * @see Program#driver()
 * @see SomeApp#driver()
 * @see ServiceLocator#driver()
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
    Set<Class<? extends Extension>> bannedExtensions();

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
     * @see Program#imageOf(Bundle, Wirelet...)
     * @see ServiceLocator#imageOf(Bundle, Wirelet...)
     */
    ApplicationImage<A> imageOf(Bundle<?> assembly, Wirelet... wirelets);

    /**
     * Returns whether or not applications produced by this driver have an {@link ApplicationRuntime}.
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
     * {@link SomeApp#run(Bundle, Wirelet...)} and {@link Program#start(Bundle, Wirelet...)}.
     * 
     * @param assembly
     *            the main assembly of the application
     * @param wirelets
     *            optional wirelets
     * @return the launched application instance
     * @throws BuildException
     *             if the application could not be build
     * @throws InitializationException
     *             if the application failed to initializing
     * @throws PanicException
     *             if the application had an executing phase and it fails
     * @see Program#start(Bundle, Wirelet...)
     * @see SomeApp#run(Bundle, Wirelet...)
     * @see ServiceLocator#of(Bundle, Wirelet...)
     */
    A launch(Bundle<?> assembly, Wirelet... wirelets); // newInstance

    default A launchJob(Bundle<?> assembly, Wirelet... wirelets) {
        // Er ikke sikker paa vi kan bruge den hed med signaturen <A>

        // JobDriver
        throw new UnsupportedOperationException();
    }
    // Maaske er den her paa ApplicationRuntimeExtension.launch()
    // JobExtension.execute()

    /**
     * Returns the launch mode of applications's created by this driver.
     * <p>
     * The launch mode can be overridden by using {@link ExecutionWirelets#launchMode(InstanceState)}.
     * <p>
     * Drivers for applications without a runtime will always return {@link InstanceState#INITIALIZED}.
     * 
     * @return the default launch mode of application's created by this driver
     * @see #launch(Bundle, Wirelet...)
     * @see #imageOf(Bundle, Wirelet...)
     * @see ExecutionWirelets#launchMode(InstanceState)
     */
    ApplicationLaunchMode launchMode();

    // Foer var den som wirelet.
    // Men Problemet med en wirelet og ikke en metode er at vi ikke beregne ApplicationBuildKind foerend
    // vi har processeret alle wirelets

    // Alternativ paa Driveren -> Fungere daarlig naar vi har child apps

    // eller selvstaendig metode -> Er nok den bedste for nu

    // og saa ServiceLocator.newReusableImage

    /**
     * Creates a new application mirror from the specified assembly and optional wirelets.
     * <p>
     * The {@link ApplicationDescriptor application descriptor} will returns XXX at build time.
     * 
     * @param assembly
     *            the assembly to create an application mirror from
     * @param wirelets
     *            optional wirelets
     * @return an application mirror
     */
    ApplicationMirror mirrorOf(Bundle<?> assembly, Wirelet... wirelets);

    default void print(Bundle<?> assembly, Wirelet... wirelets) {
        ApplicationMirror b = ApplicationMirror.of(assembly, wirelets);
        b.forEachComponent(cc -> {
            StringBuilder sb = new StringBuilder();
            sb.append(cc.path()).append("");
            if (cc instanceof BeanMirror bm) {
                sb.append(" [").append(bm.beanType().getName()).append("]");
            }
            System.out.println(sb.toString());
        });
    }

    // Andre image optimizations
    //// Don't cache beans info
    /// Nu bliver jeg i tvivl igen... Fx med Tester 
    ApplicationImage<A> reusableImageOf(Bundle<?> assembly, Wirelet... wirelets);

    /** {@return the type (typically an interface) of the application instances created by this driver.} */
    // This is not the resultType
    Class<?> type();

    default void verify(Bundle<?> assembly, Wirelet... wirelets) {
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
     * @see Builder#executable(ApplicationLaunchMode)
     */
    ApplicationDriver<A> withLaunchMode(ApplicationLaunchMode launchMode);

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
    sealed interface Builder permits PackedApplicationDriver.Builder {

        /**
         * Creates a new artifact driver.
         * <p>
         * The specified implementation can have the following types injected.
         * 
         * If the specified implementation implements {@link AutoCloseable} a {@link ApplicationRuntime} can also be injected.
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

        <A> ApplicationDriver<A> buildOld(MethodHandle mhNewShell, Wirelet... wirelets);

        /**
         * Disables 1 or more extensions. Attempting to use a disabled extension will result in an
         * {@link UnavailableExtensionException} being thrown
         * 
         * @param extensionTypes
         *            the types of extension to disable
         * @return
         */
        Builder disableExtension(Class<? extends Extension> extensionType);

        /**
         * Application produced by the driver are executable. And will be launched by the specified launch mode by default.
         * 
         * @param launchMode
         *            the launch mode of the application
         * @return this builder
         * @see ApplicationDriver#withLaunchMode(ApplicationLaunchMode)
         */
        Builder executable(ApplicationLaunchMode launchMode);

        default Builder restartable() {
            return this;
        }
        // cannot be instantiated, typically used if you just want to analyze

        /**
         * 
         * @param applicationType
         * @return
         * 
         * @see ApplicationDriver#type()
         */
        default Builder resultType(Class<?> applicationType) {
            throw new UnsupportedOperationException();
        }

        default Builder nameable() {
            // add a string with the name of the application
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
        // noRuntimeEnvironment

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

interface ApplicationDriverSandbox<A> {
    // driver.use(A, W1, W2) == driver.with(W1).use(A, W2)
    // Hmmm, saa er den jo lige pludselig foerend..
    // ComponentDriveren
    // Maaske det giver mening alligevel...
    // Det er ihvertfald lettere at forklare...
    /**
     * Builds and validates the application
     * 
     * @param assembly
     *            the assembly to validate
     * @param wirelets
     *            optional wirelets
     * @throws AssertionError
     *             if the application failed to build
     */
    // Validation vs Build exceptions
    // Smider vi
    // Altsaa maaske skal det starte i devtools
    // I don't think it tests missing contract clauses
    // assertValid(..., hasContract);
    // I navnet valid ligger ikke fullfilled. Laver jeg en hjaelpe assembly.
    // er den jo stadig valid... selvom vi ikke propper fake parametere ind..
    //
    default void assertValid(Bundle<?> assembly, Wirelet... wirelets) {
        // Checks that the container can be sucessfully build...
        // What about fullfilled??? Er den ok hvis vi f.eks.
        // mangler nogle service argumenter???

        // Usefull for test
        // ServiceAsserts.exposes(fff)

        // ServiceWirelet.assertContract(

        // Maaske hedder wirelets ikke noget med assert...
        // men validate (eller check)....
        // Og assert goer saa bare at vi smider den med en AssertException...
        // Men vi kan ogsaa vaelge at faa det i en liste....
        // App.assertValid(new FooAssembly()), ServiceWirelets.assertExactContract(adasdasd.));
        // App.assertValid(new FooAssembly()), ServiceWirelets.checkExactContract(adasdasd.));
        // App.assertValid(new FooAssembly()), ServiceWirelets.validateExactContract(adasdasd.));

        // App.assertValid(new FooAssembly(), ContractWirelets.checkFullfilled());
        // App.assertValid(new FooAssembly(), ContractWirelets.validateFullfilled());
        // A specific type of analyze that throws ValidationError...
        validate(assembly, wirelets).assertValid();
    }

    default <T> ApplicationDriver<T> bind(Class<T> cl) {
        // Ideen er lidt at man f.eks. fra Job<R> kan binde R...
        // og sig Job.of(String.class, ....);
        // og sig Job.buildImage(String.class, ....);
        // og sig Job.buildImage(TypeToken<r> tt, ....);
        // I sidste ende kan man maaske selv lave castet??
        // Og saa bare tilfoeje en wirelet? ala
        // return (ApplicationDriver<R>) driver.with(Wirelet.bindTypeVariable(...));
        // BindableApplicationDriver???
        throw new UnsupportedOperationException();
    }

    default ApplicationDriverSandbox<A> extractResultTypeFromTypeparameter(int index) {
        // Ideen er at vi kan sige Job<T> -> Tag T ud af Job og brug den til at determined

        return this;
    }

    default void fff() {
        // ApplicationDriver.builder().enableExtension(ApplicationRuntimeExtension.class);
        ApplicationDriverSandbox.builder().enableExtension(ServiceExtension.class, e -> e.exportAll());

        ApplicationDriverSandbox.builder().enableExtension(JobExtension.class, (b, e) -> {
            @SuppressWarnings("unchecked")
            Class<? extends JobAssembly<?>> cl = (Class<? extends JobAssembly<?>>) b.bundleType();
            System.out.println(cl);
            // e.setResultType(<T>.extract);
            // extract <T> from Jo
        });

        // ApplicationBuildInfo = Assembly + ApplicationDriver + "Launch Context (img/delayed/instance)"
    }
//  /**
//  * Will look for annotations
//  * <p>
//  * If you want to support your own annotations. You can do by registering your hooks
//  * 
//  * @return this builder
//  */
// // Jeg vil mene vi goer det her automatisk...
// // Hvad hvis vi returnere forskellige typer???
// Builder useShellAsSource();

    default TypeToken<? super A> instanceTypeToken() {
        // What if Job<?>
        throw new UnsupportedOperationException();
    }

    default <T extends Throwable> A launchThrowing(Bundle<?> assembly, Class<T> throwing, Wirelet... wirelets) throws T {
        throw new UnsupportedOperationException();
    }

    default A launchThrowing(Bundle<?> assembly, Wirelet... wirelets) throws Throwable {
        // Tror den bliver brugt via noget ErrorHandler...
        // Hvor man specificere hvordan exceptions bliver handled

        // Skal vel ogsaa tilfoejes paa Image saa.. og paa Host#start()... lots of places

        // Her er ihvertfald noget der skal kunne konfigureres
        throw new UnsupportedOperationException();
    }

    // Tog den faktisk ud igen
    default ApplicationMirror mirror(Bundle<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    default void printContracts(Bundle<?> assembly, Wirelet... wirelets) {

    }

    // Structure record(Application, Component, Strea
    // Det ville vaere rigtig rart tror hvis BuildException have en liste af
    // validation violations...
    // Tit vil man gerne have alle fejlene eller en Component...
    // Either<Component, Validation>
    // Validataion

    // analyze
    // validate
    // check
    ///// Kunne vaere interessant fx
    // ComponentAnalysis = Either<Component, Validatable>
    // ComponentAnalysis extends Validatable

    // Ideen er at man kan smide checked exceptions...
    // Alternativt er man returnere en Completion<R>. hvor man saa kan f.eks. kalde orThrows()..

    Validation validate(Bundle<?> assembly, Wirelet... wirelets);

    /**
     * Create a new application driver that.
     * 
     * <p>
     * Wirelets that have been specified at previous occusion will be processed before the specified wirelet
     * 
     * @param wirelet
     *            the wirelet to append
     * @return the new application driver
     */
    // Altsaa den her metoder bliver bare ikke brugt ofte nok, til at vi behoever have begge
    ApplicationDriver<A> with(Wirelet wirelet);

    static Builder2 builder() {
        throw new UnsupportedOperationException();
    }

    interface Builder2 {
        /**
         * Creates a new application driver that does not support instantiation of applications. These type of drivers are
         * typically used if you only need to use {@link ApplicationDriver#build(Bundle, Wirelet...)} but do not need to
         * create actual application instances.
         * 
         * @param <A>
         *            the application type
         * @param applicationType
         *            the type returned by {@link ApplicationDriver#type()}
         * @return the new driver
         */
        default <A> ApplicationDriver<A> buildInstanceless(Class<A> applicationType) {
            // Cannot be used for instantiating...
            // Typically used if void
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("exports")
        default <E extends Extension> Builder2 enableExtension(Class<? extends E> extensionType, BiConsumer<ApplicationDescriptor, E> onInit) {
            return this;
        }

        // prerequisite
        @SuppressWarnings("exports")
        default <E extends Extension> Builder2 enableExtension(Class<? extends E> extensionType, Consumer<E> onInit) {
            return this;
        }

        @SuppressWarnings("exports")
        default Builder2 enableExtension(Class<? extends Extension> extensionType) {
            return this;
        }

        @SuppressWarnings("exports")
        default Builder2 requireAssembly(Class<? extends Bundle<?>> assembly) {
            return this;
        }
    }

    // Will fail if non-runnable

}

// Old one
///**
// * Kigge paa at tilfoeje builders til den...
// */
//interface ZApplicationDriverWithBuilder {
//
//    // Vi dropper det lookup object?
//    // eller ogsaa har vi maaske 2 metoder
//    // Man kan lave en builder
//
//    static Builder<Void> builder(MethodHandles.Lookup lookup) {
//        throw new UnsupportedOperationException();
//    }
//
//    static <T> Builder<T> builder(MethodHandles.Lookup lookup, Class<T> type) {
//        throw new UnsupportedOperationException();
//    }
//
//    static <T> Builder<T> builder(MethodHandles.Lookup lookup, TypeToken<T> type) {
//        // T kan have type variables
//        throw new UnsupportedOperationException();
//    }
//

//default Builder named(String name) {
//    // Er vel kun composer der skal bruge den
//    // Ellers tager vi vel altid fra navnet paa assemblien
//    // Og ellers tager vi vel bare navnet paa composeren.
//    // Eller applications interfaced?
//    throw new UnsupportedOperationException();
//}

//    interface Builder<T> {
//
//
//
////      // Stuff on the container always belongs to the other side...
////      // Cannot use Factory...
////     
////
////      // ApplicationDriver.of(.., Wirelet... wirelets)
////      // ApplicationDriver.ofRuntime(.., Wirelet... wirelets)
////
////
////      // kunne jo altsaa bare tage det som parametere...
////      Builder addWirelet(Wirelet... wirelets) { 
////          return this;
////      }
////      // see laenger nede i ZApplicationDriverBuilders
////
////      <A> ApplicationDriver<A> build(Class<A> clazz) {
////          throw new UnsupportedOperationException();
////      }
////      
////      <A> ApplicationDriver<A> build(Factory<A> factory) {
////          // if use source fail...
////          throw new UnsupportedOperationException();
////      }
//
//    }
//}
