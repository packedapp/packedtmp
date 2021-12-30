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

import java.util.ArrayList;
import java.util.Iterator;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionTree;

/**
 *
 */
public record PackedExtensionSelection<T extends Extension<?>> (ExtensionSetup extension, Class<T> extensionType) implements ExtensionTree<T> {

    /** {@inheritDoc} */
    @Override
    public Iterator<T> iterator() {
        ArrayList<T> list = new ArrayList<>();
        add(extension, extension.container, list);
        return list.iterator();
    }

    private void add(ExtensionSetup es, ContainerSetup container, ArrayList<T> extensions) {
        extensions.add(extensionType.cast(es.instance()));
        if (container.containerChildren != null) {
            System.out.println("====DAV fra " + container.path());
            for (ContainerSetup c : container.containerChildren) {
                ExtensionSetup childExtension = c.extensions.get(extensionType);
                if (childExtension != null) {
                    add(childExtension, c, extensions);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return null;
    }

    final class Iter implements Iterator<T> {

        T next;

        ContainerSetup current;

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public T next() {
            return next;
        }

        void advanced() {

        }
    }
}
