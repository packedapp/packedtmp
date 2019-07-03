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
package app.packed.container;

import app.packed.app.App;
import app.packed.inject.Injector;
import packed.internal.container.ContainerSource;
import packed.internal.container.PackedArtifactImage;
import packed.internal.container.PackedContainerConfiguration;

/**
 * Artifact images are immutable ahead-of-time configured {@link Artifact artifacts}. Creating artifacts in Packed is
 * already really fast, and you can easily create one 10 or hundres of microseconds. But by using artificat images you
 * can into hundres or thousounds of nanoseconds.
 * <p>
 * Use cases:
 * 
 * 
 * <p>
 * Limitations:
 * 
 * No structural changes... Only whole artifacts
 * 
 * <p>
 * An image can be used to create new instances of {@link App}, {@link Injector}, {@link BundleDescriptor} or other
 * artifact images. It can not be used with {@link ContainerBundle#link(ContainerBundle, Wirelet...)}.
 */
public interface ArtifactImage extends Artifact, ArtifactSource {

    /**
     * Returns the type of bundle that was used to create this image.
     * <p>
     * If this image was created from an existing image, the new image image will retain the original image source bundle
     * type.
     * 
     * @return the original source type of this image
     */
    // sourceType?? bundleType.. Igen kommer lidt an paa den DynamicContainerSource....
    Class<? extends ContainerBundle> sourceType();

    default ArtifactImage with(Wirelet... wirelets) {
        // BundleDescriptor.of(image.with(ServiceWirelets.provide("fpp")));
        return of(this, wirelets);
    }

    /**
     * Returns a new container image original functionality but re
     * 
     * @param name
     *            the name
     * @return the new container image
     */
    default ArtifactImage withName(String name) {
        return with(Wirelet.name(name));
    }

    /**
     * Creates a new image from the specified source.
     *
     * @param source
     *            the source of the image
     * @param wirelets
     *            any wirelets to use in the construction of the image
     * @return a new image
     * @throws RuntimeException
     *             if the image could not be constructed properly
     */
    static ArtifactImage of(ArtifactSource source, Wirelet... wirelets) {
        if (source instanceof PackedArtifactImage) {
            return ((PackedArtifactImage) source).newImage(wirelets);
        }
        PackedContainerConfiguration c = new PackedContainerConfiguration(ArtifactType.ARTIFACT_IMAGE, ContainerSource.forImage(source), wirelets);
        return new PackedArtifactImage(c.doBuild());
    }

}
// ofRepeatable();
// wirelet.checkNotFrom();
