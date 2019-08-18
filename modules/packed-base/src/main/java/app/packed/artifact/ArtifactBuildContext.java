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

import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.WireletList;
import app.packed.container.extension.Extension;

/**
 * An artifact build context is created every time an build context is create . The context is shared among all
 * extension of every container configuration for the artifact via {@link Extension#buildContext()}.
 * 
 * A build context is never available when we build something from an image. Or is it???
 */
// Skal vi overhoved have wirelets med her????

public interface ArtifactBuildContext {

    /**
     * Returns the type of artifact the build process produces.
     * 
     * @return the type of artifact the build process produces
     */
    Class<?> artifactType();

    /**
     * Returns the configuration site that initialized the build process.
     * 
     * @return the configuration site that initialized the build process
     */
    ConfigSite configSite();

    /**
     * Returns whether or not we are instantiating an actual artifact. Or if we are just producing an image or a descriptor.
     * 
     * @return whether or not we are instantiating an actual artifact
     */
    boolean isInstantiating();

    /**
     * Returns the container source of the build, for example a {@link Bundle bundle}.
     * 
     * @return the container source of the build, for example a bundle or a container image
     */
    // Maybe a Class<?> sourceType() instead, eller har vi brug for at kunne access den???
    /// Hmm kan jo starte med sourceType og saa altid tilfoeje source.
    // Der hvor den ikke fungere skide godt er med InjectionConfigurator som jo bare er en Consumer
    ArtifactSource source();

    /**
     * Any wirelets that was used when initializing the build.
     * 
     * @return a list of wirelets
     */
    WireletList wirelets();
}
// We could add ComponentPath path();
//// But it will freeze the name of the top level. Which we don't want.

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