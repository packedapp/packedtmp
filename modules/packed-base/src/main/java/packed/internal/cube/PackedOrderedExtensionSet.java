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
package packed.internal.cube;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.bundle.Extension;

/** Immutable implementation of {@link OrderedExtensionSet}. */
public final class PackedOrderedExtensionSet implements OrderedExtensionSet {

    /** An empty set. */
    public static final PackedOrderedExtensionSet EMPTY = new PackedOrderedExtensionSet(List.of());

    /** The extension types this set contains */
    final List<Class<? extends Extension>> extensions;

    public PackedOrderedExtensionSet(List<Class<? extends Extension>> extensions) {
        this.extensions = requireNonNull(extensions);
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Class<? extends Extension> extensionType) {
        return extensions.contains(extensionType);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof PackedOrderedExtensionSet)) {
            return false; // we should seal the set
        }
        return ((PackedOrderedExtensionSet) obj).extensions.equals(extensions);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return extensions.hashCode();
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

    /**
     * @param extensions
     * @return a new ordered extension set
     * @throws IllegalArgumentException
     *             if trying to add BaseExtension
     */
    @SuppressWarnings("unchecked")
    public static PackedOrderedExtensionSet of(Collection<Class<? extends Extension>> extensions) {
        List<?> l = extensions.stream().map(c -> ExtensionModel.of(c)).sorted().map(m -> m.type()).collect(Collectors.toList());
        return new PackedOrderedExtensionSet((List<Class<? extends Extension>>) l);
    }
}
