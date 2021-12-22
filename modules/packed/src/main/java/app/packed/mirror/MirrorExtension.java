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
package app.packed.mirror;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;

/**
 *
 */
// An extension that allows access to mirrors at runtime
// Primarily for debugging

// DebugExtension???? <--- saa kan vi ogsaa bruge hooks'ene
// Understreger endda maaske lidt mere at det ikke er normalt brug...
// Syntes maaske ikke der er grund til at vi har 2 extensions for det IDK

// ContainerMirror <--- Er en Type Injection Service med MirrorExtension som ejer
// ApplicationMirror
// BeanMirror

// Hvis ParamTraceren ogsaa er en del af MirrorExtensionen...
// Betyder det vi ikke kan bruge den uden for en container...
public class MirrorExtension extends Extension {

    /** The container setup. */
    final ContainerSetup container;

    /**
     * Create a new mirror extension.
     * 
     * @param configuration
     *            an extension configuration object.
     */
    /* package-private */ MirrorExtension(ExtensionConfiguration configuration) {
        this.container = ((ExtensionSetup) configuration).container;
    }
}
