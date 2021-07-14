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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.TypeToken;
import app.packed.bean.BeanMirror;
import app.packed.component.Assembly;
import app.packed.component.BuildException;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.container.BaseContainerConfiguration;
import app.packed.container.ContainerDriver;
import app.packed.exceptionhandling.PanicException;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionDisabledException;
import app.packed.job.JobAssembly;
import app.packed.job.JobExtension;
import app.packed.lifecycle.InitializationException;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceLocator;
import app.packed.state.sandbox.InstanceState;
import app.packed.validate.Validation;
import packed.internal.application.PackedApplicationDriver;

/**
 * Application drivers are responsible for building and instantiating applications.
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
 * @see App#driver()
 * @see ServiceLocator#driver()
 */
// Environment + Shell + Result
public /* sealed */ interface ApplicationDriver<A> extends ContainerDriver<BaseContainerConfiguration> {

    /**
     * Returns whether or not applications produced by this driver have an {@link ApplicationRuntime}.
     * <p>
     * Applications that are not runnable will always be launched in the Initial state.
     * 
     * @return whether or not the applications produced by this driver are runnable
     */
    default boolean hasRuntime() {
        return !disabledExtensions().contains(ApplicationRuntimeExtension.class);
    }

    /**
     * Builds an application using the specified assembly and optional wirelets and returns a new instance of it.
     * <p>
     * This method is typical not called directly by end-users. But indirectly through methods such as
     * {@link App#run(Assembly, Wirelet...)} and {@link Program#start(Assembly, Wirelet...)}.
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
     * @see Program#start(Assembly, Wirelet...)
     * @see App#run(Assembly, Wirelet...)
     * @see ServiceLocator#of(Assembly, Wirelet...)
     */
    // Maaske er den her paa ApplicationRuntimeExtension.launch()
    // JobExtension.execute()
    A launch(Assembly<?> assembly, Wirelet... wirelets); // newInstance

    /**
     * Returns the launch mode of applications's created by this driver.
     * <p>
     * The launch mode can be overridden by using {@link ApplicationRuntimeWirelets#launchMode(InstanceState)}.
     * <p>
     * Drivers for applications without a runtime will always return {@link InstanceState#INITIALIZED}.
     * 
     * @return the default launch mode of application's created by this driver
     * @see #launch(Assembly, Wirelet...)
     * @see #compose(ComponentDriver, Function, Consumer, Wirelet...)
     * @see #newImage(Assembly, Wirelet...)
     * @see ApplicationRuntimeWirelets#launchMode(InstanceState)
     */
    // runTo().. Den der fucking terminated kills me (naah)
    // Terminated -> Runs until the application has terminated
    InstanceState launchMode();

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
     * @see Program#newImage(Assembly, Wirelet...)
     * @see ServiceLocator#newImage(Assembly, Wirelet...)
     */
    ApplicationImage<A> newImage(Assembly<?> assembly, Wirelet... wirelets);

    default void print(Assembly<?> assembly, Wirelet... wirelets) {
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

    /** {@return the type (typically an interface) of the application instances created by this driver.} */
    Class<?> type();

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
    @Override
    ApplicationDriver<A> with(Wirelet... wirelets);

    /**
     * @param launchMode
     * @return
     * @throws UnsupportedOperationException
     *             if the driver produces non-runnable applications.
     */
    default ApplicationDriver<A> withLaunchMode(InstanceState launchMode) {
        return with(ApplicationRuntimeWirelets.launchMode(launchMode));
    }

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
    interface Builder {

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

        <A> ApplicationDriver<A> build(MethodHandles.Lookup caller, Class<A> artifactType, MethodHandle mh, Wirelet... wirelets);

        // cannot be instantiated, typically used if you just want to analyze
        /**
         * Creates a new application driver that does not support instantiation of applications. These type of drivers are
         * typically used if you only need to use {@link ApplicationDriver#build(Assembly, Wirelet...)} but do not need to
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

        <A> ApplicationDriver<A> buildOld(MethodHandle mhNewShell, Wirelet... wirelets);

        /**
         * Disables 1 or more extensions. Attempting to use a disabled extension will result in an
         * {@link ExtensionDisabledException} being thrown
         * 
         * @param extensionTypes
         *            the types of extension to disable
         * @return
         */
        Builder disableExtension(Class<? extends Extension> extensionType);

        default Builder enableExtension(Class<? extends Extension> extensionType) {
            return this;
        }

        // prerequisite
        default <E extends Extension> Builder enableExtension(Class<? extends E> extensionType, Consumer<E> onInit) {
            return this;
        }

        default <E extends Extension> Builder enableExtension(Class<? extends E> extensionType, BiConsumer<ApplicationBuildInfo, E> onInit) {
            return this;
        }

        default <E extends Extension> Builder requireAssembly(Class<? extends Assembly<?>> assembly) {
            return this;
        }
        // fx disallow(BytecodeGenExtension.class);
        // fx disallow(ThreadExtension.class);
        // fx disallow(FileExtension.class);
        // fx disallow(NetExtension.class); -> you want to use network.. to bad for you...

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

        /**
         * @param launchMode
         * @return
         */
        // Kan jo vaere en wirelet...

        // static ApplicationRuntime.launchMode(ApplicationDriver ad);
        // -> SelectWirelets(LaunchWirelet.class).last().launchMode();
        // static Job.resultType()...
        Builder launchMode(InstanceState launchMode);

        /**
         * 
         * @param applicationType
         * @return
         * 
         * @see ApplicationDriver#type()
         */
        default Builder type(Class<?> applicationType) {
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
    default void assertValid(Assembly<?> assembly, Wirelet... wirelets) {
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

    // Tog den faktisk ud igen
    default ApplicationMirror mirror(Assembly<?> assembly, Wirelet... wirelets) {
        return ApplicationMirror.of(/*this*/ null, assembly, wirelets);
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

    default TypeToken<? super A> instanceTypeToken() {
        // What if Job<?>
        throw new UnsupportedOperationException();
    }

    default <T extends Throwable> A launchThrowing(Assembly<?> assembly, Class<T> throwing, Wirelet... wirelets) throws T {
        throw new UnsupportedOperationException();
    }

    default ApplicationDriverSandbox<A> extractResultTypeFromTypeparameter(int index) {
        // Ideen er at vi kan sige Job<T> -> Tag T ud af Job og brug den til at determined

        return this;
    }

    default A launchThrowing(Assembly<?> assembly, Wirelet... wirelets) throws Throwable {
        // Tror den bliver brugt via noget ErrorHandler...
        // Hvor man specificere hvordan exceptions bliver handled

        // Skal vel ogsaa tilfoejes paa Image saa.. og paa Host#start()... lots of places

        // Her er ihvertfald noget der skal kunne konfigureres
        throw new UnsupportedOperationException();
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

    default void printContracts(Assembly<?> assembly, Wirelet... wirelets) {

    }

    default void fff() {
        ApplicationDriver.builder().enableExtension(ApplicationRuntimeExtension.class);
        ApplicationDriver.builder().enableExtension(ServiceExtension.class, e -> e.exportAll());

        ApplicationDriver.builder().enableExtension(JobExtension.class, (b, e) -> {
            @SuppressWarnings("unchecked")
            Class<? extends JobAssembly<?>> cl = (Class<? extends JobAssembly<?>>) b.assemblyType();
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

    Validation validate(Assembly<?> assembly, Wirelet... wirelets);

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
