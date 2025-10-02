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
package app.packed.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 */
public interface TreeView<N> {

    default boolean contains(N node) {
        throw new UnsupportedOperationException();
    }

    /** {@return the number of nodes in the tree.} */
    default int count() {
        return Math.toIntExact(stream().count());
    }

    default void forEach(Consumer<N> action) {
        stream().forEach(action);
    }

    default void forEachWithDepth(BiConsumer<Integer, N> action) {
        throw new UnsupportedOperationException();
    }

    <S> TreeView<S> map(Function<? super N, ? extends S> mapper);

    default void nodeForEach(Consumer<Node<N>> action) {
        throw new UnsupportedOperationException();
    }

    default void nodeForEachWithDepth(BiConsumer<Integer, Node<N>> action) {
        throw new UnsupportedOperationException();
    }

    default void print() {
        streamNodes().forEach(n -> {
            IO.println("  ".repeat(n.depth()) + "  " + n);
        });
    }

    /** {@return the root node in the tree.} */
    N root();

    // Maybe just nodeRoot
    Node<N> rootNode();

    Stream<N> stream();

    Stream<Node<N>> streamNodes();

    public interface Node<N> extends Iterable<N> {

        /** Creates a new tree view with this node as the root. */
        TreeView<N> asRoot();

        /** {@return the depth of the node in its tree, with the root having depth 0} */
        int depth();

        /**
         * {@return whether or not this extension has a parent extension.} Only extensions that are used in the root container
         * of an application does not have a parent extension.
         */
        boolean isRoot();

        /**
         * @return
         */
        N root();

        Stream<N> stream();

        N value();
    }

}

///** {@return an unmodifiable view of all of the children of this component.} */
//default Stream<N> children() {
//// childIterable?
//// does not work because container().containerChildren may be null
//throw new UnsupportedOperationException();
//// return CollectionUtil.unmodifiableView(container().containerChildren, c -> c.mirror());
//}

//default Stream<N> descendents(boolean includeThis) {
//// Maaske have en TreeSelector
//// Der er 3 interessant ting taenker jeg.
//// direct children
//// direct ancestors
//// direct ancestors + this
//throw new UnsupportedOperationException();
//}

//Maaske har vi en hjaelper klasse.
//Der kan tage et TreeMirror og saa expande det.
//Eller ogsaa har vi en TreeMirror<X> crossApp() metoder

//Cross Application.. Men

//Problemet omkring root
////

//ContainerLifetime
//Container
//Extension???

//Assembly
//Application
//  interface Node<N> {
//      N container();
//
//      int depth();
//  }
