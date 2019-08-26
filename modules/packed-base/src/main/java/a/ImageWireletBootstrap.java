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
package a;

import static java.util.Objects.requireNonNull;

import app.packed.container.extension.Extension;
import app.packed.inject.InjectionExtension;

/**
 *
 */

// Der bliver lavet en ny naar man bruger et image..
// Men den kan ogsaa bruges uden...

// Man kan lave saadan en for hver gang, man laver et image, som bruger wirelets
// Altsaa Vi har jo behov for det alligevel

// Hver gang man applier wirelets laver man saadan en...
// Kan ogsaa bare sige man skal have 2 constructors...
// En der tager extensionen og en der tager en previous instans

// For let hedens skyld supportere vi kun vi bootstrap objekter for alle wirelets...
public abstract class ImageWireletBootstrap<T extends Extension> {
    public abstract ImageWireletBootstrap<T> spawnNewImage();
}

class InjectionWireletBootstreapper extends ImageWireletBootstrap<InjectionExtension> {

    final InjectionExtension extension;

    InjectionWireletBootstreapper(InjectionExtension extension) {
        this.extension = requireNonNull(extension);
    }

    private InjectionWireletBootstreapper(InjectionWireletBootstreapper prev) {
        this.extension = prev.extension;
    }

    /** {@inheritDoc} */
    @Override
    public ImageWireletBootstrap<InjectionExtension> spawnNewImage() {
        return new InjectionWireletBootstreapper(this);
    }
}

// class ImageWireletBootstrapX<T extends ExtensionWirelet<E>, E extends Extension> {
//
// }

// However, extensions are not that simple.
// support for GraalVM
// Support for Artifact Image generation.
// Descriptors
// User Configuration
// Config Files
// Lifecycle
// Relation to other extensions.
