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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Stream;

import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import packed.internal.container.ContainerSetup;

/** The default implementation of {@link NamespacePath}. */
public final class PackedTreePath implements NamespacePath {

    /** A component path representing the root of a hierarchy. */
    public static final NamespacePath ROOT = new PackedTreePath();

    private final String[] elements;

    /** The hash of this path, lazily calculated. */
    private int hash;

    /** String representation, created lazily */
    private volatile String string;

    PackedTreePath(String... elements) {
        this.elements = requireNonNull(elements);
    }

    /** {@inheritDoc} */
    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(NamespacePath o) {
        return toString().compareTo(o.toString());
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        return elements.length;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PackedTreePath pcp) {
            if (pcp.elements.length != elements.length) {
                return false;
            }
            for (int i = 0; i < elements.length; i++) {
                if (!pcp.elements[i].equals(elements[i])) {
                    return false;
                }
            }
            return true;
        } else if (obj instanceof NamespacePath pcp) {
            if (pcp.depth() != elements.length) {
                return false;
            }
            if (elements.length == 0) {
                return true;
            }
            return pcp.toString().equals(toString());
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            for (String e : elements) {
                for (int i = 0; i < e.length(); i++) {
                    h = 31 * h + (e.charAt(i) & 0xff); // Do this work for UTF16?? or only Latin1
                }
            }
            hash = h;
        }
        return h;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRoot() {
        return elements.length == 0;
    }

    /** {@inheritDoc} */
    @Override
    public int length() {
        return toString().length();
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable NamespacePath parent() {
        if (isRoot()) {
            return null;
        }
        return new PackedTreePath(Arrays.copyOf(elements, elements.length - 1));
    }

    /** {@inheritDoc} */
    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String s = string;
        if (s == null) {
            StringJoiner sj = new StringJoiner("/", "/", "");
            for (String ss : elements) {
                sj.add(ss);
            }

            s = string = sj.toString();
        }
        return s;

    }

    static NamespacePath of(RuntimeComponentMirror component) {
        int depth = component.depth();
        return switch (depth) {
        case 0 -> ROOT;
        case 1 -> new PackedTreePath(component.name());
        default -> {
            String[] paths = new String[depth];
            RuntimeComponentMirror acc = component;
            for (int i = depth - 1; i >= 0; i--) {
                paths[i] = acc.name();
                acc = acc.parent;
            }
            yield new PackedTreePath(paths);
        }
        };
    }

    static NamespacePath of(ComponentSetup cc) {
        int depth = cc.depth;
        return switch (depth) {
        case 0 -> ROOT;
        case 1 -> new PackedTreePath(cc.name);
        default -> {
            String[] paths = new String[depth];
            ComponentSetup acc = cc;
            for (int i = depth - 1; i >= 0; i--) {
                paths[i] = acc.name;
                acc = acc.parent;
            }
            yield new PackedTreePath(paths);
        }
        };
    }

    public static NamespacePath of(ContainerSetup cc) {
        int depth = cc.depth;
        return switch (depth) {
        case 0 -> ROOT;
        case 1 -> new PackedTreePath(cc.name);
        default -> {
            String[] paths = new String[depth];
            ContainerSetup acc = cc;
            for (int i = depth - 1; i >= 0; i--) {
                paths[i] = acc.name;
                acc = acc.parent;
            }
            yield new PackedTreePath(paths);
        }
        };
    }

    /** {@inheritDoc} */
    @Override
    public NamespacePath add(NamespacePath other) {
        PackedTreePath pcp = (PackedTreePath) other;
        return new PackedTreePath(Stream.concat(Arrays.stream(elements), Arrays.stream(pcp.elements)).toArray(String[]::new));
    }
}
/// ** {@inheritDoc} */
// @Override
// public String toString() {
// String cached = this.cached;
// if (cached != null) {
// return cached;
// }
// if (component.parent == null) {
// cached = "/";
// } else {
// StringBuilder sb = new StringBuilder();
// toString(component, sb);
// cached = sb.toString();
// }
// return this.cached = cached;
// }
//
/// **
// * Used for recursively constructing the path string.
// *
// * @param component
// * the component to add
// * @param sb
// * the string builder to add to
// */
// private static void toString(AbstractComponent component, StringBuilder sb) {
// if (component.parent != null) {
// toString(component.parent, sb);
// sb.append("/");
// sb.append(component.name());
// }
// }