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
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.TypeToken;
import app.packed.component.Assembly;
import app.packed.component.Component;
import app.packed.component.ComponentAttributes;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifier;
import app.packed.component.Composer;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.exceptionhandling.BuildException;
import app.packed.exceptionhandling.PanicException;
import app.packed.inject.ServiceComposer;
import app.packed.inject.ServiceLocator;
import app.packed.state.InitializationException;
import app.packed.state.RunState;
import app.packed.validate.Validation;
import packed.internal.application.PackedApplicationDriver;

/**
 * Application drivers are responsible for analyzing and controlling the various lifecycle phases an application goes
 * through.
 * <p>
 * Packed comes with a number of predefined application drivers If these are not sufficient, your best bet is to look at
 * the source code of them to create your own.
 * 
 * <p>
 * This class can be used to create custom artifact types if the built-in artifact types such as {@link Program} and
 * {@link ServiceLocator} are not sufficient. In fact, the default implementations of both {@link Program} and
 * {@link ServiceLocator} uses an artifact driver themselves.
 * <p>
 * Normally, you would never create more than a single instance of an application driver.
 * 
 * @param <A>
 *            the type of application interface this driver creates.
 * 
 * @see Program#driver()
 * @see App#driver()
 * @see ServiceLocator#driver()
 */
// Environment + Shell + Result
public /* sealed */ interface ApplicationDriver<A> {

    // Structure record(Application, Component, Strea
    // Det ville vaere rigtig rart tror hvis BuildException have en liste af
    // validation violations...
    // Tit vil man gerne have alle fejlene eller en Component...
    // Either<Component, Validation>
    // Validataion
    Component analyze(Assembly<?> assembly, Wirelet... wirelets);

    // Maybe return Application??? Instead of Component
    // Maybe return something else? Alle componenter i appen'en eller component i namespaces
    // Det er jo snare et Build man returnere...
    default Application analyze2(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

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

    /**
     * Used by composers such as {@link ServiceComposer}.
     * <p>
     * This method is is rarely called directly by end-users. But indirectly through methods such as
     * {@link ServiceLocator#of(Consumer)}.
     * 
     * @param <C>
     *            the type of composer that is exposed to the user
     * @param composer
     *            the composer
     * @param consumer
     *            the consumer specified by the end user
     * @param wirelets
     *            optional wirelets
     * @return the application instance
     * 
     * @see Composer
     * @see ServiceComposer
     * @see ServiceLocator#of(Consumer)
     */
    <C extends Composer<?>> A compose(C composer, Consumer<? super C> consumer, Wirelet... wirelets);

    /**
     * Launches a new application using the specified assembly and optional wirelets.
     * <p>
     * This method is typical not called directly by end-users. But indirectly through methods such as
     * {@link App#run(Assembly, Wirelet...)} and {@link Program#start(Assembly, Wirelet...)}.
     * 
     * @param assembly
     *            the system assembly
     * @param wirelets
     *            optional wirelets
     * @return the new artifact or null if void artifact
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
    A launch(Assembly<?> assembly, Wirelet... wirelets);

    /**
     * Returns the launch mode of applications's created by this driver.
     * <p>
     * The launch mode can be overridden by using {@link ApplicationWirelets#launchMode(RunState)}.
     * 
     * @return the default launch mode of application's created by this driver
     * @see #launch(Assembly, Wirelet...)
     * @see #compose(ComponentDriver, Function, Consumer, Wirelet...)
     * @see #newImage(Assembly, Wirelet...)
     */
    RunState launchMode();
    // analyze
    // validate
    // check
    ///// Kunne vaere interessant fx
    // ComponentAnalysis = Either<Component, Validatable>
    // ComponentAnalysis extends Validatable

    // Ideen er at man kan smide checked exceptions...
    // Alternativt er man returnere en Completion<R>. hvor man saa kan f.eks. kalde orThrows()..

    default <T extends Throwable> A launchThrowing(Assembly<?> assembly, Class<T> throwing, Wirelet... wirelets) throws T {
        throw new UnsupportedOperationException();
    }

    default A launchThrowing(Assembly<?> assembly, Wirelet... wirelets) throws Throwable {
        // Tror den bliver brugt via noget ErrorHandler...
        // Hvor man specificere hvordan exceptions bliver handled

        // Skal vel ogsaa tilfoejes paa Image saa.. og paa Host#start()... lots of places

        // Her er ihvertfald noget der skal kunne konfigureres
        throw new UnsupportedOperationException();
    }

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
     * @see ServiceLocator#buildImage(Assembly, Wirelet...)
     */
    // Was buildImage
    ApplicationImage<A> newImage(Assembly<?> assembly, Wirelet... wirelets);

    // driver.use(A, W1, W2) == driver.with(W1).use(A, W2)
    // Hmmm, saa er den jo lige pludselig foerend..
    // ComponentDriveren
    // Maaske det giver mening alligevel...
    // Det er ihvertfald lettere at forklare...

    default void print(Assembly<?> assembly, Wirelet... wirelets) {
        Component c = analyze(assembly, wirelets);
        c.stream().forEach(cc -> {
            StringBuilder sb = new StringBuilder();
            sb.append(cc.path()).append(" ").append(cc.modifiers());
            if (cc.modifiers().contains(ComponentModifier.SOURCED)) {
                Class<?> cl = cc.attribute(ComponentAttributes.SOURCE_CLASS);
                sb.append(" [").append(cl.getName()).append("]");
            }
            System.out.println(sb.toString());
        });
    }

    default void printContracts(Assembly<?> assembly, Wirelet... wirelets) {

    }

    default TypeToken<? super A> typeToken() {
        // What if Job<?>
        throw new UnsupportedOperationException();
    }

    Validation validate(Assembly<?> assembly, Wirelet... wirelets);

    ApplicationDriver<A> with(Wirelet wirelet);

    ApplicationDriver<A> with(Wirelet... wirelets);

    /**
     * Returns a new {@code ApplicationDriver} builder.
     *
     * @return a application driver builder
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

        Builder launchMode(RunState launchMode);

        <A> ApplicationDriver<A> old(MethodHandle mhNewShell, Wirelet... wirelets);
        // Maybe just look for matching method/field hooks???
        // So always scan...

        // Throws ISE paa runtime? Validation? ASsertionError, Custom...
        @SuppressWarnings("unchecked")
        default Builder restrictExtensions(Class<? extends Extension>... extensionClasses) {
            throw new UnsupportedOperationException();
        }

        /**
         * Indicates that the applications the driver produces are stateless. Stateless applications will not have a runtime
         * 
         * @return this builder
         */
        Builder stateless();

        /**
         * Will look for annotations
         * <p>
         * If you want to support your own annotations. You can do by registering your hooks
         * 
         * @return this builder
         */
        Builder useShellAsSource();

//        // Stuff on the container always belongs to the other side...
//        // Cannot use Factory...
//       
//
//        // ApplicationDriver.of(.., Wirelet... wirelets)
//        // ApplicationDriver.ofRuntime(.., Wirelet... wirelets)
//
//
//        // kunne jo altsaa bare tage det som parametere...
//        Builder addWirelet(Wirelet... wirelets) {
//            return this;
//        }
//        // see laenger nede i ZApplicationDriverBuilders
//
//        <A> ApplicationDriver<A> build(Class<A> clazz) {
//            throw new UnsupportedOperationException();
//        }
//        
//        <A> ApplicationDriver<A> build(Factory<A> factory) {
//            // if use source fail...
//            throw new UnsupportedOperationException();
//        }
    }

}

/**
 * Kigge paa at tilfoeje builders til den...
 */
interface ZApplicationDriverWithBuilder {

    static Builder<Void> builder(MethodHandles.Lookup lookup) {
        throw new UnsupportedOperationException();
    }

    static <T> Builder<T> builder(MethodHandles.Lookup lookup, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    static <T> Builder<T> builder(MethodHandles.Lookup lookup, TypeToken<T> type) {
        // T kan have type variables
        throw new UnsupportedOperationException();
    }

    interface Builder<T> {
        // Hmm har vi brug for klassen foerend til allersidst???
        default <A> ApplicationDriver<A> build(Class<A> artifactType) {
            throw new UnsupportedOperationException();
        }
    }
}
