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
import app.packed.container.ContainerSource;
import app.packed.errorhandling.ErrorMessage;

/**
 * An artifact build context is created every time an build context is create .
 * 
 * A build context is never available when we build something from an image. Or is it???
 */
// Hmm, Naar vi laver et image eller en descriptor, syntes jeg egentlig ikke vi laver en Artifact
// Saa burde vi jo heller ikke have en build context....
public interface ArtifactBuildContext {

    void addError(ErrorMessage message);

    /**
     * Returns the type of artifact that is being built.
     * 
     * @return the type of artifact that is being built
     */
    Class<?> artifactType();

    /**
     * Returns the configuration site that initialized the build process.
     * 
     * @return the configuration site that initialized the build process
     */
    ConfigSite configSite();

    /**
     * Returns whether or not an artifact image is being built.
     * 
     * @return whether or not an artifact image is being built
     */
    default boolean isImage() {
        return artifactType() == ArtifactImage.class;
    }

    /**
     * Returns whether or not we are instantiating an actual artifact. Or if we are just producing an image or a descriptor.
     * 
     * @return whether or not we are instantiating an actual artifact
     */
    boolean isInstantiating();

    /**
     * Returns the source of the top level container.
     * 
     * @return the source of the top level container
     */
    Class<? extends ContainerSource> sourceType();
}

enum Action {
    IMAGE_GENERATE, INTROSPECT, INSTANTIATE_ARTIFACT;
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