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

// CurrentState???
// ErrorHandling / Notifications ???
/// Yes, why not use it to log errors...
/**
 * A build context is never available when we build something from an image. Or is it???
 * 
 * <p>
 * A
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
     * Any wirelets used all the way out.
     * 
     * @return a list of wirelets
     */
    default WireletList wirelets() {
        throw new UnsupportedOperationException();
    }

    /// Kan vi bruge dem for example med @UseExtension(onlyAllow=OutputType.INJECTOR) @OnStart
    public enum OutputType {

        /** The output type of the process is an App */
        APP,

        /** */
        IMAGE,

        /** */
        INJECTOR,

        /** The output type is a model */
        MODEL;

        public boolean isInstantiating() {
            return this == APP || this == INJECTOR;
        }
    }
}

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