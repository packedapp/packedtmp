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

import java.nio.file.Path;
import java.util.Iterator;

import app.packed.component.ComponentPath;

/** The default implementation of {@link ComponentPath}. */
final class InternalComponentPath implements ComponentPath {

    /** The lazily initialized path string. */
    private String cached = null;

    /** The component that this path point at. */
    private final InternalComponent component;

    /**
     * Creates a new path for the specified component
     *
     * @param component
     *            the component to create the path for
     */
    InternalComponentPath(InternalComponent component) {
        this.component = requireNonNull(component);
    }

    /** {@inheritDoc} */
    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(ComponentPath other) {
        return toString().compareTo(other.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ComponentPath && toString().equals(((ComponentPath) other).toString());
    }

    /** {@inheritDoc} */
    @Override
    public int findDepth() {
        int depth = 0;
        InternalComponent c = component;
        while (c.parent != null) {
            depth++;
            c = c.parent;
        }
        return depth;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean isRoot() {
        return component.parent == null;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Path> iterator() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public int length() {
        return toString().length();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath parent() {
        InternalComponent parent = component.parent;
        return parent == null ? null : parent.path();
    }

    /** {@inheritDoc} */
    @Override
    public CharSequence subSequence(int beginIndex, int endIndex) {
        return toString().subSequence(beginIndex, endIndex);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String cached = this.cached;
        if (cached != null) {
            return cached;
        }
        if (component.parent == null) {
            cached = "/";
        } else {
            StringBuilder sb = new StringBuilder();
            toString(component, sb);
            cached = sb.toString();
        }
        return this.cached = cached;
    }

    /**
     * Used for recursively constructing the path string.
     *
     * @param component
     *            the component to add
     * @param sb
     *            the string builder to add to
     */
    private static void toString(InternalComponent component, StringBuilder sb) {
        if (component.parent != null) {
            toString(component.parent, sb);
            sb.append("/");
            sb.append(component.name());
        }
    }
}
