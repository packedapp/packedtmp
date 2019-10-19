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
package app.packed.errorhandling;

import app.packed.container.Extension;
import app.packed.lang.Nullable;
import packed.internal.container.PackedContainerConfiguration;

/**
 *
 */
// Build time errors, Construction errors, Initialization errors
// Start errors, Runtime errors, Shutdown errors.

// Restart or not... Coupled with LifecycleExtension

// Child error handling hiarchies.

// How does it relate to LoggingExtension, LifecycleExtension, Other extensions

// Taenker sgu den altid er til raadighed

final class ErrorHandlerExtension extends Extension {

    /** The configuration of the container. */
    @Nullable
    PackedContainerConfiguration pcc;

    /** Should never be initialized by users. */
    ErrorHandlerExtension() {}

    void addErrorHandle() {
        // The top one in the container....
    }

    // Er vel kun restartable hvis vi har lifecycle extension installeret.
    // Og vi er deployet som et image...
    // Can only restart images....

    // RestartContext...
    // Data vi kan koere videre med
}
