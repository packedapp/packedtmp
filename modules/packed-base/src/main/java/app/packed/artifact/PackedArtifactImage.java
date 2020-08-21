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

import app.packed.component.Bundle;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerDescriptor;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.lifecycle.phases.ConstructionContext;
import packed.internal.lifecycle.phases.PackedAssemblyContext;

/** The default implementation of {@link ArtifactImage}. */
// Taenker vi maaske skal flytte den internt?
// Altsaa ved ikke lige hvordan det hosted image kommer til at fungere...
final class PackedArtifactImage<A> implements ArtifactImage<A> {

    /** The driver used to create the artifact. */
    // We should use the driver when creating the actual root node...
    // I'm not sure we should need to store it.
    private final ArtifactDriver<A> driver;

    /** The configuration of the root container. */
    private final ComponentNodeConfiguration node;

    /**
     * Creates a new image from the specified configuration and wirelets.
     * 
     * @param node
     *            the container configuration to wrap
     */
    private PackedArtifactImage(ComponentNodeConfiguration node, ArtifactDriver<A> driver) {
        this.node = requireNonNull(node);
        this.driver = driver;
        // this.bundleType = (Class<? extends Bundle<?>>) requireNonNull(bundleType);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return node.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public A create(Wirelet... wirelets) {
        return driver.newArtifact(newContext(wirelets));
    }

    /** {@inheritDoc} */
    @Override
    public ContainerDescriptor descriptor() {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ContainerDescriptor.Builder builder = new ContainerDescriptor.Builder((Class) Bundle.class);
        node.container().buildDescriptor(builder);
        return builder.build();
    }

    /** {@inheritDoc} */
    @Override
    public String artifactName() {
        // Ahhh det er
        // Only if a name has been explicitly set?
        // Or can we include "FooBar?"
        // Return Optional<String>????
        return node.getName();
    }

    public ArtifactContext newContext(Wirelet... wirelets) {
        return ConstructionContext.constructArtifact(node, WireletPack.forImage(node, wirelets));
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath path() {
        // Hmm
        return ComponentPath.ROOT;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> rawArtifactType() {
        return driver.rawType();
    }

    /** {@inheritDoc} */
    @Override
    public A start(Wirelet... wirelets) {
        return driver.newArtifact(newContext(wirelets));
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream(ComponentStream.Option... options) {
        return node.adaptToComponent().stream(options);
    }

    /**
     * If the specified source is an image returns the image with any specified wirelets applied. If the specified source is
     * a bundle creates and returns a new image from the specified bundle.
     * 
     * @param driver
     *            the artifact driver
     * @param bundle
     *            the artifact source
     * @param wirelets
     *            any wirelet
     * @return the image
     */
    static <A> PackedArtifactImage<A> newImage(ArtifactDriver<A> driver, Bundle<?> bundle, Wirelet... wirelets) {
        ComponentNodeConfiguration node = PackedAssemblyContext.assembleImage(driver, bundle, wirelets);
        return new PackedArtifactImage<A>(node, driver);
    }
}
//
///** {@inheritDoc} */
//@Override
//public PackedArtifactImage with(Wirelet... wirelets) {
//  requireNonNull(wirelets, "wirelets is null");
//  return wirelets.length == 0 ? this : new PackedArtifactImage(node, bundleType, WireletPack.fromImage(node.container(), this.wirelets, wirelets));
//}
// De kunne jo strength taget vaere metoder paa selve imaged og ikke wirelets.
// Vi kan jo sagtens internt lave det om til wirelets...
// Der er bare ingen grund til at lave det public...

// retainStackTracesForEachInstantiation...
/// Her ligger vi jo lige 1000 ns oveni hvis vi vil se hvor den er instantieret.

// Maximum number of instantiations times...
// Could, for example, be one for native.
// The only think we want to instantiate the application once... And then forget everything

//    /**
//     * @return lazy
//     */
//    static ArtifactImage lazy() {
//        throw new UnsupportedOperationException();
//    }

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
//    static Wirelet oneShot() {
//        throw new UnsupportedOperationException();
//    }
//
//    enum Mode {}
//}