/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import app.packed.build.Mirror;
import app.packed.extension.Extension;

/**
 * A mirror representing a wirelet that was specified at build-time.
 *
 * @apiNote composite wirelets are never returned as a single mirror, they are always expanded to the individual
 *          wirelets
 */
// What about guarded wirelets or wrapped wirelets??? They kind of change
// The bad thing about calling it a mirror. Is that it is not great for runtime
// Maybe better something a.la. visitor.
public interface WireletMirror extends Mirror {

    /** {@return the extension that defines the wirelet, or empty if defined by the framework} */
    Optional<Class<? extends Extension<?>>> extension();

    /** {@return any wirelet that was specified immediately after this wirelet} */
    // Nah just a list I think...
    Optional<WireletMirror> next();

    /** {@return any wirelet that was specified immediately before this wirelet} */
    Optional<WireletMirror> previous();

    ContainerMirror specificationSite();

    /** {@return the implementation of the wirelet.} */
    Class<? extends Wirelet> wireletClass();
}
