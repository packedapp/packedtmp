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
package app.packed.namespace;

import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.namespace.sandbox.BuildPermission;
import internal.app.packed.container.PackedNamespaceTemplate;

/**
 * <p>
 * Unless otherwise specified the scope
 */

// A default domain is applicationWide...

public interface NamespaceTemplate<T extends NamespaceTwin<?, ?>> {

    // Taenker maaske man skal kunne foersporge paa det.
    // Give me all domains of typeX

    // Magic Initializer for Extension + Authority

    // Igen man skal kunne iterere over dem
    // Or directly on the operator...
    // NamespaceConfiguration<?> Extension.newNamespace(Operator a, Class<? extends NamespaceConfiguration<?>> c, Authority
    // a)
    void addConfigure(Function<Object, NamespaceConfiguration<?>> a);

    <N extends NamespaceMirror<?>> NamespaceTemplate<T> mirrorType(Class<N> mirrorType, Function<? super T, ? extends N> mirrorSuppliers);

    @SuppressWarnings("exports")
    default void addPermission(BuildPermission permissions) {
        // Default values??? for example, root only
    }

    static <T extends NamespaceTwin<?, ?>> NamespaceTemplate<T> of(Supplier<T> supplier) {
        return PackedNamespaceTemplate.of(supplier);
    }
}
