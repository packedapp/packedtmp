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

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 */
public final class PackedExtensionOrdering implements ExtensionOrdering {

    static final PackedExtensionOrdering EMPTY = new PackedExtensionOrdering(List.of());

    private final List<Class<? extends Extension>> extensions;

    PackedExtensionOrdering(List<Class<? extends Extension>> extensions) {
        this.extensions = requireNonNull(extensions);
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Class<? extends Extension> extensionType) {
        return extensions.contains(extensionType);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return extensions.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Class<? extends Extension>> iterator() {
        return extensions.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return extensions.size();
    }

    /** {@inheritDoc} */
    @Override
    public Stream<Class<? extends Extension>> stream() {
        return extensions.stream();
    }

    @Override
    public String toString() {
        return extensions.toString();
    }
}
