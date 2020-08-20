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

import app.packed.attribute.AttributeHolder;
import app.packed.component.Bundle;
import app.packed.component.Component;
import app.packed.component.ComponentStream;
import app.packed.component.Wirelet;

/**
 *
 */
// Contains

// Ways to initialize, start, stop, execute, ect...
// Ways to query the image... in the same way as a Bundle...
//// Tror dog det betyder vi skal have noget a.la. 
// ArtifactImage -> SystemDescribable, saa metoder, f.eks.,
// ServiceContract.from(Image|new XBundle()); -> 
//// SystemInspector.find(iOrB, ServiceContract.class); <-- SC exposed as a contract

// Image<App> app = App.newImage(new MyApp());
// Image app = Image.of(new MyApp());

// App.driver().image(Bundle b);

// Man kan ikke lave et Image via Image...
// Fordi det maaske ikke kun er Artifact drivers der kan lave images...
// Kunne sagtens forstille mig en dag
public interface TakeImage<T> extends AttributeHolder {

    /**
     * Returns the raw type of what the image creates
     * 
     * @return the raw type
     */
    Class<?> rawType();

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
     * 
     * @return the type of bundle that was used to create this image
     */
    // Hvorfor egentlig ligger sig fast paa det kun er en bundle der har kunne lave et image???
    // Hvad hvis vi opfinder noget nyt...
    Class<? extends Bundle<?>> bundleType();

    /**
     * Creates a new artifact using this image.
     * 
     * @param wirelets
     *            wirelets used to create the artifact
     * @return the new artifact
     */
    T create(Wirelet... wirelets);

    // Create And start()
    T start(Wirelet... wirelets);

    CompletableFuture<T> startAsync(Wirelet... wirelets);

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
}
// Problemet med Image er guest images..
// Og om en envelope for en artifact.

// isGuest -> Whether or not the new artifact will become a guest of the 
// any system the image is referenced at 