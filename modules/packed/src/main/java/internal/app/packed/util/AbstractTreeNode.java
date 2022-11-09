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

import app.packed.framework.Nullable;

/**
 *
 */
public abstract class AbstractTreeNode<T extends AbstractTreeNode<T>> {

    /** The (nullable) first child of the extension. */
    @Nullable
    public T treeFirstChild;

    /** The (nullable) last child of the extension. */
    @Nullable
    T treeLastChild; // not exposed currently, as there are no use cases

    /** Any parent extension this extension may have. Only the root extension in an application does not have a parent. */
    @Nullable
    public final T treeParent;

    /** The (nullable) siebling of the extension. */
    @Nullable
    public T treeNextSiebling;

    @SuppressWarnings("unchecked")
    protected AbstractTreeNode(@Nullable T treeParent) {
        this.treeParent = treeParent;
        if (treeParent != null) {
            // Tree maintenance
            if (treeParent.treeFirstChild == null) {
                treeParent.treeFirstChild = (T) this;
            } else {
                treeParent.treeLastChild.treeNextSiebling = (T) this;
            }
            treeParent.treeLastChild = (T) this;
        }
    }

    /** A pre-order iterator for a rooted extension tree. */
    public static final class MappedPreOrderIterator<T extends AbstractTreeNode<T>, R> implements Iterator<R> {

        /** A mapper that is applied to each node. */
        private final Function<T, R> mapper;

        /** The next extension, null if there are no next. */
        @Nullable
        private T next;

        /** The root extension. */
        private final T root;

        public MappedPreOrderIterator(T root, Function<T, R> mapper) {
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
            if (current.treeNextSiebling != null) {
                return current.treeNextSiebling;
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
