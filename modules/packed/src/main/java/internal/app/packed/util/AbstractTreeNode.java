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

/**
 * A node in a tree.
 */
public abstract class AbstractTreeNode<T extends AbstractTreeNode<T>> {

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
    @SuppressWarnings("unchecked")
    public final <R> Iterator<R> iterator(Function<? super T, ? extends R> mapper) {
        return new MappedPreOrderIterator<T, R>((T) this, mapper);
    }

    public final T root() {
        @SuppressWarnings("unchecked")
        T t = (T) this;
        while (t.treeParent != null) {
            t = t.treeParent;
        }
        return t;
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
}
