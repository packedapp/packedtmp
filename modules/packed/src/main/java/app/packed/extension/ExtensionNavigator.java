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
package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.util.AbstractTreeNode;

/**
 * Represents a rooted tree of extensions of the same type with a single extension as the origin.
 * 
 * @param <E>
 *            the type of extensions
 */
public final /* primitive */ class ExtensionNavigator<E extends Extension<E>> implements Iterable<E> {

    /** We save the extension type mainly for casting. */
    private final Class<E> extensionType;
    
    private final ExtensionSetup originExtension;

    ExtensionNavigator(ExtensionSetup originExtension, Class<E> extensionType) {
        this.originExtension = requireNonNull(originExtension);
        this.extensionType = requireNonNull(extensionType);
    }

    /** {@return the number of extensions in the tree.} */
    public int count() {
        int size = 0;
        for (@SuppressWarnings("unused")
        E t : this) {
            size++;
        }
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ExtensionNavigator<?> en && (originExtension == en.originExtension);
    }

    ExtensionDescriptor extensionDescriptor() {
        return originExtension.descriptor();
    }

    @Override
    public int hashCode() {
        return originExtension.hashCode();
    }

    /**
     * {@return whether or not this extension has a parent extension.} Only extensions that are used in the root container
     * of an application does not have a parent extension.
     */
    boolean isRoot() {
        return originExtension.treeParent == null;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<E> iterator() {
        return new AbstractTreeNode.MappedPreOrderIterator<>(originExtension, e -> (E) extensionType.cast(e.instance()));
    }

//    /** {@return the root of the tree.} */
//    public root();

    public E origin() {
        return extensionType.cast(originExtension.instance());
    }

    /**
     * @return
     */
    public E root() {
        ExtensionSetup s = originExtension;
        while (s.treeParent != null) {
            s = s.treeParent;
        }
        return extensionType.cast(s.instance());
    }

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
