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
package internal.app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionDescriptor;
import app.packed.util.TreeView;

/**
 * Represents a rooted tree of extensions of the same type with a single node as the origin.
 *
 * @param <E>
 *            the type of extensions
 *
 * @see Extension#applicationNavigator()
 */
public final /* primitive */ class ExtensionTreeViewNode<E extends Extension<E>> implements TreeView.Node<E> {

    /** We use the extension type mainly for casting. */
    private final Class<E> extensionType;

    /** The origin of the navigator. */
    private final ExtensionSetup origin;

  public ExtensionTreeViewNode(ExtensionSetup origin, Class<E> extensionType) {
        this.origin = requireNonNull(origin);
        this.extensionType = requireNonNull(extensionType);
    }

    /** {@return the number of extensions in the tree.} */
    @Override
    @SuppressWarnings("unused")
    public int count() {
        int size = 0;
        for (E t : this) {
            size++;
        }
        return size;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ExtensionTreeViewNode<?> en && (origin == en.origin);
    }

    public ExtensionDescriptor extensionDescriptor() {
        return origin.model;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return origin.hashCode();
    }

    /**
     * {@return whether or not this extension has a parent extension.} Only extensions that are used in the root container
     * of an application does not have a parent extension.
     */
    @Override
    public boolean isRoot() {
        return origin.treeParent == null;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<E> iterator() {
        return origin.iterator(e -> extensionType.cast(e.instance()));
    }

//    /** {@return the root of the tree.} */
//    public root();

    @Override
    public E value() {
        return extensionType.cast(origin.instance());
    }

    /**
     * @return
     */
    @Override
    public E root() {
        return extensionType.cast(origin.root().instance());
    }

    @Override
    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ExtensionTree<" + extensionType.getSimpleName() + ">";
    }

//    // Is both root, current, and no children
//    // mostly for tests
//    static <E extends Extension<E>> ExtensionNavigator<E> ofSingle(E extension) {
//        // Den her kan godt vaere public
//        // Men dem der iterere kan ikke
//
//        // Hmm vi kan jo ikke returnere collection
//        throw new UnsupportedOperationException();
//    }

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
