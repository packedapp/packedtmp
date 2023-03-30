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

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import internal.app.packed.container.Mirror;

/**
 * Represents one or more container ordered in a tree with a single node as the root.
 */
// Alternative ContainerMirror.ofTree
public interface ContainerTreeMirror extends Mirror {

    void forEach(BiConsumer<Integer, ContainerMirror> action);

    /** {@return the root container in the tree.} */
    ContainerMirror root();

    /** {@return the number of nodes in the tree. */
    int size();

    /** {@return a stream with all containers in the tree, depth first.} */
    Stream<ContainerMirror> stream();

    interface Node {
        int depth();
        ContainerMirror container();
    }
}
