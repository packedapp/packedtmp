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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import app.packed.util.BaseTreeView;
import app.packed.util.Nullable;

/**
 * A node in a tree.
 * <p>
 * Only {@link java.util.LinkedHashMap} you can go from one sibling to another and back again.
 */
public abstract class AbstractTreeNode<T extends AbstractTreeNode<T>> implements BaseTreeView<T> {

    /** The (nullable) first child of the node. */
    @Nullable
    public T treeFirstChild;

    /** The (nullable) last child of the node. */
    @Nullable
    T treeLastChild; // not exposed currently, as there are currently no use cases

    /** The (nullable) sibling of the node. */
    @Nullable
    public T treeNextSibling;

    /** Any parent this node may have. Only the root node does not have a parent. */
    @Nullable
    public final T treeParent;

    @SuppressWarnings("unchecked")
    protected AbstractTreeNode(@Nullable T treeParent) {
        this.treeParent = treeParent;
        if (treeParent != null) {
            // Tree maintenance
            if (treeParent.treeFirstChild == null) {
                treeParent.treeFirstChild = (T) this;
            } else {
                treeParent.treeLastChild.treeNextSibling = (T) this;
            }
            treeParent.treeLastChild = (T) this;
        }
    }

    /** {@return the number of nodes in the tree.} */
    @Override
    public final int count() {
        return Math.toIntExact(stream().count());
    }

    /** {@return the depth of the node, with the root node having depth 0} */
    public final int depth() {
        int depth = 0;
        for (AbstractTreeNode<T> node = this; node.treeParent != null; node = node.treeParent) {
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
    public final <R> Iterator<R> iterator(Function<? super T, ? extends R> mapper) {
        return new MappedPreOrderIterator<T, R>(self(), mapper);
    }

    public final T root() {
        @SuppressWarnings("unchecked")
        T t = (T) this;
        while (t.treeParent != null) {
            t = t.treeParent;
        }
        return t;
    }
    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    /**
     * Returns a lazy stream that includes this node and all its children in pre-order.
     *
     * @return a pre-order stream of the nodes
     */
    @Override
    public final Stream<T> stream() {
        Iterator<T> iterator = new PreOrderIterator<>(self());

        // Convert the iterator to a Spliterator with appropriate characteristics (ORDERED, NONNULL)
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    /** A pre-order iterator for a rooted tree. */
    private static final class MappedPreOrderIterator<T extends AbstractTreeNode<T>, R> implements Iterator<R> {

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

            if (n.treeFirstChild != null) {
                next = n.treeFirstChild;
            } else {
                next = next(n);
            }

            return mapper.apply(n);
        }

        private T next(T current) {
            requireNonNull(current);
            if (current.treeNextSibling != null) {
                return current.treeNextSibling;
            }
            T parent = current.treeParent;
            if (parent == root || parent == null) {
                return null;
            } else {
                return next(parent);
            }
        }
    }

    /** A pre-order iterator for a rooted tree. */
    private static final class PreOrderIterator<T extends AbstractTreeNode<T>> implements Iterator<T> {

        /** The next node to process, or null if no more nodes are left. */
        @Nullable
        private T next;

        /** The root node of the tree being iterated over. */
        private final T root;

        private PreOrderIterator(T root) {
            this.root = this.next = root;
        }

        /**
         * Recursively finds the next sibling or the parent's next sibling if this node has no siblings.
         *
         * @param current
         *            the current node
         * @return the next sibling, or null if there is none
         */
        private T findNextSibling(T current) {
            requireNonNull(current);
            if (current.treeNextSibling != null) {
                return current.treeNextSibling;
            }
            T parent = current.treeParent;
            if (parent == root || parent == null) {
                return null;
            } else {
                return findNextSibling(parent);
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return next != null;
        }

        /** {@inheritDoc} */
        @Override
        public T next() {
            T n = next;
            if (n == null) {
                throw new NoSuchElementException();
            }

            // If there's a first child, go to it
            if (n.treeFirstChild != null) {
                next = n.treeFirstChild;
            } else {
                // Otherwise, find the next sibling or ascend the tree to find the next node
                next = findNextSibling(n);
            }

            return n;
        }
    }
}
