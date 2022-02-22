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
package packed.internal.container;

import java.util.Iterator;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionTree;

/**
 *
 */
// Tree, SequencedTree
public record PackedExtensionTree<T extends Extension<?>> (ExtensionSetup extension, Class<T> extensionType) implements ExtensionTree<T> {

    /** {@inheritDoc} */
    @Override
    public Iterator<T> iterator() {
        return new ExtensionSetup.PreOrderIterator<>(extension, e -> (T) extensionType.cast(e));
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
}
