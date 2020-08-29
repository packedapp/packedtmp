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

import app.packed.component.Bundle;
import app.packed.component.Component;
import app.packed.component.ComponentModifier;
import app.packed.component.Wirelet;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.lifecycle.phases.ConstructionContext;
import packed.internal.lifecycle.phases.PackedAssemblyContext;

/** The default implementation of {@link Image}. */
// Taenker vi maaske skal flytte den internt?
// Altsaa ved ikke lige hvordan det hosted image kommer til at fungere...
final class PackedImage<A> implements Image<A> {
    private static final int IMAGE = PackedComponentModifierSet.setProperty(0, ComponentModifier.IMAGE);

    /** The driver used to create the any shell. */
    // We should use the driver when creating the actual root node...
    // I'm not sure we should need to store it.
    private final ShellDriver<A> driver;

    /** The configuration of the root container. */
    private final ComponentNodeConfiguration node;

    /**
     * Creates a new image from the specified configuration and wirelets.
     * 
     * @param driver
     *            the artifact driver
     * @param bundle
     *            the artifact source
     * @param wirelets
     *            any wirelet
     */
    PackedImage(ShellDriver<A> driver, Bundle<?> bundle, Wirelet... wirelets) {
        this.node = PackedAssemblyContext.assemble(bundle, IMAGE, driver, wirelets);
        this.driver = driver;
    }

    /** {@inheritDoc} */
    @Override
    public Component component() {
        return node.adaptToComponent();
    }

    /** {@inheritDoc} */
    @Override
    public A initialize(Wirelet... wirelets) {
        ShellContext context = ConstructionContext.constructArtifact(node, WireletPack.forImage(node, wirelets));
        return driver.newArtifact(context);
    }

    /** {@inheritDoc} */
    @Override
    public A start(Wirelet... wirelets) {
        return initialize(wirelets);
    }
}
//
///** {@inheritDoc} */
//@Override
//public Class<?> rawShellType() {
//  return driver.rawType();
//}
//
///** {@inheritDoc} */
//@Override
//public ContainerDescriptor descriptor() {
//  @SuppressWarnings({ "unchecked", "rawtypes" })
//  ContainerDescriptor.Builder builder = new ContainerDescriptor.Builder((Class) Bundle.class);
//  node.container().buildDescriptor(builder);
//  return builder.build();
//}
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
