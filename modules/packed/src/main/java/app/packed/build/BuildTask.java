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
package app.packed.build;

import app.packed.assembly.Assembly;
import app.packed.extension.Extension;

/**
 *
 */
// Den er jo ikke kun build step. Det er også ejer af ting...
// Maaske har vi ikke author, men build step istedet for...

// Is it ComposerStep????
// For example, delayed Codegen kunne også vaere et build step

// Maybe BuildTask is better? But then again Extensions are 2 tasks really

// ContainerHook er jo en slags BuildStep...
// Extension har sådan set 2 build steps...
public sealed interface BuildTask permits Assembly, Extension {

    /** The state of an {@link Assembly}. */
    public enum State {

        /** The assembly has not yet been used in a build process. */
        BEFORE_BUILD,

        /** The assembly is currently being used in a build process. */
        IN_USE,

        /** The assembly has already been used in a build process (either successfully or unsuccessfully). */
        AFTER_BUILD;
    }
}
