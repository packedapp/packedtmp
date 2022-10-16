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
package app.packed.container;

import java.util.Iterator;

import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.util.AbstractTreeNode;

/**
 *
 * @implNote It is important that this class implements equals/hashCode as this is used by equals/hashCode on
 *           ExtensionMirror.
 */
// Should take current + root
// Maybe make into a concrete class. instead of an interface.
// Har svaert ved at se vi har flere implementationer.
// Med mindre vi har noget slice and dice
record ExtensionNavigatorImpl<T extends Extension<T>> (ExtensionSetup extension, Class<T> extensionType) implements ExtensionNavigator<T> {

    /** {@inheritDoc} */
    @Override
    public Iterator<T> iterator() {
        return new AbstractTreeNode.MappedPreOrderIterator<>(extension, e -> (T) extensionType.cast(e.instance()));
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
}
