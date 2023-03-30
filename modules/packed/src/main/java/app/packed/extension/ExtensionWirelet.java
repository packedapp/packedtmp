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
package app.packed.extension;

import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;

/**
 * Extension wirelets are a specific type of wirelets that can only be defined by an extension.
 * <p>
 * At build time use {@link Extension#selectWirelets(Class)}
 * <p>
 * At runtime an extension bean may bind to {@link app.packed.container.WireletSelection} with type of wirelet as a type
 * parameter.
 *
 *
 * @param <E>
 *            The type of the extension this wirelet is a part up
 *
 * @see Extension#selectWirelets(Class)
 *
 */
public non-sealed abstract class ExtensionWirelet<E extends Extension<E>> extends Wirelet {

    // ExtensionModel extension;
    // Then it is only looked up once.
    // Wirelets are transient objects so an extra pointer shouldn't matter
}

class AcmeExtension extends Extension<AcmeExtension> {}

record AcmeExtensionBean(WireletSelection<AcmeWirelet> wirelets) {}

class AcmeWirelet extends ExtensionWirelet<AcmeExtension> {}