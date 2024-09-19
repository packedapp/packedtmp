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
package internal.app.packed.component;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import app.packed.build.BuildMirror;
import app.packed.util.Nullable;
import app.packed.util.TreeView;

/**
 *
 */
public abstract class AbstractTreeMirror<T extends BuildMirror, N extends Mirrorable<T>> implements TreeView<T> {

    @Override
    public Stream<T> stream() {
        return null;
    }

    /** The root node */
    final N root;

    /** An optional filter for tree */
    @Nullable
    final Predicate<? super N> filter;

    protected AbstractTreeMirror(N root, Predicate<? super N> filter) {
        this.root = root;
        this.filter = filter;
    }

    /** {@inheritDoc} */
    @Override
    public final T root() {
        return root.mirror();
    }

    @Override
    public int count() {
        return TreeView.super.count();
    }

    @Override
    public Node<T> nodeRoot() {
        return TreeView.super.nodeRoot();
    }

    public static class MirrorNode<T extends BuildMirror, N extends Mirrorable<T>> implements TreeView.Node<T> {

        final N node;

        final AbstractTreeMirror<T, N> tree;

        MirrorNode(AbstractTreeMirror<T, N> tree, N node) {
            this.tree = tree;
            this.node = node;
        }

        /** {@inheritDoc} */
        @Override
        public int count() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isRoot() {
            return tree.root == node;
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<T> iterator() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public T value() {
            return node.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public T root() {
            return tree.root();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<T> stream() {
            return null;
        }
    }
}
