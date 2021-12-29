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
import app.packed.extension.ExtensionSelection;

/**
 *
 */
public record PackedExtensionSelection<T extends Extension<T>> (ContainerSetup container, Class<? extends Extension<?>> extensionType)
        implements ExtensionSelection<T> {

    /** {@inheritDoc} */
    @Override
    public Iterator<T> iterator() {
        ArrayList<T> list = new ArrayList<>();
        add(container, list);
        return list.iterator();
    }

    @SuppressWarnings("unchecked")
    private void add(ContainerSetup container, ArrayList<T> extensions) {
        if (container.containerChildren != null) {
            for (ContainerSetup c : container.containerChildren) {
                ExtensionSetup es = c.extensions.get(extensionType);
                extensions.add((T) es.instance());
                add(c, extensions);
            }
        }
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
