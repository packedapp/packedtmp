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

import java.util.Optional;

import app.packed.util.Nullable;
import packed.internal.container.DefaultContainerImage;

/**
 *
 */
// Maybe an interface???
public final class BuildTarget {

    @Nullable
    private final DefaultContainerImage image;

    /**
     * Creates a new build target.
     * 
     * @param image
     */
    BuildTarget(@Nullable DefaultContainerImage image) {
        this.image = image;
    }

    /** Any image that is used in building this entity. */
    public Optional<ContainerImage> image() {
        return Optional.ofNullable(image);
    }

    /** Any image that is used in building this entity. */
    public ContainerSource source() {
        throw new UnsupportedOperationException();
    }

    public Type target() {
        throw new UnsupportedOperationException();
    }

    public boolean registerForNative() {
        return true;
    }

    public enum Type {
        APP, MODEL, INJECTOR, IMAGE;
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
