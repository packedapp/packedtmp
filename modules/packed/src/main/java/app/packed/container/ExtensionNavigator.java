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

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a rooted tree of extensions of the same type with a single extension as the origin.
 * 
 * @param <E>
 *            the type of extensions
 */
public interface ExtensionNavigator<E extends Extension<E>> extends Iterable<E> {

    ExtensionDescriptor extensionDescriptor();

    default E origin() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return whether or not this extension has a parent extension.} Only extensions that are used in the root container
     * of an application does not have a parent extension.
     */
    default boolean isRoot() {
        return root() == origin();
    }

    /** {@return the number of extensions in the tree.} */
    default int count() {
        int size = 0;
        for (@SuppressWarnings("unused")
        E t : this) {
            size++;
        }
        return size;
    }

    /** {@return the root of the tree.} */
    E root();

    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    // Is both root, current, and no children
    // mostly for tests
    static <E extends Extension<E>> ExtensionNavigator<E> ofSingle(E extension) {
        // Den her kan godt vaere public
        // Men dem der iterere kan ikke

        // Hmm vi kan jo ikke returnere collection
        throw new UnsupportedOperationException();
    }

    // Node operations
    // boolean isRoot();
    // Tree connectedTree();
    // root
    // parent
    // children
    // sieblings
    // forEachChild
    // int index.... from [0 to size-1] In order of usage????

}
