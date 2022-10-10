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

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.ExtensionDescriptor;
import app.packed.container.ExtensionNavigator;

/**
 *
 * @implNote It is important that this class implements equals/hashCode as this is used by equals/hashCode on ExtensionMirror.
 */
// Should take current + root
public record PackedExtensionNavigator<T extends Extension<T>> (ExtensionSetup extension, Class<T> extensionType) implements ExtensionNavigator<T> {

    /** {@inheritDoc} */
    @Override
    public Iterator<T> iterator() {
        return new PreOrderIterator<>(extension, e -> (T) extensionType.cast(e.instance()));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ExtensionTree<" + extensionType.getSimpleName() + ">";
    }

    /** {@inheritDoc} */
    @Override
    public T root() {
        return extensionType.cast(extension.instance());
    }

    /** {@inheritDoc} */
    @Override
    public ExtensionDescriptor extensionDescriptor() {
        return extension.model;
    }
    

    /** A pre-order iterator for a rooted extension tree. */
    static final class PreOrderIterator<T extends Extension<?>> implements Iterator<T> {

        /** A mapper that is applied to each node. */
        private final Function<ExtensionSetup, T> mapper;

        /** The next extension, null if there are no next. */
        @Nullable
        private ExtensionSetup next;

        /** The root extension. */
        private final ExtensionSetup root;

        PreOrderIterator(ExtensionSetup root, Function<ExtensionSetup, T> mapper) {
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
        public T next() {
            ExtensionSetup n = next;
            if (n == null) {
                throw new NoSuchElementException();
            }

            if (n.childFirst != null) {
                next = n.childFirst;
            } else {
                next = next(n);
            }

            return mapper.apply(n);
        }

        private ExtensionSetup next(ExtensionSetup current) {
            requireNonNull(current);
            if (current.childSiebling != null) {
                return current.childSiebling;
            }
            ExtensionSetup parent = current.parent;
            if (parent == root || parent == null) {
                return null;
            } else {
                return next(parent);
            }
        }
    }
}
