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
package internal.app.packed.container;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import app.packed.container.ContainerMirror;
import app.packed.container.ContainerTreeMirror;

/**
 *
 */
public class PackedContainerTree implements ContainerTreeMirror {

    final Predicate<? super ContainerSetup> nodeSelector;

    final ContainerSetup root;

    PackedContainerTree(ContainerSetup root, Predicate<? super ContainerSetup> nodeSelector) {
        this.root = root;
        this.nodeSelector = nodeSelector;
    }

    @Override
    public ContainerMirror root() {
        return root.mirror();
    }

    @Override
    public Node<ContainerMirror> rootNode() {
        return ContainerTreeMirror.super.rootNode();
    }

    public static class MirrorNode implements Node<ContainerMirror> {

        final ContainerSetup container;
        final PackedContainerTree tree;

        MirrorNode(PackedContainerTree tree, ContainerSetup container) {
            this.tree = tree;
            this.container = container;
        }

        /** {@inheritDoc} */
        @Override
        public int count() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isRoot() {
            return tree.root == container;
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<ContainerMirror> iterator() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ContainerMirror origin() {
            return container.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public ContainerMirror root() {
            return tree.root();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<ContainerMirror> stream() {
            return null;
        }
    }

    public enum Mode {
        APPLICATION, ASSEMBLY, DEPLOYMENT;
    }
}
