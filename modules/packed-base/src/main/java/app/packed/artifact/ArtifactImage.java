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

import java.util.Optional;

import app.packed.analysis.BundleDescriptor;
import app.packed.component.Component;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
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
 * An image can be used to create new instances of {@link app.packed.artifact.App}, {@link BundleDescriptor} or other
 * artifact images. It can not be used with {@link Bundle#link(Bundle, Wirelet...)}.
 * 
 * @apiNote In the future, if the Java language permits, {@link ArtifactImage} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 * 
 */
public interface ArtifactImage extends Assembly {

    /**
     * Returns the configuration site of this image.
     * 
     * @return the configuration site of this image
     */
    ConfigSite configSite();

    /**
     * Returns any description that have been set for the image.
     * <p>
     * The returned description is always identical to the description of the root container.
     * 
     * @return any description that has been set for the image
     * @see ContainerConfiguration#setDescription(String)
     * @see Bundle#setDescription(String)
     */
    Optional<String> description();

    /**
     * Returns a bundle descriptor for this image.
     * 
     * @return the bundle descriptor
     * 
     * @see BundleDescriptor#of(Bundle)
     */
    BundleDescriptor descriptor();

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
    // Or can we include "FooBar?"
    String name();

    /**
     * Returns the type of bundle that was used to create this image.
     * <p>
     * Images created from an existing image, will retain the source type of the existing image.
     * 
     * @return the type of bundle that was used to create this image
     */
    Class<? extends Bundle> sourceType();

    /**
     * Returns a component stream consisting of all the components in this image.
     * 
     * @param options
     *            stream options
     * @return the component stream
     * @see Component#stream(app.packed.component.ComponentStream.Option...)
     */
    ComponentStream stream(ComponentStream.Option... options);

    /**
     * Returns a new artifact image by applying the specified wirelets.
     * <p>
     * The specified wirelets are never evaluated until the new image is used to create a new artifact or bundle descriptor.
     * 
     * @param wirelets
     *            the wirelets to apply
     * @return the new image
     */
    ArtifactImage with(Wirelet... wirelets);

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
    static ArtifactImage build(Assembly source, Wirelet... wirelets) {
        return PackedArtifactImage.of(source, wirelets);
    }
}