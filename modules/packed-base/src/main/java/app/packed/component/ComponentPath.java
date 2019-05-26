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
package app.packed.component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;

/**
 * A component path points to a component in a container expressed in a string of characters in which path components,
 * separated by the delimiting character "/", represent each unique component.
 * <p>
 * Implementations of this interface are always immutable and safe for use by multiple concurrent threads.
 * <p>
 * Two components paths are equal if their {@link #length()} is identical and for every valid char index
 * {@link #charAt(int)} returns the same value for both paths. The hash code of a component path is identical to the
 * hash code of its string representation.
 * <p>
 * This interface will be extended in the future with additional methods.
 */
public interface ComponentPath extends Comparable<ComponentPath>, Iterable<Path>, CharSequence {

    /** A component path representing the root of a hierarchy. */
    static final ComponentPath ROOT = new ComponentPath() {

        /** {@inheritDoc} */
        @Override
        public char charAt(int index) {
            return toString().charAt(index);
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(ComponentPath other) {
            return other.isRoot() ? 0 : 1;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ComponentPath && ((ComponentPath) other).isRoot();
        }

        @Override
        public int findDepth() {
            return 0;
        }

        /** {@inheritDoc} */
        @Override
        public ComponentPath parent() {
            return null;
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean isRoot() {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public int length() {
            return 1;
        }

        /** {@inheritDoc} */
        @Override
        public CharSequence subSequence(int start, int end) {
            return toString().subSequence(start, end);
        }

        @Override
        public String toString() {
            return "/";
        }

        @Override
        public Iterator<Path> iterator() {
            return Collections.emptyIterator();
        }
    };

    /**
     * Returns the number of elements in this path. This is not a constant time operation as we might need to traverse
     * through all parents of this component, but it is usually really fast.
     *
     * @return the number of elements in the path, or {@code 0} if this path represents a root component
     */
    int findDepth();

    /**
     * Returns the <em>parent path</em>, or null if this path does not have a parent (is a root).
     *
     * @return a path representing the path's parent
     */
    ComponentPath parent();// Should probably be optional??? Or for performance reasons nullable... hmm

    /**
     * Returns whether or not this component is the root component in a container hierarchy.
     *
     * @return whether or not this component is the root component
     */
    boolean isRoot();

    /**
     * Converts a path string, or a sequence of strings that when joined form a path string, to a {@code ComponentPath}.
     * This method works similar to {@link Paths#get(java.net.URI)}.
     *
     * @param first
     *            the path string or initial part of the component path string
     * @param more
     *            additional strings to be joined to form the component path string
     * @return the resulting {@code ComponentPath}
     * @throws IllegalArgumentException
     *             if the specified path string cannot be converted to a {@code ComponentPath}
     */
    public static ComponentPath of(String first, String... more) {
        throw new UnsupportedOperationException();
    }
}
