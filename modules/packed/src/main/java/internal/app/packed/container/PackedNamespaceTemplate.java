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
package internal.app.packed.container;

import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.namespace.NamespaceMirror;
import app.packed.namespace.NamespaceOperator;
import app.packed.namespace.NamespaceTemplate;

/**
 *
 */
public final class PackedNamespaceTemplate<T extends NamespaceOperator<?>> implements NamespaceTemplate<T> {
    public final Supplier<T> supplier;

    private PackedNamespaceTemplate(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /** {@inheritDoc} */
    @Override
    public <D extends NamespaceMirror<?>> NamespaceTemplate<T> mirrorType(Class<D> mirrorType, Function<? super T, ? extends D> mirrorSuppliers) {
        throw new UnsupportedOperationException();
    }

    public static <T extends NamespaceOperator<?>> PackedNamespaceTemplate<T> of(Supplier<T> supplier) {
        return new PackedNamespaceTemplate<>(supplier);
    }
}
