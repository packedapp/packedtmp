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
import app.packed.component.ComponentStream;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerBundle;
import app.packed.container.ContainerDescriptor;
import packed.internal.artifact.PackedArtifactImage;

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
 * {@link ContainerBundle#link(ContainerBundle, Wirelet...)}.
 * 
 * @apiNote In the future, if the Java language permits, {@link ArtifactImage} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 * 
 */
public interface ArtifactImage extends ArtifactSource {

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
     * Returns a new artifact image by applying the specified wirelets.
     * 
     * @param wirelets
     *            the wirelets to apply
     * @return the new image
     */
    // f.eks. applyPartialConfiguration(SomeConf)... Vi aendrer schemaet..
    // withFixedConf(app.threads = 123)... withDefaultConf(app.threads = 123)
    // Vi fejler hvis det ikke kan bruges??? Pure static solution...
    ArtifactImage with(Wirelet... wirelets);

    /**
     * Creates a new image from the specified bundle.
     *
     * @param bundle
     *            the bundle to use for image creation
     * @param wirelets
     *            wirelets used for the construction of the image.
     * @return the new image
     * @throws RuntimeException
     *             if the image could not be constructed
     */
    static ArtifactImage of(ContainerBundle bundle, Wirelet... wirelets) {
        return PackedArtifactImage.lazyCreate(bundle, wirelets);
    }
}