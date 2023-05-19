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

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import app.packed.util.TreeNavigator;

/**
 *
 */
// Maaske er det ikke et mirror... Men et tree view
public interface TreeMirror<N> {

    /** {@return the number of nodes in the tree.} */
    int count();

    void forEach(BiConsumer<Integer, N> action);

    /** {@return the root node in the tree.} */
    N root();

    TreeNavigator<N> rootNode();

    Stream<N> stream();


//    interface Node<N> {
//        N container();
//
//        int depth();
//    }
}
