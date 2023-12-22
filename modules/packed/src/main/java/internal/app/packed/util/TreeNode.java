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
package internal.app.packed.util;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import app.packed.util.Nullable;
import internal.app.packed.util.TreeNode.ActualNode;

/**
 * A node in a tree.
 */

class Foo implements ActualNode<Foo> {

    final TreeNode<Foo> node = new TreeNode<>(null, this);

    /** {@inheritDoc} */
    @Override
    public TreeNode<Foo> node() {
        return node;
    }
}

// Det den her kan som LinkedHashMap ikke kan er at gaa fra en sieblig til en anden.
public class TreeNode<T extends ActualNode<T>> {

    /** The (nullable) first child of the node. */
    @Nullable
    private T firstChild;

    /** The (nullable) last child of the node. */
    @Nullable
    private T lastChild; // not exposed currently, as there are currently no use cases

    /** The (nullable) sibling of the node. */
    @Nullable
    private T nextSibling;

    /** Any parent this node may have. Only the root node does not have a parent. */
    @Nullable
    private final T parent;

    final T value;

    public interface ActualNode<T extends ActualNode<T>> {
        TreeNode<T> node();
    }

    public boolean isRoot() {
        return parent == null;
    }

    public T parentOrNull() {
        return parent;
    }

    public TreeNode(@Nullable T treeParent, T treeThis) {
        this.parent = treeParent;
        this.value = treeThis;
        if (treeParent != null) {
            // Tree maintenance
            if (treeParent.node().firstChild == null) {
                treeParent.node().firstChild = treeThis;
            } else {
                treeParent.node().lastChild.node().nextSibling = treeThis;
            }
            treeParent.node().lastChild = treeThis;
        }
    }

    public Iterable<T> children() {
        class TreeNodeIterator implements Iterator<T> {
            private T currentNode;

            TreeNodeIterator() {
                this.currentNode = TreeNode.this.firstChild;
            }

            @Override
            public boolean hasNext() {
                return currentNode != null;
            }

            @Override
            public T next() {
                T temp = currentNode;
                currentNode = currentNode.node().nextSibling;
                return temp;
            }
        }
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return new TreeNodeIterator();
            }
        };
    }

    public final int depth() {
        int depth = 0;
        for (TreeNode<T> node = this; node.parent != null; node = node.parent.node()) {
            depth++;
        }
        return depth;
    }

    /**
     * Returns a pre-order iterator that uses the specified mapper to map each node to a result.
     *
     * @param <R>
     *            the type of result to map the node to
     * @param mapper
     *            the node mapper
     * @return a pre-order iterator
     */
    @SuppressWarnings("unchecked")
    public final <R> Iterator<R> iterator(Function<? super T, ? extends R> mapper) {
        return new MappedPreOrderIterator<T, R>((T) this, mapper);
    }

    public final T root() {
        TreeNode<T> t = this;
        T p = t.parent;
        while (p != null) {
            p = t.parent;
            t = p.node();
        }
        return p;
    }

    /** A pre-order iterator for a rooted tree. */
    static final class ChildIterator<T extends ActualNode<T>> implements Iterator<T> {

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public T next() {
            return null;
        }

    }

    /** A pre-order iterator for a rooted tree. */
    private static final class MappedPreOrderIterator<T extends ActualNode<T>, R> implements Iterator<R> {

        /** A mapper that is applied to each node. */
        private final Function<? super T, ? extends R> mapper;

        /** The next extension, null if there are no next. */
        @Nullable
        private T next;

        /** The root extension. */
        private final T root;

        private MappedPreOrderIterator(T root, Function<? super T, ? extends R> mapper) {
            this.root = this.next = root;
            this.mapper = mapper;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return next != null;
        }

        /** {@inheritDoc} */
        @Override
        public R next() {
            T n = next;
            if (n == null) {
                throw new NoSuchElementException();
            }

            if (n.node().firstChild != null) {
                next = n.node().firstChild;
            } else {
                next = next(n);
            }

            return mapper.apply(n);
        }

        private T next(T current) {
            requireNonNull(current);
            if (current.node().nextSibling != null) {
                return current.node().nextSibling;
            }
            T parent = current.node().parent;
            if (parent == root || parent == null) {
                return null;
            } else {
                return next(parent);
            }
        }
    }
}
