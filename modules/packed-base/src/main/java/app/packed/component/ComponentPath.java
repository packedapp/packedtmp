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

import java.nio.file.Paths;

import app.packed.lang.Nullable;
import packed.internal.component.PackedComponentPath;

/**
 * A component path points to a single component in a hierarchy of components (component system) expressed as a string
 * of characters in which path parts, separated by the delimiting character "/", represent each unique component.
 * <p>
 * Implementations of this interface are, unless otherwise specified, immutable and safe for use by multiple concurrent
 * threads.
 * <p>
 * Two component paths are equal if their string representation are identical. The hash code of a component path is
 * identical to the hash code of its string representation.
 * <p>
 * This interface will be extended with additional methods in the future.
 */
// Iteralble Path??? Hmm, er det fulde paths eller del paths??? Den er lidt forvirrende
public interface ComponentPath extends Comparable<ComponentPath>, /* , Iterable<ComponentPath>, */ CharSequence {

    /** A component path representing the root of a hierarchy. */
    static final ComponentPath ROOT = PackedComponentPath.ROOT;

    /**
     * Returns the number of elements in this path.
     *
     * @return the number of elements in the path, or {@code 0} if this path represents a root component
     */
    int depth();

    /**
     * Returns whether or not this component is the root component in a container hierarchy.
     *
     * @return whether or not this component is the root component
     */
    boolean isRoot();

    /**
     * Returns the <em>parent path</em>, or null if this path does not have a parent (is a root).
     *
     * @return a path representing the path's parent
     */
    @Nullable
    ComponentPath parent();// Should probably be optional??? Or for performance reasons nullable... hmm

    /**
     * Returns the string representation of this component path.
     * <p>
     * The returned path string uses the {@code '/'} character to separate names in the path.
     *
     * @return the string representation of this component path
     */
    @Override
    String toString();

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

    // TODO hashCode contract
}
