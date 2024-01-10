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

import app.packed.component.Mirror;

/**
 *
 */
// Hvis det er kun er mirrors den bliver brugt paa...
// Saa skal den maaske hedde noget andet
public interface TreeView<N extends Mirror> {

    default boolean contains(N node) {
        throw new UnsupportedOperationException();
    }

    /** {@return the number of nodes in the tree.} */
    default int count() {
        return Math.toIntExact(stream().count());
    }

    default void forEach(Consumer<N> action) {
        throw new UnsupportedOperationException();
    }

    default void forEachNode(Consumer<Node<N>> action) {
        throw new UnsupportedOperationException();
    }

    default void forEachNodeWithDepth(BiConsumer<Integer, Node<N>> action) {
        throw new UnsupportedOperationException();
    }

    default void forEachWithDepth(BiConsumer<Integer, N> action) {
        throw new UnsupportedOperationException();
    }

    default void print(Function<? super N, String> f) {}

    /** {@return the root node in the tree.} */
    default N root() {
        throw new UnsupportedOperationException();
    }

    default Node<N> rootNode() {
        throw new UnsupportedOperationException();
    }

    default Stream<N> stream() {
        throw new UnsupportedOperationException();
    }

    /**
     * A tree plus a node
     */
    // Det den her kan er at bibeholde treet naar man refererer.
    // Fx ContainerMirror.children har jo bare alle boern med ligegyldig om filtrerne
    public interface Node<T> extends Iterable<T> {

        /** {@return the number of nodes in the tree.} */
        int count();

        default int depth() {
            // Tror vi laver en recursive parent counter
            return 0;
        }

        /**
         * {@return whether or not this extension has a parent extension.} Only extensions that are used in the root container
         * of an application does not have a parent extension.
         */
        boolean isRoot();

        default <S> Node<T> map(Function<T, S> mapper) {
            // ContainerSetup -> makeTree -> Root, Predicate
            // containerSetup.map(s->s.mirror())
            throw new UnsupportedOperationException();
        }

        T value();

        /**
         * @return
         */
        T root();

        Stream<T> stream();
    }

///** {@return an unmodifiable view of all of the children of this component.} */
//default Stream<N> children() {
//  // childIterable?
//  // does not work because container().containerChildren may be null
//  throw new UnsupportedOperationException();
//  // return CollectionUtil.unmodifiableView(container().containerChildren, c -> c.mirror());
//}

//default Stream<N> descendents(boolean includeThis) {
//  // Maaske have en TreeSelector
//  // Der er 3 interessant ting taenker jeg.
//  // direct children
//  // direct ancestors
//  // direct ancestors + this
//  throw new UnsupportedOperationException();
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
//    interface Node<N> {
//        N container();
//
//        int depth();
//    }
}