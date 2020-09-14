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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import packed.internal.container.ExtensionModel;

/**
 *
 */

//Class vs Descriptor
// toClassList();, toDescriptorList();c
// Graph<ExtensionDescriptor, Void>
// Does ED only contain actual used dependencies????
// IDK... Maybe we only have classes???
// Problem is we need to retain info about inter extension
// relationships at runtime.

// specials --> ExtensionX before ExtensionY
// Altsaa hvad hvis vi explicit laver en order fx via BaeExtension???

/**
 * An immutable ordered set of extensions that are deterministic ordering of a set of extension types.
 * <p>
 * This set never includes BaseExtension.
 * 
 * <p>
 * An extension ordering will not accept extension types that identical names but different identities. This can happen
 * fx if they the same extension is loaded with multiple different class loaders.
 */
// ExtensionSet or ExtensionList
public interface ExtensionSet extends Iterable<Class<? extends Extension>> {

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
    static ExtensionSet empty() {
        return PackedExtensionSet.EMPTY;
    }

    /**
     * Returns an ordered extension set containing an arbitrary number of extension types.
     * 
     * @param extensions
     *            the extension types to include
     * @return a new set
     * @throws IllegalArgumentException
     *             if trying to add BaseExtension
     */
    @SafeVarargs
    static ExtensionSet of(Class<? extends Extension>... extensions) {
        return of(List.of(extensions));
    }

    /**
     * @param extensions
     * @return a new ordered extension set
     * @throws IllegalArgumentException
     *             if trying to add BaseExtension
     */
    @SuppressWarnings("unchecked")
    static ExtensionSet of(Collection<Class<? extends Extension>> extensions) {
        List<?> l = extensions.stream().map(c -> ExtensionModel.of(c)).sorted().map(m -> m.modelType()).collect(Collectors.toList());
        return new PackedExtensionSet((List<Class<? extends Extension>>) l);
    }
}
