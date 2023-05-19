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
import java.util.stream.Stream;

import app.packed.container.ContainerMirror;
import app.packed.container.ContainerTreeMirror;
import app.packed.util.TreeNavigator;

/**
 *
 */
public class PackedContainerTree implements ContainerTreeMirror {

    final Mode mode;

    final ContainerSetup root;

    PackedContainerTree(Mode mode, ContainerSetup root) {
        this.mode = mode;
        this.root = root;
    }

    @Override
    public ContainerMirror root() {
        return root.mirror();
    }

    @Override
    public TreeNavigator<ContainerMirror> rootNode() {
        return ContainerTreeMirror.super.rootNode();
    }

    public class MirrorNode implements TreeNavigator<ContainerMirror> {

        final ContainerSetup container;

        MirrorNode(ContainerSetup container) {
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
            return root == container;
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
            return null;
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
