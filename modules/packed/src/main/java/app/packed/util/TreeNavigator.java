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

import java.util.stream.Stream;

/**
 * A tree plus a node
 */
// TreeNode??
public interface TreeNavigator<T> extends Iterable<T> {

    /** {@return the number of nodes in the tree.} */
    int count();

    /**
     * {@return whether or not this extension has a parent extension.} Only extensions that are used in the root container
     * of an application does not have a parent extension.
     */
    boolean isRoot();

//    /** {@return the root of the tree.} */
//    public root();

    T origin();

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