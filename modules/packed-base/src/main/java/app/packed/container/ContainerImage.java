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

import packed.internal.container.ContainerFactory;

/**
 *
 */
public interface ContainerImage extends ContainerSource {

    /**
     * Returns the name of the container that this image creates.
     * 
     * @return the name of the container that this image creates
     */
    String name();

    /**
     * Returns the type of bundle that used to create this image.
     * <p>
     * If this image was created from an existing image, the new image image will retain the original image source bundle
     * type.
     * 
     * @return the original source type of this image
     */
    // sourceType?? bundleType
    Class<? extends AnyBundle> source();

    ContainerImage with(Wirelet... wirelets);

    /**
     * Returns a new container image retaining original functionality but re
     * 
     * @param name
     *            the name
     * @return the new container image
     */
    default ContainerImage withName(String name) {
        return with(Wirelet.name(name));
    }

    /**
     * Creates a new container image from the specified source.
     * 
     * @param source
     *            the source to create an image from
     * @return the new image
     * @throws RuntimeException
     *             if the image could not be corrected
     */
    static ContainerImage of(ContainerSource source, Wirelet... wirelets) {
        return ContainerFactory.imageOf(source, wirelets);
    }
}
