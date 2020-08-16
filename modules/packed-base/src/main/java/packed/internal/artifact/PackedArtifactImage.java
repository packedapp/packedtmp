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

import app.packed.artifact.ArtifactContext;
import app.packed.artifact.ArtifactImage;
import app.packed.artifact.ArtifactSource;
import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.ComponentStream;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerDescriptor;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.container.PackedContainerRole;

/** The default implementation of {@link ArtifactImage}. */
public final class PackedArtifactImage implements ArtifactImage {

    /** The configuration of the root container. */
    private final PackedContainerRole pcc;

    /**
     * Any wirelets that have been applied to the image. Might consist of a chain of wirelet containers with repeat usage of
     * {@link #with(Wirelet...)}.
     */
    @Nullable
    private final WireletPack wc;

    Class<? extends Bundle<?>> bundleType;

    /**
     * Creates a new image from the specified configuration and wirelets.
     * 
     * @param pcc
     *            the container configuration to wrap
     * @param wc
     *            any wirelets specified when creating the image or later via {@link #with(Wirelet...)}
     */
    public PackedArtifactImage(PackedContainerRole pcc, Class<? extends Bundle<?>> bundleType, @Nullable WireletPack wc) {
        this.pcc = requireNonNull(pcc);
        this.wc = wc;
        this.bundleType = requireNonNull(bundleType);
    }

    public ArtifactContext newContext(Wirelet... wirelets) {
        return InstantiationContext.instantiateArtifact(pcc.component, WireletPack.fromImage(pcc, wc, wirelets));
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return pcc.component.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> description() {
        return Optional.ofNullable(pcc.component.getDescription());
    }

    /** {@inheritDoc} */
    @Override
    public ContainerDescriptor descriptor() {
        // Need to support wirelet context...
        ContainerDescriptor.Builder builder = new ContainerDescriptor.Builder(sourceType());
        pcc.buildDescriptor(builder);
        return builder.build();
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        // Only if a name has been explicitly set?
        // Or can we include "FooBar?"
        // Return Optional<String>????
        return wc == null ? pcc.component.getName() : wc.name(pcc.component);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Bundle<?>> sourceType() {
        return bundleType; // images can only be created from bundles
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream(ComponentStream.Option... options) {
        return pcc.component.adaptToComponent().stream(options);
    }

    /** {@inheritDoc} */
    @Override
    public PackedArtifactImage with(Wirelet... wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        return wirelets.length == 0 ? this : new PackedArtifactImage(pcc, bundleType, WireletPack.fromImage(pcc, wc, wirelets));
    }

    /**
     * If the specified source is an image returns the image with any specified wirelets applied. If the specified source is
     * a bundle creates and returns a new image from the specified bundle.
     * 
     * @param source
     *            the artifact source
     * @param wirelets
     *            any wirelet
     * @return the image
     */
    @SuppressWarnings("unchecked")
    public static PackedArtifactImage lazyCreate(ArtifactSource source, Wirelet... wirelets) {
        if (source instanceof PackedArtifactImage) {
            return ((PackedArtifactImage) source).with(wirelets);
        }
        Bundle<?> bundle = (Bundle<?>) source;
        PackedContainerRole pcc = PackedContainerRole.assemble(PackedAccemblyContext.image(), bundle, wirelets);
        return new PackedArtifactImage(pcc, (Class<? extends Bundle<?>>) bundle.getClass(), pcc.component.wireletContext);
    }
}

// De kunne jo strength taget vaere metoder paa selve imaged og ikke wirelets.
// Vi kan jo sagtens internt lave det om til wirelets...
// Der er bare ingen grund til at lave det public...
final class XArtifactImageWirelets {

    // retainStackTracesForEachInstantiation...
    /// Her ligger vi jo lige 1000 ns oveni hvis vi vil se hvor den er instantieret.

    // Maximum number of instantiations times...
    // Could, for example, be one for native.
    // The only think we want to instantiate the application once... And then forget everything

    /**
     * @return lazy
     */
    static ArtifactImage lazy() {
        throw new UnsupportedOperationException();
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

    // Ideen er at vi kun skal lave en container en gang. F.eks. NativeBoot
    static Wirelet oneShot() {
        throw new UnsupportedOperationException();
    }

    enum Mode {}
}
