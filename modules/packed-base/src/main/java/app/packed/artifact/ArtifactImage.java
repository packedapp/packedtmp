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

import java.util.concurrent.CompletableFuture;

import app.packed.component.Bundle;
import app.packed.component.Component;
import app.packed.component.ComponentStream;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerBundle;
import app.packed.container.ContainerDescriptor;

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
 * An image can be used to create new instances of {@link app.packed.artifact.App}, {@link ContainerDescriptor} or other
 * artifact images. Artifact images can not be used as a part of other containers, for example, via
 * 
 * 
 * @apiNote In the future, if the Java language permits, {@link ArtifactImage} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 * 
 */
public interface ArtifactImage<A> {

    /**
     * Returns the raw type of what the image creates
     * 
     * @return the raw type
     */
    Class<?> rawType();

    /**
     * Returns the configuration site of this image.
     * 
     * @return the configuration site of this image
     */
    ConfigSite configSite();

    /**
     * Returns a bundle descriptor for this image.
     * 
     * @return the bundle descriptor
     * 
     * @see ContainerDescriptor#of(ContainerBundle)
     */
    // ImageDescriptor with all wirelets????? Eller bare med i BundleDescriptor???
    // Vi har jo feks anderledes contract... Og kan vi se alt???
    // AssemblyDescriptor?

    // Altsaa helt sikker med contracts saa skal det jo vaere whatever der er appliet...
    /// Saa det gaelder jo saadan set ogsaa med #name()
    ContainerDescriptor descriptor();

    /**
     * Returns the name of this artifact.
     * <p>
     * The returned name is always identical to the name of the artifact's root container.
     * <p>
     * If no name is explicitly set when creating the artifact, the runtime will generate a name that guaranteed to be
     * unique among any of the artifact'ssiblings.**@return the name of this artifact
     * 
     * @return the name
     */
    // Only if a name has been explicitly set?
    // Or can we include "FooBar?" Ja det taenker jeg
    String name();

    /**
     * Returns the type of bundle that was used to create this image.
     * <p>
     * An image created from another image, will retain the source type of the source image.
     * 
     * @return the type of bundle that was used to create this image
     */
    Class<? extends Bundle<?>> sourceType();

    /**
     * Returns a component stream consisting of all the components in this image.
     * 
     * @param options
     *            stream options
     * @return the component stream
     * @see Component#stream(app.packed.component.ComponentStream.Option...)
     */
    /// Hmmmm. Altsaa vi skal maaske heller have en descriptor plug
    // descriptor().stream()...
    ComponentStream stream(ComponentStream.Option... options);

    /**
     * Creates a new artifact using this image.
     * 
     * @param wirelets
     *            wirelets used to create the artifact
     * @return the new artifact
     */
    A create(Wirelet... wirelets);

    // Create And start()
    A start(Wirelet... wirelets);

    default CompletableFuture<A> startAsync(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
//Contains

//Ways to initialize, start, stop, execute, ect...
//Ways to query the image... in the same way as a Bundle...
////Tror dog det betyder vi skal have noget a.la. 
//ArtifactImage -> SystemDescribable, saa metoder, f.eks.,
//ServiceContract.from(Image|new XBundle()); -> 
////SystemInspector.find(iOrB, ServiceContract.class); <-- SC exposed as a contract

//Image<App> app = App.newImage(new MyApp());
//Image app = Image.of(new MyApp());

// App.driver().image(Bundle b);

//Man kan ikke lave et Image via Image...
//Fordi det maaske ikke kun er Artifact drivers der kan lave images...
//Kunne sagtens forstille mig en dag
// Nej... syntes ikke alle skal kunne noedvendig bruge den driver...
// Maaske har vi en hemlig driver...
// ArtifactDriver<A> driver();
///**
//* Returns a new artifact image by applying the specified wirelets.
//* 
//* @param wirelets
//*            the wirelets to apply
//* @return the new image
//*/
//// f.eks. applyPartialConfiguration(SomeConf)... Vi aendrer schemaet..
//// withFixedConf(app.threads = 123)... withDefaultConf(app.threads = 123)
//// Vi fejler hvis det ikke kan bruges??? Pure static solution...
//// rename to prefixWith() <--- no validation...
//ArtifactImage with(Wirelet... wirelets);