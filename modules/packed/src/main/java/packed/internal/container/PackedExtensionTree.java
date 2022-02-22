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
package packed.internal.container;

import java.util.Iterator;
import java.util.NoSuchElementException;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionTree;

/**
 *
 */
// Tree, SequencedTree
public record PackedExtensionTree<T extends Extension<?>> (ExtensionSetup extension, Class<T> extensionType) implements ExtensionTree<T> {

    /** {@inheritDoc} */
    @Override
    public Iterator<T> iterator() {
        return new Iter2<>(extension);
    }
//
//    private void add(ExtensionSetup es, ContainerSetup container, ArrayList<T> extensions) {
//        extensions.add(extensionType.cast(es.instance()));
//        if (container.containerChildren != null) {
//            System.out.println("====DAV fra " + container.path());
//            for (ContainerSetup c : container.containerChildren) {
//                ExtensionSetup childExtension = c.extensions.get(extensionType);
//                if (childExtension != null) {
//                    add(childExtension, c, extensions);
//                }
//            }
//        }
//    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ExtensionTree<" + extensionType.getSimpleName() + ">";
    }

    // Could wrap a Function<ExtensionSetup,T> or allow both configuration and extension
    // as T
    static final class Iter2<T extends Extension<?>> implements Iterator<T> {

        final ExtensionSetup root;
        ExtensionSetup next;

        Iter2(ExtensionSetup root) {
            this.root = this.next = root;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return next != null;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            ExtensionSetup n = next;
            if (n == null) {
                throw new NoSuchElementException();
            }

            if (n.firstChild != null) {
                next = n.firstChild;
            } else {
                next = findNext(n);
            }

            return (T) n.instance();
        }

        private ExtensionSetup findNext(ExtensionSetup current) {
            if (current.siebling != null) {
                return current.siebling;
            }
            ExtensionSetup parent = current.parent;
            if (parent == root) {
                return null;
            } else {
                return findNext(parent);
            }
        }
    }

    final class Iter implements Iterator<T> {

        ContainerSetup current;

        T next;

        void advanced() {

        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public T next() {
            return next;
        }
    }
}
