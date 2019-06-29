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
import packed.internal.container.ContainerFactory;

/**
 * A pre-generated image of an artifact.
 * <p>
 * An image can be used to create new instances of {@link App}, {@link Injector}, {@link BundleDescriptor} or other
 * artifact images. It can not be used with {@link ContainerBundle#link(ContainerBundle, Wirelet...)}.
 */
public interface ArtifactImage extends ContainerSource, Artifact {

    /**
     * Returns the type of bundle that was used to create this image.
     * <p>
     * If this image was created from an existing image, the new image image will retain the original image source bundle
     * type.
     * 
     * @return the original source type of this image
     */
    // sourceType?? bundleType
    Class<? extends ContainerBundle> source();

    ArtifactImage with(Wirelet... wirelets);

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
     * Generates a new container image from the specified container source.
     * 
     * @param source
     *            the container source to generate the image from
     * @param wirelets
     *            wirelets
     * @return the generated image
     * @throws RuntimeException
     *             if the image could not be generated for some reason
     */
    static ArtifactImage of(ContainerSource source, Wirelet... wirelets) {
        return ContainerFactory.imageOf(source, wirelets);
    }

    // ofRepeatable();
    // Ideen er vi f.eks. gerne vil instantiere bygge hele containeren. Men f.eks. ikke initializere
    // wirelet.checkNotFrom();
}
