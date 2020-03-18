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
package packed.internal.artifact;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import app.packed.analysis.BundleDescriptor;
import app.packed.artifact.ArtifactContext;
import app.packed.artifact.ArtifactDriver;
import app.packed.artifact.ArtifactImage;
import app.packed.artifact.Assembly;
import app.packed.base.Nullable;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.Wirelet;
import packed.internal.component.ComponentConfigurationToComponentAdaptor;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.WireletContext;
import packed.internal.moduleaccess.ModuleAccess;

/**
 * Artifact images are immutable ahead-of-time configured artifacts. By configuring an artifact ahead of time, the
 * actual time to instantiation an artifact can be severely decreased often down to a couple of microseconds. In
 * addition to this, artifact images can be reusable, so you can create multiple artifacts from a single image.
 * 
 * Creating artifacts in Packed is already really fast, and you can easily create one 10 or hundres of microseconds. But
 * by using artifact images you can into hundres or thousounds of nanoseconds.
 * <p>
 * Use cases: Extremely fast startup.. graal
 * 
 * Instantiate the same container many times
 * <p>
 * Limitations:
 * 
 * No structural changes... Only whole artifacts
 * 
 * <p>
 * An image can be used to create new instances of {@link app.packed.artifact.App}, {@link BundleDescriptor} or other
 * artifact images. It can not be used with {@link Bundle#link(Bundle, Wirelet...)}.
 */
public final class PackedArtifactImage implements ArtifactImage {

    /** The configuration of the root container. */
    private final PackedContainerConfiguration pcc;

    /** Any wirelets that have been applied to the image. */
    @Nullable
    private final WireletContext wc;

    /**
     * Creates a new image from the specified configuration and wirelets.
     * 
     * @param pcc
     *            the configuration this image will wrap
     * @param wc
     *            any wirelets for the image configuration or artifact instantiation
     */
    private PackedArtifactImage(PackedContainerConfiguration pcc, @Nullable WireletContext wc) {
        this.pcc = requireNonNull(pcc);
        this.wc = wc;
    }

    public PackedContainerConfiguration configuration() {
        return pcc;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return pcc.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> description() {
        return Optional.ofNullable(pcc.getDescription());
    }

    /** {@inheritDoc} */
    @Override
    public BundleDescriptor descriptor() {
        // Need to support wirelet context...
        BundleDescriptor.Builder builder = new BundleDescriptor.Builder(sourceType());
        pcc.buildDescriptor(builder);
        return builder.build();
    }

    /** {@inheritDoc} */
    // Only if a name has been explicitly set?
    // Or can we include "FooBar?"
    @Override
    public String name() {
        // Return Optional<String>????
        return wc == null ? pcc.getName() : wc.name();
    }

    /**
     * Instantiates a new artifact using the specified driver.
     * 
     * @param <T>
     *            the type of artifact to instantiate
     * @param driver
     *            the artifact driver
     * @param wirelets
     *            any wirelets used for instantiation
     * @return the instantiated artifact
     */
    public <T> T newArtifact(ArtifactDriver<T> driver, Wirelet... wirelets) {
        WireletContext newWc = WireletContext.create(pcc, this.wc, wirelets);
        ArtifactContext context = pcc.instantiateArtifact(newWc).newArtifactContext(); // Does the actual instantiation
        return ModuleAccess.artifact().newArtifact(driver, context);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Bundle> sourceType() {
        return (Class<? extends Bundle>) pcc.sourceType();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream(ComponentStream.Option... options) {
        return ComponentConfigurationToComponentAdaptor.of(pcc).stream(options);
    }

    /** {@inheritDoc} */
    @Override
    public PackedArtifactImage with(Wirelet... wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        return wirelets.length == 0 ? this : new PackedArtifactImage(pcc, WireletContext.create(pcc, wc, wirelets));
    }

    /**
     * Creates an artifact image using the specified source.
     *
     * @param source
     *            the source of the image
     * @param wirelets
     *            any wirelets to use when construction the image. The wirelets will also be available when instantiating an
     *            actual artifact
     * @return the image that was built
     * @throws RuntimeException
     *             if the image could not be constructed
     */
    public static PackedArtifactImage build(Assembly source, Wirelet... wirelets) {
        if (source instanceof PackedArtifactImage) {
            return ((PackedArtifactImage) source).with(wirelets);
        }
        // TODO check that it is a bundle????
        PackedContainerConfiguration pcc = new PackedContainerConfiguration(BuildOutput.image(), source, wirelets);
        return new PackedArtifactImage(pcc.doBuild(), pcc.wireletContext);
    }

    // repeatable/singleUse

    // Lifecycle... Men det afhaender jo ogsaa af repeatable/single use....
    // Man kan jo ikke initializisere en repeatable....

    // lazy/non-lazy

    // Creates an image that will be initialized the first time it is executed...
    // F.eks. name will initialize it...
    // I think we will apply wirelets lazily as well.

    //// GraalVM <- Calculere alle lazy images??? Det taenker jeg...
    // Vi skal double down med det lazy paa runtime, og validation paa test time
    // Evt. et build plugin der validere det????

    /**
     * @return lazy
     */
    static ArtifactImage lazy() {
        throw new UnsupportedOperationException();
    }

    enum Mode {}
}

// De kunne jo strength taget vaere metoder paa imaged og ikke wirelets.
// Vi kan jo sagtens internt lave det om til wirelets...
// Der er bare ingen grund til at lave det public...
final class ArtifactImageWirelets {

    // retainStackTracesForEachInstantiation...
    /// Her ligger vi jo lige 1000 ns oveni hvis vi vil se hvor den er instantieret.

    // Maximum number of instantiations times...
    // Could, for example, be one for native.
    // The only think we want to instantiate the application once... And then forget everything

    // Ideen er at vi kun skal lave en container en gang. F.eks. NativeBoot
    static Wirelet oneShot() {
        throw new UnsupportedOperationException();
    }
}

class BadIdeas {

    // public static ArtifactImage of(Class<? extends Bundle> bundle, Wirelet... wirelets) {
    // requireNonNull(bundle, "bundle is null");
    // ContainerSourceModel csm = ContainerSourceModel.of(bundle);
    // Bundle b;
    // try {
    // b = (Bundle) csm.emptyConstructor().invoke();
    // } catch (Throwable e) {
    // ThrowableUtil.rethrowErrorOrRuntimeException(e);
    // throw new UndeclaredThrowableException(e);
    // }
    // return of(b, wirelets);
    // }

    // Skal bruge en artifact driver til at instantiatere dem jo....
    void run(String[] args, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    void run(Wirelet... wirelets) {
        // Paa den anden side, hvis vi f.eks. faar Job<R>
        // Saa vil run jo se anderledes ud
        // Will create an artifact of unknown type....
        // Ideen er lidt at App.run()... aldrig egentlig laver en app.

        // ArtifactDriver'en kan jo ogsaa goere et ellet andet.

        throw new UnsupportedOperationException();
    }

    CompletableFuture<Void> runAsync(Wirelet... wirelets) {
        // Will create an artifact of unknown type....
        // Ideen er lidt at App.run()... aldrig egentlig laver en app.
        throw new UnsupportedOperationException();
    }
}
