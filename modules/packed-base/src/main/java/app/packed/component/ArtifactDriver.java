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
package app.packed.component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Function;

import app.packed.base.Nullable;
import app.packed.base.TypeToken;
import app.packed.inject.ServiceLocator;
import app.packed.state.Host;
import app.packed.state.InitializationException;
import app.packed.state.PanicException;
import packed.internal.classscan.InstantiatorBuilder;
import packed.internal.component.PackedArtifactDriver;
import packed.internal.component.PackedInitializationContext;

/**
 * Artifact drivers are responsible for creating artifacts, for example, instances of {@link App}.
 * <p>
 * This class can be used to create custom artifact types if the built-in artifact types such as {@link App} and
 * {@link ServiceLocator} are not sufficient. In fact, the default implementations of both {@link App} and
 * {@link ServiceLocator} uses an artifact driver themselves.
 * <p>
 * Normally, you would never create more than a single instance of a artifact driver.
 * 
 * @param <A>
 *            The type of artifacts this driver creates.
 * 
 * @see App#driver()
 * @apiNote In the future, if the Java language permits, {@link ArtifactDriver} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
// Environment + Shell + Result
public interface ArtifactDriver<A> {

    // forAnalysis? Ville gerne vaere explicit om at vi ikke analysere
    // inde i metoden.. Men at folk kan bruge componenten.
    Component analyze(Assembly<?> assembly, Wirelet... wirelets);

    /**
     * Builds a new image using the specified assembly and optional wirelets.
     * 
     * @param assembly
     *            the assembly that should be used to build the image
     * @param wirelets
     *            optional wirelets
     * @return a new image
     * @throws BuildException
     *             if the image could not build
     */
    Image<A> buildImage(Assembly<?> assembly, Wirelet... wirelets);

    <CO extends Composer<?>, CC extends ComponentConfiguration> A compose(ComponentDriver<CC> componentDriver,
            Function<? super CC, ? extends CO> composerFactory, Composable<? super CO> consumer, Wirelet... wirelets);

    /**
     * Create a new artifact using the specified assembly.
     * 
     * @param assembly
     *            the system bundle
     * @param wirelets
     *            optional wirelets
     * @return the new artifact or null if void artifact
     * @throws BuildException
     *             if the artifact could not be assembled properly
     * @throws InitializationException
     *             if the artifact failed to initializing
     * @throws PanicException
     *             if the artifact had an executing phase and it fails
     */
    @Nullable
    A use(Assembly<?> assembly, Wirelet... wirelets);

    // driver.use(A, W1, W2) == driver.with(W1).use(A, W2)
    // Hmmm, saa er den jo lige pludselig foerend..
    // ComponentDriveren
    // Maaske det giver mening alligevel...
    // Det er ihvertfald lettere at forklare...
    ArtifactDriver<A> with(Wirelet wirelet);

    ArtifactDriver<A> with(Wirelet... wirelets);

    /**
     * Returns a daemon artifact driver.
     * 
     * @return a daemon artifact driver
     */
    // Hvad skal default lifestate vaere for
    static ArtifactDriver<Void> daemon() {
        return PackedArtifactDriver.DAEMON;
        // ArtifactDriver.Builder<Void> daemonBuilder()
        // ArtifactDriver.Builder<T> result(...)
        // ArtifactDriver.Builder<T> shell(...)
    }

    /**
     * Returns an artifact driver that can be used for analysis. Statefull
     * 
     * @return the default artifact driver for analysis
     */
    // maybe just analyzer
    // I think it should fail if used to create images/instantiate anything
    static ArtifactDriver<?> defaultAnalyzer() {
        return daemon();
    }

    /**
     * Creates a new artifact driver.
     * <p>
     * The specified implementation can have the following types injected.
     * 
     * If the specified implementation implements {@link AutoCloseable} a {@link Host} can also be injected.
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
    static <S> ArtifactDriver<S> of(MethodHandles.Lookup caller, Class<? extends S> implementation) {
        // We automatically assume that if the implementation implements AutoClosable. Then we need a guest.
        boolean isGuest = AutoCloseable.class.isAssignableFrom(implementation);

        if (implementation == Void.class) {
            throw new IllegalArgumentException("Cannot specify Void.class use daemon() instead");
        }

        // We currently do not support @Provide ect... Don't know if we ever will
        // Create a new MethodHandle that can create artifact instances.

        InstantiatorBuilder ib = InstantiatorBuilder.of(caller, implementation, PackedInitializationContext.class);
        ib.addKey(Component.class, PackedInitializationContext.MH_COMPONENT, 0);
        ib.addKey(ServiceLocator.class, PackedInitializationContext.MH_SERVICES, 0);
        if (isGuest) {
            ib.addKey(Host.class, PackedInitializationContext.MH_CONTAINER, 0);
        }

        MethodHandle mh = ib.build();
        return new PackedArtifactDriver<>(isGuest, mh);
    }

    static <A> ArtifactDriver<A> of(MethodHandles.Lookup caller, Class<A> artifactType, MethodHandle mh) {
        return PackedArtifactDriver.of(caller, artifactType, mh);
    }

    static <S> ArtifactDriver<S> ofStateless(MethodHandles.Lookup caller, Class<? extends S> implementation) {
        throw new UnsupportedOperationException();
    }
}

interface ZArtifactDriver<A> {

    default void analyze(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

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

        // A specific type of analyze that throws ValidationError...
        throw new UnsupportedOperationException();
    }

    // Ideen er at man kan smide checked exceptions...
    default A invoke(Assembly<?> assembly, Wirelet... wirelets) throws Throwable {
        // Skal vel ogsaa tilfoejes paa Image saa.. og paa Host#start()... lots of places

        // Her er ihvertfald noget der skal kunne konfigureres
        throw new UnsupportedOperationException();
    }

}

interface ZArtifactDriverBuilders {

    // Enten returnere ComponentAnalysis eller Component...
    // CA kan have en masse hjaelpe metoder
    @SuppressWarnings("unused")
    private static Component analyze(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

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

        // Pre, Post wirelets
        // Whilelist/Blacklist extensions

        // Generic Parameterized types... F.eks. Job<R> or BigMap<K, V>
        // Maaske vil vi ikke have component med...

        // Hmm har vi brug for klassen foerend til allersidst???
        default <A> ArtifactDriver<A> build(Class<A> artifactType) {
            throw new UnsupportedOperationException();
        }

        // hostless er maaske bedre????

        default ZArtifactDriverBuilders.Builder<T> stateless() {
            return this;
        }

        // CompletableFuture<A> asynchronous();/??
//        /**
//         * Returns a set of the various modifiers that will by set on the underlying component. whether or not the type of artifact
//         * being created by this driver has an execution phase. This is determined by whether or not the artifact implements
//         * {@link AutoCloseable}.
//         * 
//         * @return whether or not the artifact being produced by this driver has an execution phase
//         */
//        // Saa heller descriptor();?????
        // Men hvorfor en descriptor. Kan det ikke vaere direkte paa driveren.
        // Ikke hvis man skal kunne faa en descriptor fra attributes. Men hvor
        // man ikke skal have rettigheder til direkte at
//        ComponentModifierSet modifiers();

        // Methods to analyze...
        // Pair<Component, Artifact>?
    }
}
//Main Functionality
//Make artifacts
//Make images
//Analyze, validate, print, ect...

//Vi kaldte den shell engang. Men gik tilbage til Artifact. Fordi det virker aandsvagt at se man skal have
//en stateless artifact. Og det styre saa alt andet... En artifact er mere noget man laver til et eksisterende system
//Ikke selv systeme.
//Altsaa hvad hvis jeg vil have et ActorSystem.. til en del component...

//Tror ikke artifacts kan bruge annoteringer??? Altsaa maaske paa surragates???
//Ville maaske vaere fedt nok bare at kunne sige
//@OnShutdown()
//sysout "FooBar was removed"

// Det ville jo vaere oplagt at kunne bruge f.eks. @Provide...
// Men artifakt'en bliver foerst lavet meget senere...

//Support of injection of the artifact into the Container...
//We do not generally support this, as people are free to use any artifact they may like.
//Which would break encapsulation