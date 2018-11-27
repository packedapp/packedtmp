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
package packed.internal.inject.buildnodes;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import app.packed.inject.Dependency;
import app.packed.inject.Key;
import packed.internal.inject.Node;

/**
 *
 */
public class NodeMap implements Iterable<Node<?>> {

    final HashMap<Key<?>, Node<?>> nodes = new HashMap<>();

    final NodeMap parent;

    public NodeMap() {
        this.parent = null;
    }

    public NodeMap(NodeMap parent) {
        this.parent = requireNonNull(parent);
    }

    @SuppressWarnings("unchecked")
    public <T> Node<T> getRecursive(Key<T> type) {
        // System.out.println("Looking for " + type);
        // System.out.println("Contents " + map.keySet());
        Node<T> node = (Node<T>) nodes.get(type);
        if (node == null && parent != null) {
            return parent.getRecursive(type);
        }
        return node;
    }

    public Node<?> getNode(Dependency dependency) {
        return getRecursive(dependency.getKey());
    }

    public void put(Node<?> node) {
        requireNonNull(node.getKey());
        nodes.put(node.getKey(), node);
    }

    public boolean putIfAbsent(Node<?> node) {
        requireNonNull(node.getKey());
        return nodes.putIfAbsent(node.getKey(), node) == null;
    }

    public List<Node<?>> toAll() {
        return new ArrayList<>(nodes.values());
    }

    public void toRuntimeNodes() {
        nodes.replaceAll((k, v) -> v.toRuntimeNode());
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Node<?>> iterator() {
        return nodes.values().iterator();
    }
}

// Couple of ideas.
// We will probably have a couple of maps
// One for 1 entry, one for no entries, etc? Want to be able to a quick newInjector(String.class,"ssdd);

// For the big one.
// We use an array of twos.
// With good hashfunctions, we should get almost no collisions.
// If we have Keys or type literals created from a raw type. It is unpacked before it is stored.
// In this we can just search for identity when calling get(Class<?>) as classes are internalized.

// The same goes with TypeLiteral, if it does not have an annotation. Unpack it.

// For some of the comples types, we can have an int switch in the type indicating
// what kind type is and provide faster equals, und so weiter.

// If key or typeliteral is a rawtype, it should have the same hashcode as the rawtype

// The wildcard things are going to be slow:(
// Maybe have a special map implementation that searches differently because it knows about wildcards

// Maybe not so big of a problem. We have flat hierachies.
// And 99 % of the time we look for an existing type

// Or maybe we store it in a special value.
// So we have the generic + All its specialized (if any)
// The hashcode would be of the raw type