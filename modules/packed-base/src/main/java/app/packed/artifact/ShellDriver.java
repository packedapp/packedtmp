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
package app.packed.artifact;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.function.Function;

import app.packed.component.Bundle;
import app.packed.component.Component;
import app.packed.component.ComponentModifierSet;
import app.packed.component.CustomConfigurator;
import app.packed.component.WireableComponentDriver;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.guest.Guest;
import app.packed.service.Injector;
import app.packed.service.ServiceRegistry;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedAssemblyContext;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.PackedInitializationContext;
import packed.internal.component.PackedWireableComponentDriver;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.invoke.MethodHandleBuilder;
import packed.internal.invoke.OpenClass;
import packed.internal.util.ThrowableUtil;

/**
 * Shell drivers are responsible for creating new shells by instantiating stuff.
 * <p>
 * This class can be extended to create custom shell types if the built-in shell types such as {@link App} and
 * {@link Injector} are not sufficient. In fact, the default implementations of both {@link App} and {@link Injector}
 * are just thin facade that delegates all calls to an stuff instance.
 * <p>
 * Normally, you should never instantiate more then a single instance of driver for any shell implementation.
 * <p>
 * Iff a driver creates shells with an execution phase. The shell must implement {@link AutoCloseable}.
 * 
 * @param <S>
 *            The type of shell this driver creates.
 * @see App#driver()
 */

//Tror bare vi laver om til et interface.... 

// Tror ikke shells kan bruge annoteringer??? Altsaa maaske paa surragates???
// Ville maaske vaere fedt nok bare at kunne sige
// @OnShutdown()
// sysout "FooBar was removed"

// Support of injection of the shell into the Container...
// We do not generally support this, as people are free to any shell they may like.
// Which would break encapsulation
public final class ShellDriver<S> {

    /** The method handle responsible for creating new shell instances. */
    private final MethodHandle instantiatior;

    /** The initial set of modifiers for any system that uses this driver. */
    private final int modifiers;

    /**
     * Creates a new driver.
     * 
     * @param isGuest
     *            the type of shell that is created
     * @param instantiatior
     *            a method handle that create new shell instances
     */
    private ShellDriver(boolean isGuest, MethodHandle instantiatior) {
        this.modifiers = PackedComponentModifierSet.I_SHELL + (isGuest ? PackedComponentModifierSet.I_GUEST : 0);
        this.instantiatior = requireNonNull(instantiatior);
    }

    public <D> S configure(WireableComponentDriver<D> driver, CustomConfigurator<D> consumer, Wirelet... wirelets) {
        return configure(driver, e -> e, consumer, wirelets);
    }

    public <C, D> S configure(WireableComponentDriver<D> driver, Function<D, C> factory, CustomConfigurator<C> consumer, Wirelet... wirelets) {
        ComponentNodeConfiguration node = PackedAssemblyContext.configure(this, (PackedWireableComponentDriver<D>) driver, factory, consumer, wirelets);
        PackedInitializationContext ac = PackedInitializationContext.initialize(node);
        return newShell(ac);
    }

    /**
     * Creates and initializes a new shell (and system) using the specified bundle.
     * <p>
     * This method will invoke {@link #newShell(PackedInitializationContext)} to create the actual shell.
     * 
     * @param bundle
     *            the system bundle
     * @param wirelets
     *            any wirelets that should be used when assembling or initializing the system
     * @return the new shell
     * @throws RuntimeException
     *             if the shell could not be created
     */
    public S initialize(Bundle<?> bundle, Wirelet... wirelets) {
        // Assembles the system
        ComponentNodeConfiguration node = PackedAssemblyContext.assemble(modifiers, bundle, this, wirelets);

        // Initializes the system
        PackedInitializationContext pic = PackedInitializationContext.initialize(node);

        return newShell(pic); // Returns a shell to the user
    }

    /**
     * Returns whether or not the type of shell being created by this driver has an execution phase. This is determined by
     * whether or not the shell implements {@link AutoCloseable}.
     * 
     * @return whether or not the shell being produced by this driver has an execution phase
     */
    public ComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    /**
     * Creates a new image using the specified bundle and this driver.
     * 
     * @param bundle
     *            the bundle to use when assembling the image
     * @param wirelets
     *            optional wirelets
     * @return a new image
     */
    public Image<S> newImage(Bundle<?> bundle, Wirelet... wirelets) {
        // Assembles a system image
        ComponentNodeConfiguration node = PackedAssemblyContext.assemble(PackedComponentModifierSet.I_IMAGE + modifiers, bundle, this, wirelets);

        return new ShellImage(node); // Returns a new to the user
    }

    /**
     * Create a new shell using the supplied context.
     * 
     * @param pic
     *            the initialization context to wrap
     * @return the new shell
     */
    S newShell(PackedInitializationContext pic) {
        try {
            return (S) instantiatior.invoke(pic);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /**
     * Create, initialize and start a new shell using the specified source.
     * 
     * @param bundle
     *            the source of the top-level container
     * @param wirelets
     *            any wirelets that should be used to create the shell
     * @return the new shell
     * @throws UnsupportedOperationException
     *             if the driver does not produce an shell with an execution phase
     */
    public S start(Bundle<?> bundle, Wirelet... wirelets) {
        // Assembles the system
        ComponentNodeConfiguration node = PackedAssemblyContext.assemble(modifiers, bundle, this, wirelets);

        // Initializes the system
        PackedInitializationContext pic = PackedInitializationContext.initialize(node);

        // Starts the system (blocking)
        pic.guest().start();

        return newShell(pic); // Returns a shell to the user
    }

    public Class<?> type() {
        return instantiatior.type().returnType();
    }

    public static <A> ShellDriver<A> of(MethodHandles.Lookup caller, Class<A> shellType, Class<? extends A> implementation) {
        boolean isGuest = AutoCloseable.class.isAssignableFrom(implementation);
        Constructor<?> con = implementation.getDeclaredConstructors()[0];
        MethodHandleBuilder builder = MethodHandleBuilder.of(implementation, PackedInitializationContext.class);
        builder.addKey(Component.class, PackedInitializationContext.MH_COMPONENT, 0);
        builder.addKey(ServiceRegistry.class, PackedInitializationContext.MH_SERVICES, 0);
        if (isGuest) {
            builder.addKey(Guest.class, PackedInitializationContext.MH_GUEST, 0);
        }
        OpenClass oc = new OpenClass(caller, implementation, true);
        MethodHandle mh2 = builder.build(oc, con);
        return new ShellDriver<>(isGuest, mh2);
    }

    // A method handle that takes an ArtifactContext and produces something that is compatible with A
    public static <A> ShellDriver<A> of(MethodHandles.Lookup caller, Class<A> shellType, MethodHandle mh) {
        // TODO validate type
        // shellType must match MH
        boolean isGuest = AutoCloseable.class.isAssignableFrom(shellType);
        // TODO fix....

        return new ShellDriver<>(isGuest, mh);
    }

    /** An implementation of {@link Image} uses by {@link ShellDriver#newImage(Bundle, Wirelet...)}. */
    private final class ShellImage implements Image<S> {

        /** The assembled image node. */
        private final ComponentNodeConfiguration node;

        /**
         * Creates a new image from the specified configuration and wirelets.
         * 
         * @param node
         *            the artifact driver
         */
        private ShellImage(ComponentNodeConfiguration node) {
            this.node = node;
        }

        /** {@inheritDoc} */
        @Override
        public Component component() {
            return node.adaptToComponent();
        }

        /** {@inheritDoc} */
        @Override
        public S initialize(Wirelet... wirelets) {
            PackedInitializationContext pic = PackedInitializationContext.initializeImage(node, WireletPack.forImage(node, wirelets));
            return newShell(pic);
        }

        /** {@inheritDoc} */
        @Override
        public S start(Wirelet... wirelets) {
            return initialize(wirelets);
        }
    }

//  public static <A> ArtifactDriver<A> of(MethodHandles.Lookup caller, Class<A> shellType, Factory<? extends A> implementation) {
//      throw new UnsupportedOperationException();
//  }
//  <E extends A> ArtifactDriver<A> mapTo(Class<E> decoratingType, Function<A, E> decorator) {
//      // Ideen er egentlig at f.eks. kunne wrappe App, og tilfoeje en metode...
//      // Men altsaa, maaske er det bare at kalde metoderne direkte i context...
//      // PackedApp kalder jo bare direkte igennem
//      throw new UnsupportedOperationException();
//  }

//
//    static <A> A start(Class<A> shellType, ArtifactSource source, Wirelet... wirelets) {
//        // The only thing we save is defining a driver..
//        // But we need the driver for App#driver... so not much saved
//        throw new UnsupportedOperationException();
//    }
//  Supplier<A> startingProvider(ArtifactSource a, Wirelet... wirelets) {
//  // Kunne ogsaa lave den paa image...
//  // Men altsaa taenker vi godt vil have noget wirelets med...
//  // <A> Supplier<A> ArtifactImage.supplier(ArtifactDriver<A> driver);
//  throw new UnsupportedOperationException();
//}
//    /** Options that can be specified when creating a new driver or via {@link #withOptions(Option...)}. */

    // Ideen er lidt at vi koerer ArchUnit igennem here....
    // Altsaa Skal vi have en BaseEnvironment.. hvor vi kan specificere nogle options for alle
    // F.eks. black liste ting...

    // Invoked by each driver??
    // List<ArtifactDriver.Option> BaseEnvironment.defaultOptions(Class<?> shellDriver);
    // BaseEnvironment via service loader. Exactly one... Extensions should never create one.
    // Users
    // Men skal man kunne overskriver den forstaaet paa den maade at stramme den...
    // F.eks. med en order... Alle skal have unik orders (ellers fejl)
    // D.v.s. CompanyBaseEnvironment(order = 1) , DivisionBaseEnvironment(order = 2)
    // Ellers ogsaa installere man en masse options... //Allowed algor

    // IDK Den fungere ikke lige skide godt med et image...
    // Can jo ikke prefix'e med noget som helst hvis foerst imaged er lavet...
    // Eller f.eks. Whitelist/Blacklist kan vi godt. fordi vi har listen af dem...
    // naar vi instantiere...
    // Saa vi kan checke ting...
    // Men ikke paavirke hvordan de bliver lavet...

    static class ZOption {

        // If not autoclosable
        public ZOption forceGuest() {
            throw new UnsupportedOperationException();
        }

        public ZOption forceNonGuest() {
            throw new UnsupportedOperationException();
        }

        // Normally we just check if App.iface extends AutoClosable...
        // But might want to override this.
        // forceManaged, forceUnmanaged

        // String Reason??? This extension has been blacklisted
        @SafeVarargs
        static ZOption blacklistExtensions(Class<? extends Extension>... extensions) {
            throw new UnsupportedOperationException();
        }

        static ZOption blacklistExtensions(String... extensions) {
            throw new UnsupportedOperationException();
        }

        // Mapning af Execeptions/Errors....
        //// Saa er det let at rette i f.eks. App

        // Debug Options...

        // whitelistExtension(...)
        // blacklistExtension

        //// Can only use of them
        // Altsaa det maa vaere meget taet paa ArchUnit a.la.. Hmmm

        static ZOption nameProvider() {
            // prefix???
            // Ideen er ihvertfald at
            throw new UnsupportedOperationException();
        }

        static ZOption postfixWirelets(Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        // Hmmm... Fungere jo ikke rigtigt med image....
        static ZOption prefixWirelets(Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        // custom ServiceProvider..
        static ZOption serviceProvider() {
            throw new UnsupportedOperationException();
        }

        // Hmmm... Fungere jo ikke rigtigt med image......
        @SafeVarargs
        static ZOption whitelistExtensions(Class<? extends Extension>... extensions) {
            throw new UnsupportedOperationException();
        }

        // non execution -> Create...

        // Ideen var man skulle kunne angive nogle prefix wirelets naar man lavede en shell...
        // Men det er nu lavet om til options pre and post wirelets
        // executing -> Create (and initialize), start, startAsync, Execute, executeAsync
        // StartExecutor ?
        // final T create(Assembly source, Wirelet... shellWirelets) {
        //
//         // Ideen er lidt at Artifact Implementering, kan kalde med dens egen wirelets...
//         // ala
//         // Maaske er det en option.. .. ellers andThen... eller have en Wirelet customWirelets(Assemble)..
        //
//         // start(Assembly, Wirelet... wirelets) {
//         // create(Assembly, wirelets, ArtifactWirelets.startSynchronous());
//         // create(Assembly, wirelets, ArtifactWirelets.startAsynchronous());
//         // }
//         // What about execute....
//         throw new UnsupportedOperationException();
        // }
    }

    // Ideen er lidt at vi har en OptionList som aggregere alle options
    // Det er en public klasse i packedapp.internal.shell.OptionAggregate
    // Den har en PackedContainerConfiguration saa adgang til. og f.eks. kalder
    // aggregate.addExtension(Class<? extends Extension>) <- may throw....
//    static class ArtifactOptionAggregate {
//        ArtifactOptionAggregate(Option[] options) {}
//
//        ArtifactOptionAggregate with(Option[] options) {
//            throw new UnsupportedOperationException();
//        }
//    }
}
////// FRA SHELL CONTEXT
//start() osv smider UnsupportedOperationException hvis LifeycleExtension ikke er installeret???
//Naeh syntes bare man returnere oejeblikligt

//Noget med lifecycle
//Noget med Injector // Hvis vi har sidecars.... Er det maaske bare der...
//Container tillader sidecar... App goer ikke. Saa kan man hvad man vil...

//Entry point koere bare automatisk efter start....
//Men ville vaere rart at vide

//Distenction mellem business service or infrastructure service....
//Hmm, problemet er vel at vi gerne vil injecte begge....

//Noget med entrypoint?? Nej tror ikke vi har behov for at expose dett..

//TypeLiteral??? Maaske returnere execute() et object...

//Optional<?>??? Maybe a ResultClass
//default Object result() {
// // awaitResult()...
// // awaitResultUninterruptable()...
// // Ideen er lidt at vi kan vente paa det...
// return null;
//}
//
////En Attribute????
//default Class<?> resultType() {
// // Ideen er her taenkt at vi kan bruge den samme med Job...
// //// En anden slags entry point annotering...
// return void.class;
//}
