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
 * Represents a rooted tree of 1 or more extension instances.
 */
// Maaske T extends Extension | T extends ExtensionBean... 
// Saa kan vi ogsaa bruge den paa runtime

//// Kan vi lave den som generisk tree???
//// * Maaske vi vil returnere nogle ExtensionConfiguration's

// map(extension->extension.configuration()) 

// TreeView<T>


// Immutable???


// Maybe -> Extension.TreeView Taenker aldrig det er noget man selv laver

public interface ExtensionTree<T extends Extension<T>> extends Iterable<T> {


    /** {@return the number of extensions in the tree.} */
    default int count() {
        int size = 0;
        for (@SuppressWarnings("unused")
        T t : this) {
            size++;
        }
        return size;
    }

    /** {@return the root of the tree.} */
    T root();

    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    static <E extends Extension<E>> ExtensionTree<E> ofSingle(E extension) {
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