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

import app.packed.application.ApplicationPhase;
import app.packed.component.Mirror;
import app.packed.extension.Extension;

/**
 * A mirror representing a wirelet specified at build- or runtime.
 *
 * <p>
 *
 * @apiNote composite wirelets are never returned as a single mirror, but always expanded
 */
// What about guarded wirelets or wrapped wirelets??? They kind of change
public interface WireletMirror extends Mirror {

    // If this extension an extension wirelet. The extension the wirelet is a part of
    Optional<Class<? extends Extension<?>>> extension();

    /** {@return any wirelet that was specified immediately after this wirelet} */
    Optional<WireletMirror> next();

    /** {@return any wirelet that was specified immediately before this wirelet} */
    Optional<WireletMirror> previous();

    ContainerMirror specificationSite();

    ApplicationPhase specifiedAt();

    /** {@return the implementation of the wirelet.} */
    Class<? extends Wirelet> wireletClass();
}
