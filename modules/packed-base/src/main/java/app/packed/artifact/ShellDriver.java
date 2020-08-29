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
import java.lang.invoke.MethodType;
import java.util.function.Function;

import app.packed.component.Bundle;
import app.packed.component.CustomConfigurator;
import app.packed.component.WireableComponentDriver;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.service.Injector;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedWireableComponentDriver;
import packed.internal.lifecycle.phases.ConstructionContext;
import packed.internal.lifecycle.phases.PackedAssemblyContext;
import packed.internal.util.ThrowableUtil;

/**
 * Artifact drivers are responsible for creating new artifacts by wrapping instances of {@link ShellContext}.
 * <p>
 * This class can be extended to create custom artifact types if the built-in artifact types such as {@link App} and
 * {@link Injector} are not sufficient. In fact, the default implementations of both {@link App} and {@link Injector}
 * are just thin facade that delegates all calls to an {@link ShellContext} instance.
 * <p>
 * Normally, you should never instantiate more then a single instance of driver for any artifact implementation.
 * <p>
 * Iff a driver creates artifacts with an execution phase. The artifact must implement {@link AutoCloseable}.
 * 
 * @param <A>
 *            The type of artifact this driver creates.
 * @see App#driver()
 */

//Tror bare vi laver om til et interface.... 

// Tror ikke artifacts kan bruge annoteringer??? Altsaa maaske paa surragates???
// Ville maaske vaere fedt nok bare at kunne sige
// @OnShutdown()
// sysout "FooBar was removed"

// Support of injection of the artifact into the Container...
// We do not generally support this, as people are free to any artifact they may like.
// Which would break encapsulation

// Non-Executable : Initialize
// Executable : Initialize | Start | Execute
public final class ShellDriver<A> {

    /** The type of artifact this driver produces. */
    private final Class<A> artifactType;

    /** Whether or not the created artifact has an execution phase. */
    private final boolean isGuest;

    /** The method handle responsible for creating the new artifact. */
    private final MethodHandle mh;

    /**
     * Creates a new driver.
     * 
     * @param artifactType
     *            the type of artifact that is created
     * @param mh
     *            the method handle that creates the actual artifact
     */
    @SuppressWarnings("unchecked")
    private ShellDriver(Class<?> artifactType, MethodHandle mh) {
        this.artifactType = (Class<A>) requireNonNull(artifactType);
        this.isGuest = AutoCloseable.class.isAssignableFrom(artifactType);
        this.mh = requireNonNull(mh);
    }

    public <D> A configure(WireableComponentDriver<D> driver, CustomConfigurator<D> consumer, Wirelet... wirelets) {
        return configure(driver, e -> e, consumer, wirelets);
    }

    public <C, D> A configure(WireableComponentDriver<D> driver, Function<D, C> factory, CustomConfigurator<C> consumer, Wirelet... wirelets) {
        ComponentNodeConfiguration node = PackedAssemblyContext.configure(this, (PackedWireableComponentDriver<D>) driver, factory, consumer, wirelets);
        ShellContext ac = ConstructionContext.constructArtifact(node, node.wirelets);
        return newArtifact(ac);
    }

    /**
     * Creates and initializes a new shell (and system) using the specified bundle.
     * <p>
     * This method will invoke {@link #newArtifact(ShellContext)} to create the actual artifact.
     * 
     * @param bundle
     *            the system bundle
     * @param wirelets
     *            any wirelets that should be used when assembling or initializing the system
     * @return the new shell
     * @throws RuntimeException
     *             if the artifact could not be created
     */
    public A initialize(Bundle<?> bundle, Wirelet... wirelets) {
        ComponentNodeConfiguration node = PackedAssemblyContext.assemble(bundle, 0, this, wirelets);
        ShellContext context = ConstructionContext.constructArtifact(node, node.wirelets);
        return newArtifact(context);
    }

    /**
     * Returns whether or not the type of artifact being created by this driver has an execution phase. This is determined
     * by whether or not the artifact implements {@link AutoCloseable}.
     * 
     * @return whether or not the artifact being produced by this driver has an execution phase
     */
    public boolean isGuest() {
        return isGuest;
    }

    /**
     * Create a new artifact using a previously supplied method handle.
     * 
     * @param context
     *            the artifact context to use for instantiating the artifact
     * @return the new artifact
     */
    A newArtifact(ShellContext context) {
        try {
            return (A) mh.invoke(context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    public Image<A> newImage(Bundle<?> bundle, Wirelet... wirelets) {
        return new PackedImage<>(this, bundle, wirelets);
    }

    /**
     * Returns the raw type of artifacts this driver produces.
     * 
     * @return the raw type of artifacts this driver produces
     */
    public Class<A> rawType() {
        // Should we have a TypeLiteral as well???
        // For example, if we create BigMap<String, Long>
        return artifactType;
    }

    /**
     * Create, initialize and start a new artifact using the specified source.
     * 
     * @param bundle
     *            the source of the top-level container
     * @param wirelets
     *            any wirelets that should be used to create the artifact
     * @return the new artifact
     * @throws UnsupportedOperationException
     *             if the driver does not produce an artifact with an execution phase
     */
    public A start(Bundle<?> bundle, Wirelet... wirelets) {
        ComponentNodeConfiguration node = PackedAssemblyContext.assemble(bundle, 0, this, wirelets);
        ShellContext context = ConstructionContext.constructArtifact(node, node.wirelets);
        context.start();
        return newArtifact(context);
    }

    public static <A> ShellDriver<A> of(MethodHandles.Lookup caller, Class<A> artifactType, Class<? extends A> implementation) {
        // Vi vil gerne bruge artifact type som navnet paa artifacten... istedet for implementationen
        MethodType mt = MethodType.methodType(void.class, ShellContext.class);
        final MethodHandle mh;
        try {
            mh = caller.findConstructor(implementation, mt);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return new ShellDriver<>(artifactType, mh);
    }

//    public static <A> ArtifactDriver<A> of(MethodHandles.Lookup caller, Class<A> artifactType, Factory<? extends A> implementation) {
//        throw new UnsupportedOperationException();
//    }

    // A method handle that takes an ArtifactContext and produces something that is compatible with A
    public static <A> ShellDriver<A> of(MethodHandles.Lookup caller, Class<A> artifactType, MethodHandle mh) {
        // TODO validate type
        return new ShellDriver<>(artifactType, mh);
    }

//  <E extends A> ArtifactDriver<A> mapTo(Class<E> decoratingType, Function<A, E> decorator) {
//      // Ideen er egentlig at f.eks. kunne wrappe App, og tilfoeje en metode...
//      // Men altsaa, maaske er det bare at kalde metoderne direkte i context...
//      // PackedApp kalder jo bare direkte igennem
//      throw new UnsupportedOperationException();
//  }

//
//    static <A> A start(Class<A> artifactType, ArtifactSource source, Wirelet... wirelets) {
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
    // List<ArtifactDriver.Option> BaseEnvironment.defaultOptions(Class<?> artifactDriver);
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

    static class Option {

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

        // Ideen var man skulle kunne angive nogle prefix wirelets naar man lavede en artifact...
        // Men det er nu lavet om til options pre and post wirelets
        // executing -> Create (and initialize), start, startAsync, Execute, executeAsync
        // StartExecutor ?
        // final T create(Assembly source, Wirelet... artifactWirelets) {
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
    // Det er en public klasse i packedapp.internal.artifact.OptionAggregate
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
