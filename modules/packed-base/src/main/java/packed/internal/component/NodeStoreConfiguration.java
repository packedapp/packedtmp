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

/**
 *
 */
final class NodeStoreConfiguration {

    int index;

    final ComponentNodeConfiguration root;

    /** The pod used at runtime. */
    private NodeStore store;

    NodeStoreConfiguration(ComponentNodeConfiguration node) {
        this.root = requireNonNull(node);
    }

    NodeStore pod() {
        NodeStore s = store;
        if (s == null) {
            s = store = new NodeStore();
        }
        return s;
    }
}
