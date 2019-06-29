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

import app.packed.config.ConfigSite;

/**
 * A build context is create
 * 
 * A build context is never available when we build something from an image. Or is it???
 */
public interface ArtifactBuildContext {

    /**
     * Returns the type of artifact the build process produces.
     * 
     * @return the type of artifact the build process produces
     */
    ArtifactType artifactType();

    /**
     * Returns the configuration site that initialized the build process.
     * 
     * @return the configuration site that initialized the build process
     */
    ConfigSite configSite();

    /**
     * Returns the source of the build, for example a {@link ContainerBundle bundle} or an {@link ArtifactImage image}.
     * 
     * @return the source of the build, for example a bundle or a container image
     */
    ContainerSource source();

    /**
     * Any wirelets that was used when initializing the build.
     * 
     * @return a list of wirelets
     */
    WireletList wirelets();
}
// Specials -> IsFromImage, isNativeImageGenerate, isNativeImageBuild
// source instanceof ContainerImage
// APP
// INJECTOR
// DESCRIPTOR
// NATIVE_IMAGE_GENRATION
// CONTAINER_IMAGE_GENERATION
// NATIVE_IMAGE_GENERATION + CONTAINER_IMAGE_GENERATION

// Image -> App
// Image -> Injector
// Image -> Descriptor

// AnyBundle -> App
// AnyBundle -> Injector
// AnyBundle -> Descriptor
// AnyBundle -> Image

// Graal -> App
// Graal -> Injector
// Graal -> Descriptor
// Graal -> ContainerImage

// Graal + Image -> App
// Graal + Image -> Injector
// Graal + Image -> Descriptor