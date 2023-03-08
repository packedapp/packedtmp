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
package sandbox.extension.domain;

import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.container.DomainMirror;
import internal.app.packed.container.PackedDomainTemplate;

/**
 *
 */

// A default domain is applicationWide...

public interface DomainTemplate<T extends ExtensionDomain<?>> {

    // Taenker maaske man skal kunne foersporge paa det.
    // Give me all domains of typeX
    <D extends DomainMirror<?>> DomainTemplate<T> mirrorType(Class<D> mirrorType, Function<? super T, ? extends D> mirrorSuppliers);

    static <T extends ExtensionDomain<?>> DomainTemplate<T> of(Supplier<T> supplier) {
        return PackedDomainTemplate.of(supplier);
    }
}
