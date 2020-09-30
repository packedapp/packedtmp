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
package packed.internal.component;

import static java.util.Objects.requireNonNull;
import static packed.internal.component.PackedComponentModifierSet.I_IMAGE;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Function;

import app.packed.component.Bundle;
import app.packed.component.Component;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifierSet;
import app.packed.component.CustomConfigurator;
import app.packed.component.Image;
import app.packed.component.ShellDriver;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link ShellDriver}. */
public final class PackedShellDriver<S> implements ShellDriver<S> {

    /** The initial set of modifiers for any system that uses this driver. */
    private final int modifiers;

    /** The method handle responsible for creating new shell instances. */
    private final MethodHandle newShellMH;

    /**
     * Creates a new driver.
     * 
     * @param isGuest
     *            the type of shell that is created
     * @param newShellMH
     *            a method handle that can create new shell instances
     */
    public PackedShellDriver(boolean isGuest, MethodHandle newShellMH) {
        this.modifiers = PackedComponentModifierSet.I_SHELL + (isGuest ? PackedComponentModifierSet.I_GUEST : 0);
        this.newShellMH = requireNonNull(newShellMH);
    }

    public <D> S configure(ComponentDriver<D> driver, CustomConfigurator<D> consumer, Wirelet... wirelets) {
        return configure(driver, e -> e, consumer, wirelets);
    }

    @Override
    public <C, D> S configure(ComponentDriver<D> driver, Function<D, C> factory, CustomConfigurator<C> consumer, Wirelet... wirelets) {
        ComponentNodeConfiguration node = PackedAssemblyContext.configure(this, (PackedComponentDriver<D>) driver, factory, consumer, wirelets);
        PackedInitializationContext ac = PackedInitializationContext.initialize(node);
        return newShell(ac);
    }

    /**
     * Create a new shell using the specified initialization context.
     * 
     * @param pic
     *            the initialization context to wrap
     * @return the new shell
     */
    private S newShell(PackedInitializationContext pic) {
        try {
            return (S) newShellMH.invoke(pic);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /**
     * Returns a set of the various modifiers that will by set on the root component. whether or not the type of shell being
     * created by this driver has an execution phase. This is determined by whether or not the shell implements
     * {@link AutoCloseable}.
     * 
     * @return whether or not the shell being produced by this driver has an execution phase
     */
    @Override
    public ComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    /** {@inheritDoc} */
    @Override
    public Image<S> newImage(Bundle<?> bundle, Wirelet... wirelets) {
        // Assemble the system with the ComponentModifier.IMAGE modifier added
        ComponentNodeConfiguration component = PackedAssemblyContext.assemble(bundle, modifiers | I_IMAGE, this, wirelets);

        // Return a new image that be people can use (Image::use)
        return new ShellImage(component);
    }

    /** {@inheritDoc} */
    @Override
    public S newShell(Bundle<?> bundle, Wirelet... wirelets) {
        // Assemble the system
        ComponentNodeConfiguration component = PackedAssemblyContext.assemble(bundle, modifiers, this, wirelets);

        // Initialize the system. And start it if necessary (if it is a guest)
        PackedInitializationContext pic = PackedInitializationContext.initialize(component);

        // Return the system in a new shell
        return newShell(pic);
    }

    /**
     * Returns the raw type of the shell instances that this driver creates.
     * 
     * @return the raw type of the shell instances that this driver creates
     */
    public Class<?> shellRawType() {
        return newShellMH.type().returnType();
    }

    // A method handle that takes an ArtifactContext and produces something that is compatible with A
    public static <A> ShellDriver<A> of(MethodHandles.Lookup caller, Class<A> shellType, MethodHandle mh) {
        // TODO validate type
        // shellType must match MH
        boolean isGuest = AutoCloseable.class.isAssignableFrom(shellType);
        // TODO fix....

        return new PackedShellDriver<>(isGuest, mh);
    }

    /** Options that can be applied when creating a shell driver. */
    interface Option {

        // If not autoclosable
        static Option forceGuest() {
            throw new UnsupportedOperationException();
        }

        static Option forceNonGuest() {
            throw new UnsupportedOperationException();
        }

        // Normally we just check if App.iface extends AutoClosable...
        // But might want to override this.
        // forceManaged, forceUnmanaged

        // String Reason??? This extension has been blacklisted
        @SafeVarargs
        static Option blacklistExtensions(Class<? extends Extension>... extensions) {
            throw new UnsupportedOperationException();
        }

        static Option blacklistExtensions(String... extensions) {
            throw new UnsupportedOperationException();
        }

        // Mapning af Execeptions/Errors....
        //// Saa er det let at rette i f.eks. App

        // Debug Options...

        // whitelistExtension(...)
        // blacklistExtension

        //// Can only use of them
        // Altsaa det maa vaere meget taet paa ArchUnit a.la.. Hmmm

        static Option nameProvider() {
            // prefix???
            // Ideen er ihvertfald at
            throw new UnsupportedOperationException();
        }

        static Option postfixWirelets(Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        // Hmmm... Fungere jo ikke rigtigt med image....
        static Option prefixWirelets(Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        // custom ServiceProvider..
        static Option serviceProvider() {
            throw new UnsupportedOperationException();
        }

        // Hmmm... Fungere jo ikke rigtigt med image......
        @SafeVarargs
        static Option whitelistExtensions(Class<? extends Extension>... extensions) {
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

    /** An implementation of {@link Image} used by {@link ShellDriver#newImage(Bundle, Wirelet...)}. */
    private final class ShellImage implements Image<S> {

        /** The assembled image node. */
        private final ComponentNodeConfiguration compConf;

        /**
         * Create a new image from the specified component.
         * 
         * @param compConf
         *            the assembled component
         */
        private ShellImage(ComponentNodeConfiguration compConf) {
            this.compConf = requireNonNull(compConf);
        }

        /** {@inheritDoc} */
        @Override
        public Component component() {
            return compConf.adaptToComponent();
        }

        /** {@inheritDoc} */
        @Override
        public S use(Wirelet... wirelets) {
            // Initialize a new system using the previously assembled node
            PackedInitializationContext pic = PackedInitializationContext.initializeFromImage(compConf, WireletPack.forImage(compConf, wirelets));

            // Wrap the system in a shell and return it
            return newShell(pic);
        }
    }
}

// Ideen er lidt at vi har en OptionList som aggregere alle options
// Det er en public klasse i packedapp.internal.shell.OptionAggregate
// Den har en PackedContainerConfiguration saa adgang til. og f.eks. kalder
// aggregate.addExtension(Class<? extends Extension>) <- may throw....
//static class ArtifactOptionAggregate {
//    ArtifactOptionAggregate(Option[] options) {}
//
//    ArtifactOptionAggregate with(Option[] options) {
//        throw new UnsupportedOperationException();
//    }
//}
//public static <A> ArtifactDriver<A> of(MethodHandles.Lookup caller, Class<A> shellType, Factory<? extends A> implementation) {
//  throw new UnsupportedOperationException();
//}
//<E extends A> ArtifactDriver<A> mapTo(Class<E> decoratingType, Function<A, E> decorator) {
//  // Ideen er egentlig at f.eks. kunne wrappe App, og tilfoeje en metode...
//  // Men altsaa, maaske er det bare at kalde metoderne direkte i context...
//  // PackedApp kalder jo bare direkte igennem
//  throw new UnsupportedOperationException();
//}

//
//static <A> A start(Class<A> shellType, ArtifactSource source, Wirelet... wirelets) {
//    // The only thing we save is defining a driver..
//    // But we need the driver for App#driver... so not much saved
//    throw new UnsupportedOperationException();
//}
//Supplier<A> startingProvider(ArtifactSource a, Wirelet... wirelets) {
//// Kunne ogsaa lave den paa image...
//// Men altsaa taenker vi godt vil have noget wirelets med...
//// <A> Supplier<A> ArtifactImage.supplier(ArtifactDriver<A> driver);
//throw new UnsupportedOperationException();
//}
///** Options that can be specified when creating a new driver or via {@link #withOptions(Option...)}. */

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
