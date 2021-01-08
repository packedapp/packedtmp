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
package packed.internal.bundle;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import app.packed.container.Extension;
import app.packed.container.ExtensionDescriptor;

/**
 * An set of extensions ordered by {@link ExtensionDescriptor#depth()} and {@link ExtensionDescriptor#fullName()}.
 * 
 * <p>
 * An extension ordering will not accept extension types that identical names but different identities. This can happen
 * fx if they the same extension is loaded with multiple different class loaders.
 * 
 * 
 * @apiNote In the future, if the Java language permits, {@link OrderedExtensionSet} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 */
// SortedExtensionSet
public interface OrderedExtensionSet extends Iterable<Class<? extends Extension>> {

    /**
     * Returns whether or not this ordering contains the specified extension type.
     * 
     * @param extensionType
     *            the extension type to test
     * @return whether or not this ordering contains the specified extension type
     */
    boolean contains(Class<? extends Extension> extensionType);

    /**
     * Returns a stream containing a descriptor of every extension type in this set.
     * 
     * @return a stream containing a descriptor of every extension type in this set
     */
    default Stream<ExtensionDescriptor> descriptors() {
        return stream().map(ExtensionDescriptor::of);
    }

    /**
     * Returns whether or not this ordering contains any extensions.
     * 
     * @return whether or not this ordering contains any extensions
     */
    boolean isEmpty();

    /**
     * Returns the number of extensions in this ordering.
     * 
     * @return the number of extensions in this ordering
     */
    int size();

    /**
     * Returns a stream containing all the extension types in this set.
     * 
     * @return a stream containing all the extension types in this set
     */
    Stream<Class<? extends Extension>> stream();

    /**
     * Returns an ordering that contains no extensions.
     * 
     * @return an ordering that contains no extensions
     */
    static OrderedExtensionSet of() {
        return PackedOrderedExtensionSet.EMPTY;
    }

    /**
     * Returns an ordered extension set containing an arbitrary number of extension types.
     * 
     * @param extensions
     *            the extension types to include in the new set
     * @return a new ordered extension set
     * @throws IllegalArgumentException
     *             if the specified multiple extensions with the same {@link ExtensionDescriptor#fullName()} but from
     *             different class loaders
     */
    @SafeVarargs
    static OrderedExtensionSet of(Class<? extends Extension>... extensions) {
        return of(List.of(extensions));
    }

    /**
     * @param extensions
     *            the extension types to include in the new set
     * @return a new ordered extension set
     * @throws IllegalArgumentException
     *             if the specified multiple extensions with the same {@link ExtensionDescriptor#fullName()} but from
     *             different class loaders
     */
    static OrderedExtensionSet of(Collection<Class<? extends Extension>> extensions) {
        return PackedOrderedExtensionSet.of(extensions);
    }
}
