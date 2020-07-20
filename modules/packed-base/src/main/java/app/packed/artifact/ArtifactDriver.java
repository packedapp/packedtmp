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

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;
import app.packed.service.Injector;
import packed.internal.artifact.AssembleOutput;
import packed.internal.artifact.PackedArtifactImage;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.WireletPack;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

/**
 * Artifact drivers are responsible for creating new artifacts by wrapping instances of {@link ArtifactContext}.
 * <p>
 * This class can be extended to create custom artifact types if the built-in artifact types such as {@link App} and
 * {@link Injector} are not sufficient. In fact, the default implementations of both {@link App} and {@link Injector}
 * are just thin facade that delegates all calls to an {@link ArtifactContext} instance.
 * <p>
 * Normally, you would never instantiate more then a single instance of any driver implementations.
 * <p>
 * Iff a driver creates artifacts with an execution phase. The artifact must implement {@link AutoCloseable}.
 * <p>
 * Implementations of this class must be safe for access by multiple threads concurrently.
 * 
 * @param <A>
 *            The type of artifact this driver creates.
 * @see App#driver()
 */

// Tror ikke artifacts kan bruge annoteringer???
// Altsaa maaske paa surragates???

// Ville maaske vaere fedt nok bare at kunne sige
// @OnShutdown()
// sysout "FooBar was removed"

// Support of injection of the artifact into the Container...
// We do not generally support this, as people are free to any artifact they may like.

// ? Er der services man gerne kun vil have er available til f.eks. PackedApp.class. 
// Men ikke til andre der kalder ind paa App.getServices();

// Non-Executable : Initialize
// Executable : Initialize | Start | Execute
public abstract class ArtifactDriver<A> {

    /** A type variable extractor to find the type of artifacts this driver creates. */
    private static final TypeVariableExtractor ARTIFACT_DRIVER_TV_EXTRACTOR = TypeVariableExtractor.of(ArtifactDriver.class);

//    static {
//        ModuleAccess.initialize(AppPackedArtifactAccess.class, new AppPackedArtifactAccess() {
//
//            /** {@inheritDoc} */
//            @Override
//            public <T> T newArtifact(ArtifactDriver<T> driver, ArtifactContext context) {
//                return driver.newArtifact(context);
//            }
//        });
//    }

    /** The type of artifact this driver produces. */
    private final Class<A> artifactType;

    /** Whether or not the created artifact has an execution phase. */
    private final boolean hasExecutionPhase;

    /** Creates a new driver. */
    @SuppressWarnings("unchecked")
    protected ArtifactDriver(/* Option... options */) {
        this.artifactType = (Class<A>) ARTIFACT_DRIVER_TV_EXTRACTOR.extract(getClass());

        // Hvad hvis vi ikke vil ekspornere en close metode...
        // F.eks. et eller andet der kun kan blive deployet paa en host...
        // Men brugere skal ikke kunne stoppe dem... //Option to override
        // Men okay hvad med TTL wirelets...
        this.hasExecutionPhase = AutoCloseable.class.isAssignableFrom(artifactType);
    }

    /** Creates a new driver. */
    private ArtifactDriver(ArtifactDriver<A> existing) {
        this.artifactType = existing.artifactType;
        this.hasExecutionPhase = existing.hasExecutionPhase;
    }

    /**
     * Returns the type of artifact this driver produce.
     * 
     * @return the type of artifact this driver produce
     */
    // RawType? Should we have a TypeLiteral???
    // For example, I create BigMap<String, Long>
    public final Class<A> artifactType() {
        return artifactType;
    }

    /**
     * Creates and initializes a new artifact using the specified source.
     * <p>
     * This method will invoke {@link #newArtifact(ArtifactContext)} to create the actual artifact.
     * 
     * @param source
     *            the source of the top-level container
     * @param wirelets
     *            any wirelets that should be used to create the artifact
     * @return the new artifact
     * @throws RuntimeException
     *             if the artifact could not be created
     */
    public final A instantiate(ArtifactSource source, Wirelet... wirelets) {
        return newArtifact(create(source, wirelets));
    }

    /**
     * Create, initialize and start a new artifact using the specified source.
     * 
     * @param source
     *            the source of the top-level container
     * @param wirelets
     *            any wirelets that should be used to create the artifact
     * @return the new artifact
     * @throws UnsupportedOperationException
     *             if the driver does not produce an artifact with an execution phase
     */
    public final A start(ArtifactSource source, Wirelet... wirelets) {
        ArtifactContext context = create(source, wirelets);
        context.start();
        return newArtifact(context);
    }

    static <A> A start(Class<A> artifactType, ArtifactSource source, Wirelet... wirelets) {
        // The only thing we save is defining a driver..
        // But we need the driver for App#driver... so not much saved
        throw new UnsupportedOperationException();
    }

    private ArtifactContext create(ArtifactSource source, Wirelet... wirelets) {
        PackedContainerConfiguration pcc;
        WireletPack wc;
        // Either we create from an image, or from a bundle
        if (source instanceof PackedArtifactImage) {
            PackedArtifactImage pai = (PackedArtifactImage) source;
            pcc = pai.configuration();
            wc = WireletPack.fromImage(pcc, pai.wirelets(), wirelets);
        } else { // assert Bundle?
            pcc = PackedContainerConfiguration.of(AssembleOutput.artifact(this), source, wirelets);
            pcc.assemble();
            wc = pcc.wireletContext;
        }
        return pcc.instantiateArtifact(wc);
    }

    // <T, R> // AppDriver<App, Void>

    // Kan take restart wirelets...\
    public final Object execute(ArtifactSource source, Wirelet... wirelets) {
        ArtifactContext context = create(source, wirelets);
        context.start();
        return null;
    }

    /**
     * Returns whether or not the type of artifact being created by this driver has an execution phase. This is determined
     * by whether or not the artifact implements {@link AutoCloseable}.
     * 
     * @return whether or not the artifact being produced by this driver has an execution phase
     */
    public final boolean hasExecutionPhase() {
        return hasExecutionPhase;
    }

    Supplier<A> startingProvider(ArtifactSource a, Wirelet... wirelets) {
        // Kunne ogsaa lave den paa image...
        // Men altsaa taenker vi godt vil have noget wirelets med...
        // <A> Supplier<A> ArtifactImage.supplier(ArtifactDriver<A> driver);
        throw new UnsupportedOperationException();
    }

    /**
     * Create a new artifact by wrapping an artifact context.
     * <p>
     * This method is invoked by the runtime via calls such as {@link #instantiate(ArtifactSource, Wirelet...)} and
     * {@link #start(ArtifactSource, Wirelet...)}.
     * <p>
     * The implementation of this method must be safe for use by multiple concurrent threads.
     * 
     * @param context
     *            the artifact context to wrap
     * @return the new artifact
     */
    protected abstract A newArtifact(ArtifactContext context);

    // Hmmm
    public final <C> A configure(Function<ContainerConfiguration, C> factory, Consumer<C> consumer, Wirelet... wirelets) {
        PackedContainerConfiguration pcc = PackedContainerConfiguration.of(AssembleOutput.artifact(this), consumer, wirelets);
        C c = factory.apply(pcc);
        consumer.accept(c);
        pcc.assemble();
        ArtifactContext pac = pcc.instantiateArtifact(pcc.wireletContext);
        return newArtifact(pac);
    }

    public static <A> ArtifactDriver<A> of(MethodHandles.Lookup caller, Class<A> artifactType, Factory<? extends A> implementation) {
        throw new UnsupportedOperationException();
    }

    public static <A> ArtifactDriver<A> of(MethodHandles.Lookup caller, Class<A> artifactType, Class<? extends A> implementation) {

        // ArtifactContext + All public servervices are available...

        throw new UnsupportedOperationException();
        // of(App.class, PackedApp.class);
    }

    final ArtifactDriver<A> withOptions(Option... options) {
        return new WithOptionDriver<>(this);
//        ArtifactDriver<App> ad = App.driver().withOptions(Option.blacklistExtensions(ServiceExtension.class));
//        ad.instantiate(new Bundle() {
//
//            @Override
//            protected void compose() {}
//        });
//
//        throw new UnsupportedOperationException();
    }

    /** A special driver used for {@link #withOptions(Option...)}. */
    private static class WithOptionDriver<T> extends ArtifactDriver<T> {

        /** The existing driver to wrap. */
        final ArtifactDriver<T> existing;

        WithOptionDriver(ArtifactDriver<T> existing) {
            super(existing);
            this.existing = requireNonNull(existing);
        }

        /** {@inheritDoc} */
        @Override
        protected T newArtifact(ArtifactContext context) {
            return existing.newArtifact(context);
        }
    }

    /** Options that can be specified when creating a new driver or via {@link #withOptions(Option...)}. */

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

        // custom ServiceProvider..
        static Option serviceProvider() {
            throw new UnsupportedOperationException();
        }

        static Option nameProvider() {
            // prefix???
            // Ideen er ihvertfald at
            throw new UnsupportedOperationException();
        }

        // Mapning af Execeptions/Errors....
        //// Saa er det let at rette i f.eks. App

        // Debug Options...

        // whitelistExtension(...)
        // blacklistExtension

        //// Can only use of them
        // Altsaa det maa vaere meget taet paa ArchUnit a.la.. Hmmm

        // String Reason??? This extension has been blacklisted
        @SafeVarargs
        static Option blacklistExtensions(Class<? extends Extension>... extensions) {
            throw new UnsupportedOperationException();
        }

        static Option blacklistExtensions(String... extensions) {
            throw new UnsupportedOperationException();
        }

        // Hmmm... Fungere jo ikke rigtigt med image......
        @SafeVarargs
        static Option whitelistExtensions(Class<? extends Extension>... extensions) {
            throw new UnsupportedOperationException();
        }

        // Hmmm... Fungere jo ikke rigtigt med image....
        static Option prefixWirelets(Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        static Option postfixWirelets(Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }
    }

    // Ideen er lidt at vi har en OptionList som aggregere alle options
    // Det er en public klasse i packedapp.internal.artifact.OptionAggregate
    // Den har en PackedContainerConfiguration saa adgang til. og f.eks. kalder
    // aggregate.addExtension(Class<? extends Extension>) <- may throw....
    static class ArtifactOptionAggregate {
        ArtifactOptionAggregate(Option[] options) {}

        ArtifactOptionAggregate with(Option[] options) {
            throw new UnsupportedOperationException();
        }
    }
}

class XextVersion<T> {

    // Ja den er faktisk fin nok syntes jeg...

    final <E extends T> ArtifactDriver<T> mapTo(Class<E> decoratingType, Function<T, E> decorator) {
        // Ideen er egentlig at f.eks. kunne wrappe App, og tilfoeje en metode...
        // Men altsaa, maaske er det bare at kalde metoderne direkte i context...
        // PackedApp kalder jo bare direkte igennem
        throw new UnsupportedOperationException();
    }
}

// Droppet for Option....
// Taget som en ArtifactDriver(Option... options)
// Ja vi dropper configure.... Og saa bliver den en blanding af option objekter
// Og saa evt. metoder der kan overskrives...
// ArchUnit...
//protected void configure() {
//    // Default Constraints....
//
//    // Kan vi bruge noget af det samme som ComponentExtension...
//    // De regler kan vel bruges paa baadde
//    // extension nivuea, ArtifactNiveau, Bundle Niveau
//
//    // configuration
//    //// forbidden extensions (lifecycle primarily)
//    //// Allow injection of ArtifactInstance (for example, App).
//    //// In which case it will be injectable into any component...
//
//    // Alternativ
//    // @ArtifactDriver.Limitations(forbiddenExtensions(LifecycleExtension.class)
//
//    // Hvordan sikre vi os at configure er koert?????
//    // Bruger instantitere den jo selv...
//
//    // Taenker vi godt kan kalde den fra constructeren....
//
//    // Either a configure() class
//    // For example, supports lifecycle... if not-> Lifecycle cycle methods on
//    // PackedContainer (Artifact?) throws Unsupported
//
//    // Needs Lifecycle
//}

//// Lav en constructor baade med og uden Alt
//// ArtifactDriver(boolean isRunning)
//// ArtifactDriver(boolean isRunning, Settings settings)
//static abstract class Settings {
//  protected abstract void configure();
//
//  protected final void disableExtensions() {
//
//  }
//}

// non execution -> Create...

// Ideen var man skulle kunne angive nogle prefix wirelets naar man lavede en artifact...
// Men det er nu lavet om til options pre and post wirelets
// executing -> Create (and initialize), start, startAsync, Execute, executeAsync
// StartExecutor ?
//final T create(Assembly source, Wirelet... artifactWirelets) {
//
//    // Ideen er lidt at Artifact Implementering, kan kalde med dens egen wirelets...
//    // ala
//    // Maaske er det en option.. .. ellers andThen... eller have en Wirelet customWirelets(Assemble)..
//
//    // start(Assembly, Wirelet... wirelets) {
//    // create(Assembly, wirelets, ArtifactWirelets.startSynchronous());
//    // create(Assembly, wirelets, ArtifactWirelets.startAsynchronous());
//    // }
//    // What about execute....
//    throw new UnsupportedOperationException();
//}