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

import static java.util.Objects.requireNonNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import app.packed.container.Extension;

/** Immutable implementation of {@link ExtensionDependencySet}. */
final /* primitive */ class ExtensionDependencySet extends AbstractSet<Class<? extends Extension>> {

    /** The extension types this set contains */
    // Need to changes this to a list
    final List<Class<? extends Extension>> extensions;

    private ExtensionDependencySet(List<Class<? extends Extension>> extensions) {
        this.extensions = requireNonNull(extensions);
    }

    /**
     * Returns whether or not this ordering contains the specified extension type.
     * 
     * @param extensionClass
     *            the extension type to test
     * @return whether or not this ordering contains the specified extension type
     */
    public boolean contains(Class<? extends Extension> extensionClass) {
        return extensions.contains(extensionClass);
    }

    @Override
    public boolean contains(Object o) {
        return extensions.contains(o);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ExtensionDependencySet oes && oes.extensions.equals(extensions);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return extensions.hashCode();
    }

    @Override
    public Iterator<Class<? extends Extension>> iterator() {
        return extensions.iterator();
    }

    @Override
    public int size() {
        return extensions.size();
    }

    @Override
    public String toString() {
        return extensions.toString();
    }

    /**
     * @param extensions
     * @return a new ordered extension set
     * @throws IllegalArgumentException
     *             if trying to add BaseExtension
     */
    @SuppressWarnings("unchecked")
    public static ExtensionDependencySet of(Collection<Class<? extends Extension>> extensions) {
        List<?> l = extensions.stream().map(c -> ExtensionModel.of(c)).sorted().map(m -> m.extensionClass()).toList();
        return new ExtensionDependencySet((List<Class<? extends Extension>>) l);
    }
}
