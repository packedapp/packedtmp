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
import app.packed.config.ConfigSite;
import app.packed.inject.Injector;

// CurrentState???
// ErrorHandling / Notifications ???
// hasErrors()...
//// Maybe we want to log the actual extension as well.
// so extension.log("fooo") instead
/// Yes, why not use it to log errors...
/**
 * A build context is create
 * 
 * A build context is never available when we build something from an image. Or is it???
 * 
 */
public interface BuildContext {

    /**
     * Returns the configuration site that initialized the build process.
     * 
     * @return the configuration site that initialized the build process
     */
    ConfigSite configSite();

    /**
     * Returns the output type of the build process.
     * 
     * @return the output type of the build process
     */
    OutputType outputType();

    /**
     * Returns the source of the build, for example a bundle or a container image.
     * 
     * @return the source of the build, for example a bundle or a container image
     */
    ContainerSource source();

    /**
     * Any wirelets used when initializing the build.
     * 
     * @return a list of wirelets
     */
    WireletList wirelets();

    /// Kan vi bruge dem for example med @UseExtension(onlyAllow=OutputType.INJECTOR) @OnStart
    /** The output type of a build process. */
    public enum OutputType {

        /**
         * The output type is an analyze. This is typically via {@link BundleDescriptor#of(Bundle)} or when analyzing an
         * application for graal.
         */
        ANALYZE,

        /**
         * The output type of the process is an {@link App}. This is typically either via
         * {@link App#of(ContainerSource, Wirelet...)} or {@link App#run(ContainerSource, Wirelet...)}.
         */
        APP,

        /**
         * The output type of the build process is a {@link ContainerImage}. This is typically via
         * {@link ContainerImage#of(ContainerSource, Wirelet...)}.
         */
        CONTAINER_IMAGE,

        /**
         * The output type of the process is an {@link App}. This is typically either via
         * {@link Injector#of(ContainerSource, Wirelet...)} or {@link Injector#of(java.util.function.Consumer, Wirelet...)}.
         */
        INJECTOR;

        /**
         * Returns whether or not output will result in any kind of instantiation.
         * 
         * @return whether or not output will result in any kind of instantiation
         */
        public boolean isInstantiating() {
            return this == APP || this == INJECTOR;
        }
    }
}
// Specials -> IsFromImage, isNativeImageGenerate, isNativeImageBuild

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