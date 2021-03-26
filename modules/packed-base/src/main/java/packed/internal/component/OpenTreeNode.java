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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import app.packed.base.Nullable;

/** A base class for writing tree-based data structures with named (String) nodes. */

// Vi rykker maaske ogsaa alt det naming prefix logic ned her???
public abstract class OpenTreeNode<T extends OpenTreeNode<T>> {

    /** The name of this node. */
    protected String name;

    /** Children of this node (lazily initialized). Insertion order maintained by {@link #treeNextSibling} and friends. */
    @Nullable
    LinkedHashMap<String, T> treeChildren;

    /** The depth of the component in the hierarchy (including any parent artifacts). */
    final int treeDepth;

    /** The parent of this component, or null for a root component. */
    @Nullable
    protected final T treeParent;

    OpenTreeNode(@Nullable T parent) {
        this.treeParent = parent;
        this.treeDepth = parent == null ? 0 : parent.treeDepth + 1;
    }

    int numberOfChildren() {
        return treeChildren == null ? 0 : treeChildren.size();
    }

    protected void addChildFinalName(T child, String name) {
        Map<String, T> c = treeChildren;
        if (c == null) {
            child.name = name;
            c = treeChildren = new LinkedHashMap<>();
            c.put(name, child);
            return;
        }

        String n = name;
        int counter = 1;
        while (c.putIfAbsent(n, child) != null) {
            n = name + counter++;
        }
    }

    @SuppressWarnings("unchecked")
    <S> List<S> toList(Function<T, S> mapper) {
        requireNonNull(mapper, "mapper is null");
        LinkedHashMap<String, T> children = treeChildren;
        if (children == null) {
            return List.of();
        } else {
            List.copyOf(children.values());
        }
        int size = treeChildren == null ? 0 : treeChildren.size();
        if (size == 0) {
            return List.of();
        } else {
            Object[] o = new Object[size];
            int index = 0;
            for (T child : children.values()) {
                o[index++] = mapper.apply(child);
            }
            return (List<S>) List.of(o);
        }
    }
}
