/*
 * Copyright (c) 2026 Kasper Nielsen.
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
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import app.packed.util.TreeView;

/** Implementation of {@link TreeView}. */
public final class PackedTreeView<N extends AbstractTreeNode<N>, T> implements TreeView<T> {

    /** An optional filter for tree */
    @Nullable
    private final Predicate<? super N> filter;

    private final Function<? super N, ? extends T> mapper;

    /** The root node */
    private final N root;

    public PackedTreeView(N root, @Nullable Predicate<? super N> filter, Function<? super N, ? extends T> mapper) {
        this.root = requireNonNull(root);
        this.filter = filter;
        this.mapper = requireNonNull(mapper);
    }

    @Override
    public int count() {
        return Math.toIntExact(stream().count());
    }

    /** {@inheritDoc} */
    @Override
    public <S> TreeView<S> map(Function<? super T, ? extends S> mapper) {
        return new PackedTreeView<>(root, filter, this.mapper.andThen(mapper));
    }

    /** {@inheritDoc} */
    @Override
    public T root() {
        return mapper.apply(root);
    }

    @Override
    public Node<T> rootNode() {
        return toNode(root);
    }

    @Override
    public Stream<T> stream() {
        return stream0().map(mapper);
    }

    private Stream<N> stream0() {
        if (filter != null) {
            return root.stream(filter);
        } else {
            return root.stream();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Stream<Node<T>> streamNodes() {
        return stream0().map(e -> (Node<T>) new PackedTreeViewNode<>(PackedTreeView.this, e));
    }

    public PackedTreeViewNode<N, T> toNode(N node) {
        return new PackedTreeViewNode<>(this, node);
    }

    public static class PackedTreeViewNode<N extends AbstractTreeNode<N>, T> implements TreeView.Node<T> {

        final N node;

        final PackedTreeView<N, T> tree;

        PackedTreeViewNode(PackedTreeView<N, T> tree, N node) {
            this.tree = tree;
            this.node = node;
        }

        /** {@inheritDoc} */
        @Override
        public TreeView<T> asRoot() {
            return new PackedTreeView<N, T>(node, tree.filter, tree.mapper);
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            if (node.treeParent == null) {
                return 0;
            }
            return node.treeParent.depth() + 1;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isRoot() {
            return tree.root == node;
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<T> iterator() {
            return stream().toList().iterator();
        }

        /** {@inheritDoc} */
        @Override
        public T root() {
            return tree.root();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<T> stream() {
            return node.stream().map(tree.mapper);
        }

        /** {@inheritDoc} */
        @Override
        public T value() {
            return tree.mapper.apply(node);
        }

        @Override
        public String toString() {
            return Objects.toString(value());
        }
    }
}
