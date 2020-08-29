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
package packed.internal.lifecycle.phases;

import packed.internal.errorhandling.ErrorMessage;

/**
 * An assembly context is created every time an build context is create .
 * 
 * A build context is never available when we build something from an image. Or is it???
 */

// Can faas fra extension context, Component Context.
// Men f.eks. addError() bliver noedt til at smide ISE hvis folk kalder metoder
// Efter assembly is done

// Nu naar vi kompilere en artifact med ind... kan vi jo ret set have en Artifact med
// Det eneste problem er hvis vi har host/guest... Saa er PodContext jo altsaa lidt bedre til at have det.

// addSuccessProcess(Runnable r);
// addFailureProcess(Runnable r);
// addCompletionProcesser(Consumer<@Nullable Throwable> d);

// Boer vaere i component ikke artifact...

// Kan ikke sige
public interface AssemblyContext {

    default boolean isActive() {
        return true;
    }

    void addError(ErrorMessage message);

    /**
     * Returns whether or not we are instantiating an actual artifact. Or if we are just producing an image or a descriptor.
     *
     * @return whether or not we are instantiating an actual artifact
     */
    boolean isInstantiating();

    /**
     * Returns whether or not the assembly is generating an image.
     * 
     * @return whether or not the assembly is generating an image
     */
    default boolean isGeneratingImage() {
        return false;
    }

    /**
     * The action is mainly used.
     * 
     * For example, for image to clean up ressources that are not after stuff has been resolved...
     * 
     * Introspect, Stuff that is not needed if we know we are never going to instantiate anything... (for example, method
     * handles)
     */
    public enum Mode {

        IMAGE_GENERATION,

        /** Performs an introspection of some kind. */
        INTROSPECT,

        /** Instantiates a new artifact. */
        INSTANTIATE;
    }
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